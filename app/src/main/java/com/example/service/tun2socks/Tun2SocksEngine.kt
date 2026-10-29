package com.example.service.tun2socks

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * High-performance Tun2Socks engine for Android VpnService.
 * Bridges raw L3 IP packets from the TUN interface (tun0) with L4 SOCKS5 / TCP / UDP sockets.
 * Supports:
 * - Native libtun2socks.so integration via JNI if available in jniLibs.
 * - Pure Kotlin ManagedTun2Socks fallback with protected sockets (VpnService.protect).
 * - UDP DNS forwarding (port 53) to 1.1.1.1 / 8.8.8.8.
 * - Virtual TCP connection state machine with SOCKS5 (127.0.0.1:1080) relay.
 */
class Tun2SocksEngine(
    private val vpnService: VpnService,
    private val pfd: ParcelFileDescriptor,
    private val socksHost: String = "127.0.0.1",
    private val socksPort: Int = 1080,
    private val dnsServers: List<String> = listOf("1.1.1.1", "8.8.8.8")
) {

    private val isRunning = AtomicBoolean(false)
    private var workerThread: Thread? = null
    private var dnsExecutor: ExecutorService? = null
    private var tcpExecutor: ExecutorService? = null

    // Track active TCP sessions: key = "srcIp:srcPort->dstIp:dstPort"
    private val tcpSessions = ConcurrentHashMap<String, TcpSession>()

    companion object {
        const val TAG = "Tun2SocksEngine"
        var isNativeLibLoaded: Boolean = false
            private set

        init {
            try {
                System.loadLibrary("tun2socks")
                isNativeLibLoaded = true
                Log.i(TAG, "Native library libtun2socks.so loaded successfully.")
            } catch (e: Throwable) {
                isNativeLibLoaded = false
                Log.d(TAG, "libtun2socks.so not found in APK. Using high-performance ManagedTun2Socks.")
            }
        }
    }

    // Native JNI hooks if libtun2socks.so is bundled
    private external fun nativeStart(
        vpnFd: Int,
        vpnAddress: String,
        netmask: String,
        socksServer: String,
        dnsServer: String
    ): Int

    private external fun nativeStop(): Int

    fun start() {
        if (isRunning.getAndSet(true)) return

        Log.i(TAG, "Starting Tun2Socks engine (Target SOCKS: $socksHost:$socksPort, DNS: $dnsServers)")

        if (isNativeLibLoaded) {
            try {
                val fd = pfd.fd
                val dns = dnsServers.firstOrNull() ?: "1.1.1.1"
                workerThread = thread(name = "NativeTun2SocksThread") {
                    nativeStart(fd, "10.0.0.2", "255.255.255.0", "$socksHost:$socksPort", dns)
                }
                Log.i(TAG, "Native tun2socks worker thread launched on fd: $fd")
                return
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to start native tun2socks, falling back to managed implementation", e)
            }
        }

        // Managed Kotlin Tun2Socks Engine - bounded elastic thread pools with idle timeout
        dnsExecutor = ThreadPoolExecutor(
            1,
            4,
            20L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(64)
        ).apply {
            allowCoreThreadTimeOut(true)
        }

        tcpExecutor = ThreadPoolExecutor(
            2,
            64,
            25L,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            ThreadFactory { runnable ->
                Thread(null, runnable, "TunTcpWorker", 256 * 1024)
            },
            ThreadPoolExecutor.CallerRunsPolicy()
        ).apply {
            allowCoreThreadTimeOut(true)
        }

        workerThread = thread(name = "ManagedTun2SocksRouter") {
            runManagedLoop()
        }
    }

    fun stop() {
        if (!isRunning.getAndSet(false)) return

        Log.i(TAG, "Stopping Tun2Socks engine...")

        if (isNativeLibLoaded) {
            try {
                nativeStop()
            } catch (_: Throwable) {}
        }

        workerThread?.interrupt()
        workerThread = null

        dnsExecutor?.shutdownNow()
        dnsExecutor = null

        tcpExecutor?.shutdownNow()
        tcpExecutor = null

        tcpSessions.values.forEach { session ->
            try { session.close() } catch (_: Throwable) {}
        }
        tcpSessions.clear()

        Log.i(TAG, "Tun2Socks engine stopped.")
    }

    /**
     * Main packet reading loop from TUN interface
     */
    private fun runManagedLoop() {
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val outputStream = FileOutputStream(pfd.fileDescriptor)
        val buffer = ByteArray(32767)

        Log.i(TAG, "ManagedTun2Socks loop active on tun0")

        try {
            while (isRunning.get() && !Thread.interrupted()) {
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) {
                    processIpPacket(buffer, bytesRead, outputStream)
                } else if (bytesRead < 0) {
                    break
                }
            }
        } catch (e: Exception) {
            if (isRunning.get()) {
                Log.e(TAG, "Error in ManagedTun2Socks loop", e)
            }
        } finally {
            try { inputStream.close() } catch (_: Throwable) {}
            try { outputStream.close() } catch (_: Throwable) {}
        }
    }

    /**
     * Inspects IPv4 packet and dispatches to UDP (DNS), TCP (SOCKS relay) or ICMP
     */
    private fun processIpPacket(packet: ByteArray, length: Int, out: FileOutputStream) {
        if (length < 20) return

        val versionAndIhl = packet[0].toInt() and 0xFF
        val version = versionAndIhl shr 4
        if (version != 4) return // Only IPv4 is processed

        val ihl = (versionAndIhl and 0x0F) * 4
        if (length < ihl) return

        val protocol = packet[9].toInt() and 0xFF
        val srcIp = getIpString(packet, 12)
        val dstIp = getIpString(packet, 16)

        when (protocol) {
            17 -> { // UDP
                handleUdpPacket(packet, ihl, length, srcIp, dstIp, out)
            }
            6 -> { // TCP
                handleTcpPacket(packet, ihl, length, srcIp, dstIp, out)
            }
            1 -> { // ICMP (Ping)
                handleIcmpPacket(packet, ihl, length, srcIp, dstIp, out)
            }
        }
    }

    // Cache DNS en memoria para evitar saturar el canal y dar respuesta en 0ms a dominios comunes
    private data class CachedDns(val response: ByteArray, val timestamp: Long)
    private val dnsCache = ConcurrentHashMap<String, CachedDns>()

    // ---------------------------------------------------------------------------------------------
    // DNS & UDP Forwarding
    // ---------------------------------------------------------------------------------------------
    private fun handleUdpPacket(
        packet: ByteArray,
        ihl: Int,
        totalLength: Int,
        srcIp: String,
        dstIp: String,
        out: FileOutputStream
    ) {
        if (totalLength < ihl + 8) return

        val srcPort = getPort(packet, ihl)
        val dstPort = getPort(packet, ihl + 2)
        val udpLength = getPort(packet, ihl + 4)
        val payloadOffset = ihl + 8
        val payloadLength = totalLength - payloadOffset

        if (payloadLength <= 0 || udpLength < 8) return

        val dnsQuery = packet.copyOfRange(payloadOffset, payloadOffset + payloadLength)
        if (dnsQuery.size < 12) return

        // Extraer id de consulta DNS (primeros 2 bytes)
        val queryTxId = ((dnsQuery[0].toInt() and 0xFF) shl 8) or (dnsQuery[1].toInt() and 0xFF)
        // Usar los bytes de la pregunta (a partir del byte 12) como clave de caché
        val cacheKey = java.util.Arrays.hashCode(dnsQuery.copyOfRange(12, dnsQuery.size)).toString()

        // 1. Revisar caché DNS local
        val cached = dnsCache[cacheKey]
        val now = System.currentTimeMillis()
        if (cached != null && (now - cached.timestamp < 180_000L)) {
            val cachedResp = cached.response.copyOf()
            // Parchear con el Transaction ID de la nueva consulta
            cachedResp[0] = ((queryTxId shr 8) and 0xFF).toByte()
            cachedResp[1] = (queryTxId and 0xFF).toByte()
            sendUdpResponse(out, dstIp, dstPort, srcIp, srcPort, cachedResp)
            return
        }

        // Forward DNS queries asynchronously: SSH tunnel DNS-over-TCP first, then protected UDP
        dnsExecutor?.execute {
            try {
                // 2. Primary: Resolve via SSH Tunnel (bypasses carrier DNS block & zero-balance restrictions)
                val ssh = com.example.service.ssh.SshTunnelManager.activeInstance
                var respData: ByteArray? = null
                if (ssh != null && ssh.isConnected()) {
                    respData = ssh.resolveDnsOverTcp(dnsQuery)
                }

                // 3. Fallback: Standard protected UDP socket to DNS server
                if (respData == null) {
                    val targetDns = if (dnsServers.contains(dstIp)) dstIp else dnsServers.firstOrNull() ?: "1.1.1.1"
                    val socket = DatagramSocket()
                    vpnService.protect(socket)
                    socket.soTimeout = 3000

                    val sendPacket = DatagramPacket(
                        dnsQuery,
                        dnsQuery.size,
                        InetAddress.getByName(targetDns),
                        53
                    )
                    socket.send(sendPacket)

                    val responseBuffer = ByteArray(4096)
                    val recvPacket = DatagramPacket(responseBuffer, responseBuffer.size)
                    socket.receive(recvPacket)
                    socket.close()

                    respData = recvPacket.data.copyOfRange(0, recvPacket.length)
                }

                if (respData != null) {
                    // Guardar en caché
                    if (dnsCache.size > 256) dnsCache.clear()
                    dnsCache[cacheKey] = CachedDns(respData, now)

                    sendUdpResponse(out, dstIp, dstPort, srcIp, srcPort, respData)
                }
            } catch (e: Exception) {
                // Timeout or unresolvable
            }
        }
    }

    private fun sendUdpResponse(
        out: FileOutputStream,
        srcIp: String,
        srcPort: Int,
        dstIp: String,
        dstPort: Int,
        payload: ByteArray
    ) {
        val totalLen = 20 + 8 + payload.size
        val packet = ByteArray(totalLen)

        // IPv4 Header
        packet[0] = 0x45.toByte() // Version 4, IHL 5 (20 bytes)
        packet[1] = 0x00
        setShort(packet, 2, totalLen)
        setShort(packet, 4, (Math.random() * 65535).toInt())
        packet[6] = 0x40.toByte() // Don't fragment
        packet[7] = 0x00
        packet[8] = 64.toByte() // TTL
        packet[9] = 17.toByte() // Protocol UDP
        setIp(packet, 12, srcIp)
        setIp(packet, 16, dstIp)
        setChecksum(packet, 10, calculateChecksum(packet, 0, 20))

        // UDP Header
        setShort(packet, 20, srcPort)
        setShort(packet, 22, dstPort)
        setShort(packet, 24, 8 + payload.size)
        setShort(packet, 26, 0) // Checksum optional for IPv4 UDP

        // Payload
        System.arraycopy(payload, 0, packet, 28, payload.size)

        synchronized(out) {
            try {
                out.write(packet)
                out.flush()
            } catch (_: Throwable) {}
        }
    }

    // ---------------------------------------------------------------------------------------------
    // TCP Virtual State & SOCKS5 Relay
    // ---------------------------------------------------------------------------------------------
    private fun handleTcpPacket(
        packet: ByteArray,
        ihl: Int,
        totalLength: Int,
        srcIp: String,
        dstIp: String,
        out: FileOutputStream
    ) {
        if (totalLength < ihl + 20) return

        val srcPort = getPort(packet, ihl)
        val dstPort = getPort(packet, ihl + 2)
        val seqNum = getUInt32(packet, ihl + 4)
        val ackNum = getUInt32(packet, ihl + 8)
        val dataOffset = ((packet[ihl + 12].toInt() and 0xFF) shr 4) * 4
        val flags = packet[ihl + 13].toInt() and 0xFF

        val isSyn = (flags and 0x02) != 0
        val isAck = (flags and 0x10) != 0
        val isFin = (flags and 0x01) != 0
        val isRst = (flags and 0x04) != 0

        val payloadOffset = ihl + dataOffset
        val payloadLength = totalLength - payloadOffset
        val sessionKey = "$srcIp:$srcPort->$dstIp:$dstPort"

        if (isRst) {
            tcpSessions.remove(sessionKey)?.close()
            return
        }

        if (isSyn && !isAck) {
            // Prevent session table memory leak
            if (tcpSessions.size >= 512) {
                val it = tcpSessions.entries.iterator()
                while (it.hasNext()) {
                    val entry = it.next()
                    if (entry.value.isClosed.get()) {
                        it.remove()
                    }
                }
                if (tcpSessions.size >= 512) {
                    val oldestKey = tcpSessions.keys.firstOrNull()
                    if (oldestKey != null) {
                        tcpSessions.remove(oldestKey)?.close()
                    }
                }
            }

            // New TCP Connection initiation (SYN)
            val session = TcpSession(
                srcIp = srcIp,
                srcPort = srcPort,
                dstIp = dstIp,
                dstPort = dstPort,
                initialClientSeq = seqNum,
                out = out
            )
            tcpSessions[sessionKey] = session

            // Send SYN-ACK back to TUN with MSS option (1360 bytes)
            session.sendSynAck()

            // Connect in background: direct SSH tunnel channel or local SOCKS5 proxy
            try {
                tcpExecutor?.execute {
                    session.startRelay(socksHost, socksPort, vpnService)
                }
            } catch (_: Throwable) {
                session.close()
                tcpSessions.remove(sessionKey)
            }
            return
        }

        val session = tcpSessions[sessionKey]
        if (session != null) {
            if (payloadLength > 0) {
                val data = packet.copyOfRange(payloadOffset, payloadOffset + payloadLength)
                session.handleClientData(seqNum, data)
            }

            if (isFin) {
                session.handleClientFin(seqNum)
                tcpSessions.remove(sessionKey)
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ICMP Echo (Ping)
    // ---------------------------------------------------------------------------------------------
    private fun handleIcmpPacket(
        packet: ByteArray,
        ihl: Int,
        totalLength: Int,
        srcIp: String,
        dstIp: String,
        out: FileOutputStream
    ) {
        if (totalLength < ihl + 8) return
        val type = packet[ihl].toInt() and 0xFF
        if (type == 8) { // Echo Request
            // Convert to Echo Reply (type 0)
            val reply = packet.copyOf(totalLength)
            setIp(reply, 12, dstIp)
            setIp(reply, 16, srcIp)
            setChecksum(reply, 10, 0)
            setChecksum(reply, 10, calculateChecksum(reply, 0, ihl))

            reply[ihl] = 0 // Type = Echo Reply
            setChecksum(reply, ihl + 2, 0)
            val icmpLen = totalLength - ihl
            setChecksum(reply, ihl + 2, calculateChecksum(reply, ihl, icmpLen))

            synchronized(out) {
                try {
                    out.write(reply)
                    out.flush()
                } catch (_: Throwable) {}
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // TCP Session Manager
    // ---------------------------------------------------------------------------------------------
    private inner class TcpSession(
        val srcIp: String,
        val srcPort: Int,
        val dstIp: String,
        val dstPort: Int,
        val initialClientSeq: Long,
        val out: FileOutputStream
    ) {
        private var socket: Socket? = null
        private var channel: com.jcraft.jsch.ChannelDirectTCPIP? = null
        private val mySeq = AtomicInteger((Math.random() * 100000).toInt())
        private var expectedClientSeq = initialClientSeq + 1
        val isClosed = AtomicBoolean(false)
        private val pendingClientData = java.util.concurrent.ConcurrentLinkedQueue<ByteArray>()
        private val isSocketReady = AtomicBoolean(false)

        fun sendSynAck() {
            // Include TCP MSS Option: Kind=2, Length=4, Value=1360 (0x0550)
            // Telling client kernel the max segment size is 1360 bytes
            val mssOption = byteArrayOf(0x02, 0x04, 0x05, 0x50)
            sendTcpPacket(
                flags = 0x12, // SYN + ACK
                seq = mySeq.get().toLong(),
                ack = expectedClientSeq,
                payload = ByteArray(0),
                tcpOptions = mssOption
            )
            mySeq.incrementAndGet()
        }

        fun startRelay(socksHost: String, socksPort: Int, vpnService: VpnService) {
            // 1. Primary: Direct channel through active SSH tunnel (avoids localhost TCP-over-TCP overhead)
            val ssh = com.example.service.ssh.SshTunnelManager.activeInstance
            if (ssh != null && ssh.isConnected()) {
                try {
                    val ch = ssh.openChannel(dstIp, dstPort)
                    if (ch != null) {
                        ch.connect(6000)
                        channel = ch
                        val chIn = ch.inputStream
                        val chOut = ch.outputStream

                        isSocketReady.set(true)
                        while (!pendingClientData.isEmpty()) {
                            val chunk = pendingClientData.poll() ?: break
                            chOut.write(chunk)
                        }
                        chOut.flush()

                        val buffer = ByteArray(8192)
                        while (!isClosed.get() && !Thread.interrupted()) {
                            val count = chIn.read(buffer)
                            if (count > 0) {
                                sendDataSegmented(buffer, count)
                            } else if (count < 0) {
                                break
                            }
                        }
                        return
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "SSH direct channel to $dstIp:$dstPort fallback: ${e.message}")
                } finally {
                    if (channel != null) {
                        close()
                        return
                    }
                }
            }

            // 2. Secondary: Connect via Local SOCKS5 proxy on 127.0.0.1:1080
            connectToSocks(socksHost, socksPort, vpnService)
        }

        private fun readFully(inStream: InputStream, buffer: ByteArray): Boolean {
            var offset = 0
            while (offset < buffer.size) {
                val count = inStream.read(buffer, offset, buffer.size - offset)
                if (count < 0) return false
                offset += count
            }
            return true
        }

        fun connectToSocks(socksHost: String, socksPort: Int, vpnService: VpnService) {
            try {
                val s = Socket()
                vpnService.protect(s) // Crucial: protect so traffic doesn't loop back to TUN
                s.soTimeout = 8000
                s.tcpNoDelay = true
                s.connect(InetSocketAddress(socksHost, socksPort), 3000)
                socket = s

                // Perform SOCKS5 Handshake with 127.0.0.1:1080
                val inStream = s.getInputStream()
                val outStream = s.getOutputStream()

                // 1. Auth negotiation (No authentication)
                outStream.write(byteArrayOf(0x05, 0x01, 0x00))
                outStream.flush()

                val authResp = ByteArray(2)
                if (!readFully(inStream, authResp) || authResp[0].toInt() != 0x05 || authResp[1].toInt() != 0x00) {
                    throw IllegalStateException("SOCKS5 auth negotiation failed")
                }

                // 2. CONNECT dstIp:dstPort (IPv4 = 0x01)
                val dstIpBytes = InetAddress.getByName(dstIp).address
                val connReq = ByteArray(4 + dstIpBytes.size + 2)
                connReq[0] = 0x05 // SOCKS5
                connReq[1] = 0x01 // CMD CONNECT
                connReq[2] = 0x00 // RSV
                connReq[3] = 0x01 // ATYP IPv4
                System.arraycopy(dstIpBytes, 0, connReq, 4, dstIpBytes.size)
                connReq[connReq.size - 2] = ((dstPort shr 8) and 0xFF).toByte()
                connReq[connReq.size - 1] = (dstPort and 0xFF).toByte()

                outStream.write(connReq)
                outStream.flush()

                val connResp = ByteArray(10)
                if (!readFully(inStream, connResp) || connResp[1].toInt() != 0x00) {
                    throw IllegalStateException("SOCKS5 connect to $dstIp:$dstPort failed (code: ${connResp.getOrNull(1)})")
                }

                // Drain pending data buffered during handshake
                isSocketReady.set(true)
                while (!pendingClientData.isEmpty()) {
                    val pendingChunk = pendingClientData.poll() ?: break
                    outStream.write(pendingChunk)
                }
                outStream.flush()

                // Read loop from SOCKS proxy back to TUN with MSS segment enforcement
                val buffer = ByteArray(8192)
                while (!isClosed.get() && !Thread.interrupted()) {
                    val count = inStream.read(buffer)
                    if (count > 0) {
                        sendDataSegmented(buffer, count)
                    } else if (count < 0) {
                        break
                    }
                }
            } catch (e: Exception) {
                // If local SOCKS connection fails, try direct protected socket as fallback
                connectDirectFallback(vpnService)
            } finally {
                close()
            }
        }

        private fun connectDirectFallback(vpnService: VpnService) {
            try {
                val s = Socket()
                vpnService.protect(s)
                s.soTimeout = 8000
                s.tcpNoDelay = true
                s.connect(InetSocketAddress(dstIp, dstPort), 4000)
                socket = s

                val inStream = s.getInputStream()
                val outStream = s.getOutputStream()

                isSocketReady.set(true)
                while (!pendingClientData.isEmpty()) {
                    val pendingChunk = pendingClientData.poll() ?: break
                    outStream.write(pendingChunk)
                }
                outStream.flush()

                val buffer = ByteArray(8192)
                while (!isClosed.get() && !Thread.interrupted()) {
                    val count = inStream.read(buffer)
                    if (count > 0) {
                        sendDataSegmented(buffer, count)
                    } else if (count < 0) {
                        break
                    }
                }
            } catch (_: Exception) {
                // Connection closed
            }
        }

        /**
         * Segments inbound TCP payload into chunks of max MSS (1360 bytes)
         * so each IP packet stays well within the 1500 TUN MTU.
         * This prevents EMSGSIZE drops that cause "DNS works but no internet".
         */
        private fun sendDataSegmented(buffer: ByteArray, totalCount: Int) {
            val maxMss = 1360
            var offset = 0
            while (offset < totalCount && !isClosed.get()) {
                val chunkSize = minOf(maxMss, totalCount - offset)
                val chunk = buffer.copyOfRange(offset, offset + chunkSize)
                val isLast = (offset + chunkSize >= totalCount)
                sendTcpPacket(
                    flags = if (isLast) 0x18 else 0x10, // PSH+ACK on last chunk, ACK on intermediate
                    seq = mySeq.get().toLong(),
                    ack = expectedClientSeq,
                    payload = chunk
                )
                mySeq.addAndGet(chunkSize)
                offset += chunkSize
            }
        }

        fun handleClientData(clientSeq: Long, data: ByteArray) {
            expectedClientSeq = clientSeq + data.size
            // Send ACK back to TUN
            sendTcpPacket(
                flags = 0x10, // ACK
                seq = mySeq.get().toLong(),
                ack = expectedClientSeq,
                payload = ByteArray(0)
            )

            if (!isSocketReady.get() || (socket == null && channel == null)) {
                pendingClientData.add(data.clone())
            } else {
                try {
                    val chOut = channel?.outputStream
                    if (chOut != null) {
                        chOut.write(data)
                        chOut.flush()
                    } else {
                        val outStream = socket?.getOutputStream()
                        if (outStream != null) {
                            outStream.write(data)
                            outStream.flush()
                        } else {
                            pendingClientData.add(data.clone())
                        }
                    }
                } catch (e: Exception) {
                    close()
                }
            }
        }

        fun handleClientFin(clientSeq: Long) {
            expectedClientSeq = clientSeq + 1
            // Send FIN + ACK
            sendTcpPacket(
                flags = 0x11, // FIN + ACK
                seq = mySeq.get().toLong(),
                ack = expectedClientSeq,
                payload = ByteArray(0)
            )
            close()
        }

        private fun sendTcpPacket(
            flags: Int,
            seq: Long,
            ack: Long,
            payload: ByteArray,
            tcpOptions: ByteArray? = null
        ) {
            val optLen = tcpOptions?.size ?: 0
            val tcpHeaderLen = 20 + optLen
            val totalLen = 20 + tcpHeaderLen + payload.size
            val packet = ByteArray(totalLen)

            // IP Header
            packet[0] = 0x45.toByte() // IPv4, IHL 5
            packet[1] = 0x00
            setShort(packet, 2, totalLen)
            setShort(packet, 4, (Math.random() * 65535).toInt())
            packet[6] = 0x40.toByte() // Don't fragment
            packet[7] = 0x00
            packet[8] = 64.toByte() // TTL
            packet[9] = 6.toByte() // Protocol TCP
            setIp(packet, 12, dstIp)
            setIp(packet, 16, srcIp)
            setChecksum(packet, 10, calculateChecksum(packet, 0, 20))

            // TCP Header
            setShort(packet, 20, dstPort)
            setShort(packet, 22, srcPort)
            setUInt32(packet, 24, seq)
            setUInt32(packet, 28, ack)
            packet[32] = ((tcpHeaderLen / 4) shl 4).toByte() // Data offset (32-bit words)
            packet[33] = flags.toByte()
            setShort(packet, 34, 65535) // Window size
            setShort(packet, 36, 0) // Checksum placeholder
            setShort(packet, 38, 0) // Urgent pointer

            if (tcpOptions != null && tcpOptions.isNotEmpty()) {
                System.arraycopy(tcpOptions, 0, packet, 40, tcpOptions.size)
            }

            if (payload.isNotEmpty()) {
                System.arraycopy(payload, 0, packet, 20 + tcpHeaderLen, payload.size)
            }

            // Calculate TCP Pseudo-Header Checksum
            val tcpChecksum = calculateTcpChecksum(packet, 20, tcpHeaderLen + payload.size, dstIp, srcIp)
            setShort(packet, 36, tcpChecksum)

            synchronized(out) {
                try {
                    out.write(packet)
                    out.flush()
                } catch (_: Throwable) {}
            }
        }

        fun close() {
            if (isClosed.getAndSet(true)) return
            try { channel?.disconnect() } catch (_: Throwable) {}
            channel = null
            try { socket?.close() } catch (_: Throwable) {}
            socket = null
        }
    }

    // ---------------------------------------------------------------------------------------------
    // IP and TCP Checksum & Packet Serialization Helpers
    // ---------------------------------------------------------------------------------------------
    private fun getPort(packet: ByteArray, offset: Int): Int {
        return ((packet[offset].toInt() and 0xFF) shl 8) or (packet[offset + 1].toInt() and 0xFF)
    }

    private fun getUInt32(packet: ByteArray, offset: Int): Long {
        return ((packet[offset].toLong() and 0xFF) shl 24) or
                ((packet[offset + 1].toLong() and 0xFF) shl 16) or
                ((packet[offset + 2].toLong() and 0xFF) shl 8) or
                (packet[offset + 3].toLong() and 0xFF)
    }

    private fun setShort(packet: ByteArray, offset: Int, value: Int) {
        packet[offset] = ((value shr 8) and 0xFF).toByte()
        packet[offset + 1] = (value and 0xFF).toByte()
    }

    private fun setUInt32(packet: ByteArray, offset: Int, value: Long) {
        packet[offset] = ((value shr 24) and 0xFF).toByte()
        packet[offset + 1] = ((value shr 16) and 0xFF).toByte()
        packet[offset + 2] = ((value shr 8) and 0xFF).toByte()
        packet[offset + 3] = (value and 0xFF).toByte()
    }

    private fun setChecksum(packet: ByteArray, offset: Int, checksum: Int) {
        packet[offset] = ((checksum shr 8) and 0xFF).toByte()
        packet[offset + 1] = (checksum and 0xFF).toByte()
    }

    private fun getIpString(packet: ByteArray, offset: Int): String {
        return "${packet[offset].toInt() and 0xFF}.${packet[offset + 1].toInt() and 0xFF}.${packet[offset + 2].toInt() and 0xFF}.${packet[offset + 3].toInt() and 0xFF}"
    }

    private fun setIp(packet: ByteArray, offset: Int, ipStr: String) {
        val parts = ipStr.split(".")
        if (parts.size == 4) {
            for (i in 0..3) {
                packet[offset + i] = (parts[i].toIntOrNull() ?: 0).toByte()
            }
        }
    }

    private fun calculateChecksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0L
        var i = offset
        while (i < offset + length - 1) {
            val word = ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        if (i < offset + length) {
            sum += ((data[i].toInt() and 0xFF) shl 8)
        }
        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return (sum.inv() and 0xFFFF).toInt()
    }

    private fun calculateTcpChecksum(
        packet: ByteArray,
        tcpOffset: Int,
        tcpLength: Int,
        srcIp: String,
        dstIp: String
    ): Int {
        var sum = 0L
        // Pseudo header: Src IP (4 bytes)
        val srcParts = srcIp.split(".")
        sum += ((srcParts[0].toInt() and 0xFF) shl 8) or (srcParts[1].toInt() and 0xFF)
        sum += ((srcParts[2].toInt() and 0xFF) shl 8) or (srcParts[3].toInt() and 0xFF)

        // Pseudo header: Dst IP (4 bytes)
        val dstParts = dstIp.split(".")
        sum += ((dstParts[0].toInt() and 0xFF) shl 8) or (dstParts[1].toInt() and 0xFF)
        sum += ((dstParts[2].toInt() and 0xFF) shl 8) or (dstParts[3].toInt() and 0xFF)

        // Pseudo header: Zero + Protocol (6)
        sum += 6
        // Pseudo header: TCP Length
        sum += tcpLength

        // TCP header + data
        var i = tcpOffset
        while (i < tcpOffset + tcpLength - 1) {
            val word = ((packet[i].toInt() and 0xFF) shl 8) or (packet[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        if (i < tcpOffset + tcpLength) {
            sum += ((packet[i].toInt() and 0xFF) shl 8)
        }

        while (sum shr 16 > 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return (sum.inv() and 0xFFFF).toInt()
    }
}
