package com.example.service.tun2socks

import android.net.VpnService
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Lightweight local SOCKS5 proxy server bound to 127.0.0.1:1080.
 * Ensures all outgoing sockets are protected via [VpnService.protect] so that traffic
 * reaches the physical carrier/Wi-Fi network instead of re-entering the TUN interface.
 * Uses a strictly bounded thread pool with small stack size to prevent OutOfMemoryError.
 */
class LocalSocksProxyServer(
    private val vpnService: VpnService,
    private val port: Int = 1080,
    private val customPayload: String? = null,
    private val sshTunnelManager: com.example.service.ssh.SshTunnelManager? = null
) {
    companion object {
        const val TAG = "LocalSocksProxyServer"
        private const val MAX_CONCURRENT_CONNECTIONS = 256
        private const val MAX_WORKERS = 64
    }

    private var currentSshManager: com.example.service.ssh.SshTunnelManager? = sshTunnelManager

    fun updateSshTunnelManager(manager: com.example.service.ssh.SshTunnelManager?) {
        this.currentSshManager = manager
    }

    private val isRunning = AtomicBoolean(false)
    private val activeConnections = AtomicInteger(0)
    private var serverSocket: ServerSocket? = null
    private var executor: ExecutorService? = null
    private var serverThread: Thread? = null

    private fun readFully(inStream: InputStream, buffer: ByteArray): Boolean {
        var offset = 0
        while (offset < buffer.size) {
            val count = inStream.read(buffer, offset, buffer.size - offset)
            if (count < 0) return false
            offset += count
        }
        return true
    }

    fun start(): Boolean {
        if (isRunning.getAndSet(true)) return true

        try {
            // Check if port 1080 is already bound by an external injector or SSH tunnel
            val ss = ServerSocket()
            ss.reuseAddress = true
            ss.bind(InetSocketAddress("127.0.0.1", port))
            serverSocket = ss

            // Elastic thread pool with core timeout to save battery when idle
            executor = ThreadPoolExecutor(
                2,
                MAX_WORKERS,
                25L,
                TimeUnit.SECONDS,
                SynchronousQueue(),
                ThreadFactory { runnable ->
                    Thread(null, runnable, "SocksRelayWorker", 256 * 1024)
                },
                ThreadPoolExecutor.CallerRunsPolicy()
            ).apply {
                allowCoreThreadTimeOut(true)
            }

            Log.i(TAG, "Local SOCKS5 proxy server bound to 127.0.0.1:$port")

            serverThread = thread(name = "LocalSocks5Listener") {
                while (isRunning.get() && !Thread.interrupted()) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break

                        // Prevent thread explosion: throttle connections if at capacity
                        if (activeConnections.get() >= MAX_CONCURRENT_CONNECTIONS) {
                            try { clientSocket.close() } catch (_: Throwable) {}
                            continue
                        }

                        activeConnections.incrementAndGet()
                        try {
                            executor?.execute {
                                try {
                                    handleSocksClient(clientSocket)
                                } finally {
                                    activeConnections.decrementAndGet()
                                }
                            } ?: run {
                                activeConnections.decrementAndGet()
                                try { clientSocket.close() } catch (_: Throwable) {}
                            }
                        } catch (e: Exception) {
                            activeConnections.decrementAndGet()
                            try { clientSocket.close() } catch (_: Throwable) {}
                        }
                    } catch (e: Exception) {
                        if (isRunning.get()) {
                            Log.d(TAG, "Accept loop exception: ${e.message}")
                        }
                        break
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.w(TAG, "Port $port could not be bound (possibly used by external SSH injector). Listening bypassed: ${e.message}")
            isRunning.set(false)
            return false
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return
        Log.i(TAG, "Stopping local SOCKS5 proxy server on 127.0.0.1:$port")
        try { serverSocket?.close() } catch (_: Throwable) {}
        serverSocket = null
        serverThread?.interrupt()
        serverThread = null
        executor?.shutdownNow()
        executor = null
        activeConnections.set(0)
    }

    private fun handleSocksClient(client: Socket) {
        var remoteSocket: Socket? = null
        try {
            client.soTimeout = 8000
            client.tcpNoDelay = true

            val clientIn = client.getInputStream()
            val clientOut = client.getOutputStream()

            // 1. Handshake
            val version = clientIn.read()
            if (version != 0x05) {
                client.close()
                return
            }

            val nMethods = clientIn.read()
            if (nMethods <= 0) {
                client.close()
                return
            }
            val methods = ByteArray(nMethods)
            clientIn.read(methods)

            // Respond: NO AUTHENTICATION REQUIRED (0x00)
            clientOut.write(byteArrayOf(0x05, 0x00))
            clientOut.flush()

            // 2. Request details
            val reqVersion = clientIn.read()
            val cmd = clientIn.read()
            val rsv = clientIn.read()
            val atyp = clientIn.read()

            if (cmd != 0x01) { // CONNECT only
                clientOut.write(byteArrayOf(0x05, 0x07, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
                clientOut.flush()
                client.close()
                return
            }

            val targetHost: String
            when (atyp) {
                0x01 -> { // IPv4
                    val ipBytes = ByteArray(4)
                    if (!readFully(clientIn, ipBytes)) { client.close(); return }
                    targetHost = InetAddress.getByAddress(ipBytes).hostAddress ?: ""
                }
                0x03 -> { // Domain name
                    val domainLen = clientIn.read()
                    if (domainLen <= 0) { client.close(); return }
                    val domainBytes = ByteArray(domainLen)
                    if (!readFully(clientIn, domainBytes)) { client.close(); return }
                    targetHost = String(domainBytes)
                }
                0x04 -> { // IPv6
                    val ip6Bytes = ByteArray(16)
                    if (!readFully(clientIn, ip6Bytes)) { client.close(); return }
                    targetHost = InetAddress.getByAddress(ip6Bytes).hostAddress ?: ""
                }
                else -> {
                    client.close()
                    return
                }
            }

            val portBytes = ByteArray(2)
            if (!readFully(clientIn, portBytes)) { client.close(); return }
            val targetPort = ((portBytes[0].toInt() and 0xFF) shl 8) or (portBytes[1].toInt() and 0xFF)

            // 3. Primary: Route via active SSH Tunnel (Direct-TCPIP over WebSocket/CloudFront VPS)
            val ssh = currentSshManager ?: com.example.service.ssh.SshTunnelManager.activeInstance
            // Si SSH está en proceso de handshake, esperar brevemente (hasta 3.5s)
            if (ssh != null && !ssh.isConnected() && ssh.isConnecting()) {
                val startWait = System.currentTimeMillis()
                while (!ssh.isConnected() && ssh.isConnecting() && System.currentTimeMillis() - startWait < 3500) {
                    try { Thread.sleep(100) } catch (_: InterruptedException) { break }
                }
            }
            if (ssh != null && ssh.isConnected()) {
                var channel: com.jcraft.jsch.ChannelDirectTCPIP? = null
                try {
                    channel = ssh.openChannel(targetHost, targetPort)
                    if (channel != null) {
                        channel.connect(7000)
                        val channelIn = channel.inputStream
                        val channelOut = channel.outputStream

                        // Send SOCKS5 SUCCESS (0x00)
                        clientOut.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
                        clientOut.flush()

                        val latch = java.util.concurrent.CountDownLatch(1)
                        executor?.execute {
                            try {
                                pipeStreamDirection(channelIn, clientOut) {
                                    try { client.close() } catch (_: Throwable) {}
                                }
                            } finally {
                                latch.countDown()
                            }
                        }

                        try {
                            pipeStreamDirection(clientIn, channelOut) {
                                try { channel.disconnect() } catch (_: Throwable) {}
                            }
                        } finally {
                            latch.countDown()
                        }

                        try {
                            latch.await(60, TimeUnit.SECONDS)
                        } catch (_: Throwable) {}
                        return
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "SSH direct channel to $targetHost:$targetPort failed: ${e.message}")
                } finally {
                    try { channel?.disconnect() } catch (_: Throwable) {}
                }
            }

            // 4. Fallback: Connect directly via PROTECTED socket (only for Wi-Fi / uncapped data)
            if (!customPayload.isNullOrBlank()) {
                // En saldo cero con payload, los sockets directos externos están bloqueados por la antena.
                // Responder fallo SOCKS5 de inmediato para evitar que las aplicaciones se cuelguen esperando timeout.
                clientOut.write(byteArrayOf(0x05, 0x05, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
                clientOut.flush()
                return
            }

            val remote = Socket()
            vpnService.protect(remote) // Essential: route outside TUN interface
            remote.soTimeout = 4000
            remote.tcpNoDelay = true
            remote.connect(InetSocketAddress(targetHost, targetPort), 3500)
            remoteSocket = remote

            // Send SOCKS5 SUCCESS (0x00)
            clientOut.write(byteArrayOf(0x05, 0x00, 0x00, 0x01, 0, 0, 0, 0, 0, 0))
            clientOut.flush()

            // Inject payload if configured (DTunnel/SSH Injector style payload injection)
            injectPayloadIfPresent(remote, targetHost, targetPort)

            // 5. Bi-directional relay WITHOUT premature socket close
            val remoteIn = remote.getInputStream()
            val remoteOut = remote.getOutputStream()

            val latch = java.util.concurrent.CountDownLatch(1)
            executor?.execute {
                try {
                    pipeStreamDirection(remoteIn, clientOut) {
                        try { client.close() } catch (_: Throwable) {}
                    }
                } finally {
                    latch.countDown()
                }
            }

            try {
                pipeStreamDirection(clientIn, remoteOut) {
                    try { remote.close() } catch (_: Throwable) {}
                }
            } finally {
                latch.countDown()
            }

            try {
                latch.await(60, TimeUnit.SECONDS)
            } catch (_: Throwable) {}
        } catch (e: Exception) {
            // Socket error or disconnect
        } finally {
            try { client.close() } catch (_: Throwable) {}
            try { remoteSocket?.close() } catch (_: Throwable) {}
        }
    }

    private fun injectPayloadIfPresent(remote: Socket, targetHost: String, targetPort: Int) {
        val payload = customPayload?.trim()
        if (payload.isNullOrEmpty()) return

        try {
            val formatted = payload
                .replace("[crlf]", "\r\n")
                .replace("[cr]", "\r")
                .replace("[lf]", "\n")
                .replace("[host_port]", "$targetHost:$targetPort")
                .replace("[host]", targetHost)
                .replace("[port]", targetPort.toString())
                .replace("[instant_split]", "")
                .replace("[split]", "")
                .replace("[protocol]", "HTTP/1.1")

            val out = remote.getOutputStream()
            out.write(formatted.toByteArray(Charsets.UTF_8))
            out.flush()
            Log.d(TAG, "Payload injected successfully to $targetHost:$targetPort (${formatted.length} bytes)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to inject payload: ${e.message}")
        }
    }

    private fun pipeStreamDirection(input: InputStream, output: OutputStream, onClose: (() -> Unit)? = null) {
        val buffer = ByteArray(16384)
        try {
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
                output.flush()
            }
        } catch (_: Exception) {
        } finally {
            try { onClose?.invoke() } catch (_: Throwable) {}
        }
    }
}
