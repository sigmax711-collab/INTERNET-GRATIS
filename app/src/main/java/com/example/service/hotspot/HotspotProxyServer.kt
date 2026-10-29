package com.example.service.hotspot

import android.content.Context
import android.util.Log
import com.example.service.ssh.SshTunnelManager
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Servidor proxy HTTP local (0.0.0.0:8080) para compartir la conexión VPN
 * a través de Zona Wi-Fi / Hotspot a otros dispositivos (móviles, PCs, Smart TVs).
 *
 * En Android, el tráfico de los dispositivos conectados al Hotspot no pasa
 * automáticamente por la interfaz tun0. Al configurar este proxy en el dispositivo cliente,
 * todo su tráfico es enrutado de forma transparente a través del túnel SSH de la VPN.
 */
class HotspotProxyServer(
    private val context: Context,
    private val port: Int = 8080
) {
    companion object {
        private const val TAG = "HotspotProxyServer"
        var activeInstance: HotspotProxyServer? = null
            private set

        /**
         * Detecta la dirección IP de la interfaz de Zona Wi-Fi (Hotspot/AP) o LAN.
         * En la gran mayoría de dispositivos Android la IP por defecto es 192.168.43.1.
         */
        fun getHotspotIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val iface = interfaces.nextElement()
                    val name = iface.name.lowercase()
                    // Interfaces comunes de hotspot en Android: ap0, wlan1, rndis0, tether
                    if (name.contains("ap") || name.contains("rndis") || name.contains("tether")) {
                        val addrs = iface.inetAddresses
                        while (addrs.hasMoreElements()) {
                            val addr = addrs.nextElement()
                            if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                                return addr.hostAddress ?: "192.168.43.1"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            return "192.168.43.1"
        }
    }

    private val isRunning = AtomicBoolean(false)
    private var serverSocket: ServerSocket? = null
    private var listenerThread: Thread? = null
    private var executor: ExecutorService? = null
    private val clientCount = AtomicInteger(0)

    fun isRunning(): Boolean = isRunning.get()
    fun getClientCount(): Int = clientCount.get()

    fun start(): Boolean {
        if (isRunning.getAndSet(true)) return true

        return try {
            activeInstance = this
            val ss = ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"))
            ss.reuseAddress = true
            serverSocket = ss

            executor = ThreadPoolExecutor(
                2,
                48,
                30L,
                TimeUnit.SECONDS,
                SynchronousQueue(),
                ThreadFactory { r ->
                    Thread(null, r, "HotspotWorker", 256 * 1024)
                },
                ThreadPoolExecutor.CallerRunsPolicy()
            ).apply {
                allowCoreThreadTimeOut(true)
            }

            listenerThread = thread(name = "HotspotProxyListener", isDaemon = true) {
                Log.i(TAG, "Servidor proxy Hotspot escuchando en 0.0.0.0:$port")
                while (isRunning.get() && !Thread.interrupted()) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        clientCount.incrementAndGet()
                        executor?.execute {
                            try {
                                handleClient(client)
                            } finally {
                                clientCount.decrementAndGet()
                            }
                        }
                    } catch (_: Exception) {
                        break
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error iniciando proxy Hotspot en puerto $port: ${e.message}", e)
            isRunning.set(false)
            false
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        listenerThread?.interrupt()
        listenerThread = null

        executor?.shutdownNow()
        executor = null

        if (activeInstance == this) {
            activeInstance = null
        }
        Log.i(TAG, "Servidor proxy Hotspot detenido.")
    }

    private fun handleClient(client: Socket) {
        var remoteChannel: com.jcraft.jsch.ChannelDirectTCPIP? = null
        var directSocket: Socket? = null

        try {
            client.soTimeout = 10000
            val clientIn = client.getInputStream()
            val clientOut = client.getOutputStream()

            // Leer línea de solicitud HTTP (ej: CONNECT example.com:443 HTTP/1.1)
            val headerLine = readLine(clientIn) ?: return
            val parts = headerLine.split(" ")
            if (parts.size < 2) return

            val method = parts[0].uppercase()
            val uri = parts[1]

            var targetHost = ""
            var targetPort = 80

            if (method == "CONNECT") {
                // HTTPS CONNECT host:port
                val hostPort = uri.split(":")
                targetHost = hostPort[0]
                targetPort = hostPort.getOrNull(1)?.toIntOrNull() ?: 443

                // Consumir el resto de las cabeceras hasta la línea vacía
                while (true) {
                    val line = readLine(clientIn) ?: break
                    if (line.isEmpty()) break
                }
            } else {
                // Solicitud HTTP estándar GET http://host/path o GET /path con Host: host
                val urlObj = try {
                    java.net.URI(uri)
                } catch (_: Exception) { null }

                if (urlObj != null && !urlObj.host.isNullOrBlank()) {
                    targetHost = urlObj.host
                    targetPort = if (urlObj.port > 0) urlObj.port else 80
                }

                // Si no vino en la URI, buscar en cabecera Host:
                val headers = mutableListOf<String>()
                headers.add(headerLine)
                while (true) {
                    val line = readLine(clientIn) ?: break
                    if (line.isEmpty()) break
                    headers.add(line)
                    if (targetHost.isBlank() && line.startsWith("Host:", ignoreCase = true)) {
                        val hostVal = line.substring(5).trim()
                        val hp = hostVal.split(":")
                        targetHost = hp[0]
                        targetPort = hp.getOrNull(1)?.toIntOrNull() ?: 80
                    }
                }
            }

            if (targetHost.isBlank()) {
                client.close()
                return
            }

            // Conectar al destino a través del túnel SSH de la VPN
            val ssh = SshTunnelManager.activeInstance
            if (ssh != null && ssh.isConnected()) {
                val ch = ssh.openChannel(targetHost, targetPort)
                if (ch != null) {
                    ch.connect(5000)
                    remoteChannel = ch
                    val chIn = ch.inputStream
                    val chOut = ch.outputStream

                    if (method == "CONNECT") {
                        // Responder al cliente HTTP 200 Connection Established
                        clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(Charsets.ISO_8859_1))
                        clientOut.flush()
                    }

                    // Reenviar datos bidireccionalmente
                    pipeBidirectional(clientIn, clientOut, chIn, chOut)
                    return
                }
            }

            // Fallback a socket directo si SSH no está disponible
            val s = Socket()
            s.soTimeout = 8000
            s.connect(java.net.InetSocketAddress(targetHost, targetPort), 4000)
            directSocket = s

            val sIn = s.getInputStream()
            val sOut = s.getOutputStream()

            if (method == "CONNECT") {
                clientOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray(Charsets.ISO_8859_1))
                clientOut.flush()
            }

            pipeBidirectional(clientIn, clientOut, sIn, sOut)
        } catch (_: Exception) {
        } finally {
            try { client.close() } catch (_: Throwable) {}
            try { remoteChannel?.disconnect() } catch (_: Throwable) {}
            try { directSocket?.close() } catch (_: Throwable) {}
        }
    }

    private fun readLine(inStream: InputStream): String? {
        val sb = StringBuilder()
        var c: Int
        while (inStream.read().also { c = it } != -1) {
            if (c == '\n'.code) {
                break
            }
            if (c != '\r'.code) {
                sb.append(c.toChar())
            }
        }
        if (sb.isEmpty() && c == -1) return null
        return sb.toString()
    }

    private fun pipeBidirectional(
        clientIn: InputStream,
        clientOut: OutputStream,
        remoteIn: InputStream,
        remoteOut: OutputStream
    ) {
        val latch = java.util.concurrent.CountDownLatch(1)

        executor?.execute {
            val buf = ByteArray(16384)
            try {
                var r: Int
                while (remoteIn.read(buf).also { r = it } != -1) {
                    clientOut.write(buf, 0, r)
                    clientOut.flush()
                }
            } catch (_: Exception) {
            } finally {
                latch.countDown()
            }
        }

        val buf = ByteArray(16384)
        try {
            var r: Int
            while (clientIn.read(buf).also { r = it } != -1) {
                remoteOut.write(buf, 0, r)
                remoteOut.flush()
            }
        } catch (_: Exception) {
        } finally {
            latch.countDown()
        }

        try {
            latch.await(60, TimeUnit.SECONDS)
        } catch (_: Throwable) {}
    }
}
