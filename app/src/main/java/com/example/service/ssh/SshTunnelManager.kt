package com.example.service.ssh

import android.net.VpnService
import android.util.Log
import com.jcraft.jsch.ChannelDirectTCPIP
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SocketFactory
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages the real SSH tunnel over HTTP / WebSocket CloudFront CDN for zero-rated mobile data.
 *
 * Flow:
 * 1. Opens protected Socket to carrier zero-rated host (e.g. recargas.personal.com.ar:80).
 * 2. Injects HTTP payload with [instant_split] requesting WebSocket Upgrade to CloudFront.
 * 3. Awaits HTTP 101 Switching Protocols.
 * 4. Passes the upgraded socket into JSch to negotiate SSH over WebSocket.
 * 5. Authenticates with username and password.
 * 6. Serves TCP stream forwarding (direct-tcpip) and DNS-over-TCP through the VPS datacenter connection.
 */
class SshTunnelManager(
    private val vpnService: VpnService,
    private val serverHost: String,
    private val serverPort: Int,
    private val cfHost: String? = null,
    private val payload: String?,
    private val username: String,
    private val password: String,
    private val onLog: ((String) -> Unit)? = null
) {
    companion object {
        private const val TAG = "SshTunnelManager"
        var activeInstance: SshTunnelManager? = null
            private set

        // IPs de AWS CloudFront en Sudamérica y globales para bypass DNS cuando el chip no tiene saldo
        private val CLOUDFRONT_FALLBACK_IPS = listOf(
            "13.224.161.90",
            "13.224.161.27",
            "13.32.121.50",
            "18.65.229.41",
            "65.8.11.14",
            "108.138.64.20",
            "143.204.101.40",
            "54.230.228.10"
        )
    }

    private var rawSocket: Socket? = null
    private var sshSession: Session? = null
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)

    var onConnectionDropped: (() -> Unit)? = null

    fun notifyConnectionDropped() {
        if (isConnected.compareAndSet(true, false)) {
            Log.w(TAG, "Notifying connection dropped to background monitor")
            onConnectionDropped?.invoke()
        }
    }

    fun isConnected(): Boolean {
        val s = sshSession
        val sock = rawSocket
        return isConnected.get() && s != null && s.isConnected && sock != null && !sock.isClosed && sock.isConnected
    }
    fun isConnecting(): Boolean = isConnecting.get()

    data class AuthCandidate(val username: String, val password: String, val label: String)
    data class ConnectedTunnelSocket(val socket: Socket, val inputStream: InputStream)
    private data class HandshakeResult(val socket: Socket, val inputStream: InputStream, val status: String, val isSuccess: Boolean)

    /**
     * Resuelve los endpoints candidatos (IPs o nombres) para el destino CloudFront,
     * combinando DNS del sistema y fallback de IPs globales si el chip no tiene saldo.
     */
    private fun resolveCloudFrontEndpoints(targetCf: String): List<String> {
        val list = mutableListOf<String>()
        // 1. Intentar resolución DNS normal, priorizando IPv4
        try {
            val addrs = java.net.InetAddress.getAllByName(targetCf)
            for (a in addrs.filterIsInstance<java.net.Inet4Address>()) {
                val ip = a.hostAddress
                if (!ip.isNullOrBlank() && !list.contains(ip)) {
                    list.add(ip)
                }
            }
            for (a in addrs.filterIsInstance<java.net.Inet6Address>()) {
                val ip = a.hostAddress
                if (!ip.isNullOrBlank() && !list.contains(ip)) {
                    list.add(ip)
                }
            }
        } catch (_: Exception) {
            // Falla común cuando no hay saldo en datos móviles de Personal o Claro
        }

        // 2. Si no resolvió o para redundancia, agregar el nombre y las IPs fijas de Anycast IPv4
        if (!list.contains(targetCf)) {
            list.add(targetCf)
        }
        for (ip in CLOUDFRONT_FALLBACK_IPS) {
            if (!list.contains(ip)) {
                list.add(ip)
            }
        }
        return list
    }

    /**
     * Resuelve los endpoints candidatos (IPs o nombres) para el host de la operadora (Personal / Claro),
     * combinando DNS de la red activa celular (APN), DNS del sistema y lista de IPs conocidas
     * del portal de saldo cero para evitar fallos de UnknownHostException cuando el chip no tiene saldo.
     */
    private fun resolveCarrierHostEndpoints(host: String): List<String> {
        val list = mutableListOf<String>()

        // 1. Intentar resolver usando el DNS de la red celular activa (APN)
        try {
            val cm = vpnService.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNet = cm?.activeNetwork
            if (activeNet != null) {
                val addrs = activeNet.getAllByName(host)
                for (a in addrs.filterIsInstance<java.net.Inet4Address>()) {
                    val ip = a.hostAddress
                    if (!ip.isNullOrBlank() && !list.contains(ip)) list.add(ip)
                }
            }
        } catch (_: Exception) {}

        // 2. Intentar resolución estándar de Android
        try {
            val addrs = java.net.InetAddress.getAllByName(host)
            for (a in addrs.filterIsInstance<java.net.Inet4Address>()) {
                val ip = a.hostAddress
                if (!ip.isNullOrBlank() && !list.contains(ip)) list.add(ip)
            }
        } catch (_: Exception) {}

        // 3. Fallback de IPs conocidas del portal de la operadora para bypass total de DNS
        if (host.contains("personal.com.ar", ignoreCase = true)) {
            val personalIps = listOf(
                "18.67.0.51",
                "18.67.0.14",
                "18.67.0.42",
                "18.67.0.108",
                "18.65.229.41",
                "13.224.161.90",
                "13.224.161.27",
                "143.204.101.40",
                "181.94.4.37"
            )
            for (ip in personalIps) {
                if (!list.contains(ip)) list.add(ip)
            }
        } else if (host.contains("claro.com.ar", ignoreCase = true)) {
            val claroIps = listOf(
                "13.32.179.38",
                "13.32.179.18",
                "13.32.179.47",
                "13.32.179.21",
                "3.170.91.51",
                "3.170.91.100"
            )
            for (ip in claroIps) {
                if (!list.contains(ip)) list.add(ip)
            }
        }

        // 4. Agregar el hostname directamente por si el sistema lo resuelve en el connect
        if (!list.contains(host)) {
            list.add(0, host)
        }

        return list
    }

    /**
     * Establece el socket seguro hacia el servidor.
     * En Argentina sin saldo (Personal / Claro), las antenas bloquean todas las IPs externas
     * y únicamente permiten conexiones hacia los hosts de saldo cero (recargas.personal.com.ar o simple.claro.com.ar).
     * Por ello, el flujo con Payload inyecta el Upgrade a WebSocket / Payload sobre el host de la operadora
     * hacia el CDN de CloudFront que hace de proxy hacia la VPS.
     */
    private fun openConnectedSocket(): ConnectedTunnelSocket {
        val targetCf = when {
            !cfHost.isNullOrBlank() -> cfHost
            serverHost.contains("cloudfront.net") -> serverHost
            payload?.contains("d3axktnpdrjmnq.cloudfront.net") == true -> "d3axktnpdrjmnq.cloudfront.net"
            payload?.contains("d3cdy6viklzpq1.cloudfront.net") == true -> "d3cdy6viklzpq1.cloudfront.net"
            else -> "d2htdw8fj0jdwe.cloudfront.net"
        }

        // Determinar ruta HTTP/WS: extraer de payload si existe, o usar / por defecto según el panel
        val targetPath = when {
            payload?.contains("GET /speedwaypro2") == true -> "/speedwaypro2"
            payload?.contains("GET /.rar") == true -> "/.rar"
            payload?.contains("GET /sergio1") == true -> "/sergio1"
            else -> "/"
        }

        // CASO 1: CON PAYLOAD (Personal o Claro sin saldo)
        // Conexión exclusiva con Payload a través del host/IP de la operadora hacia CloudFront.
        // NUNCA conectar directo a CloudFront si hay payload, porque la red móvil bloquea CloudFront directo.
        if (!payload.isNullOrBlank()) {
            val carrierEndpoints = resolveCarrierHostEndpoints(serverHost)
            log("Iniciando conexión con Payload vía $serverHost:$serverPort...")

            for (endpoint in carrierEndpoints) {
                try {
                    val isIp = endpoint != serverHost
                    val targetDesc = if (isIp) "$serverHost ($endpoint:$serverPort)" else "$serverHost:$serverPort"
                    log("Conectando socket con Payload a $targetDesc...")

                    val s = Socket()
                    vpnService.protect(s)
                    s.tcpNoDelay = true
                    s.soTimeout = 8000
                    s.connect(InetSocketAddress(endpoint, serverPort), 5000)

                    log("Inyectando Payload en $targetDesc (Destino: $targetCf)...")
                    injectPayload(s, payload, serverHost, serverPort, targetCf)

                    val hs = performHandshake(s)
                    log("Respuesta servidor ($targetDesc): ${hs.status}")
                    if (hs.isSuccess) {
                        log("✔ Enlace con Payload establecido con éxito.")
                        return ConnectedTunnelSocket(hs.socket, hs.inputStream)
                    } else {
                        try { s.close() } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    log("Aviso conexión Payload ($endpoint): ${e.message ?: e.toString()}")
                }
            }

            // Alternativa con WebSocket simple sobre el host de la operadora antes de rendirse
            try {
                log("Probando alternativa WebSocket directa a $serverHost:$serverPort...")
                val s = Socket()
                vpnService.protect(s)
                s.tcpNoDelay = true
                s.soTimeout = 8000
                s.connect(InetSocketAddress(serverHost, serverPort), 5000)

                val req = StringBuilder()
                req.append("GET $targetPath HTTP/1.1\r\n")
                req.append("Host: ").append(targetCf).append("\r\n")
                req.append("X-Online-Host: ").append(serverHost).append("\r\n")
                req.append("X-Forward-Host: ").append(serverHost).append("\r\n")
                req.append("Upgrade: websocket\r\n")
                req.append("Connection: Upgrade\r\n\r\n")
                s.getOutputStream().write(req.toString().toByteArray(Charsets.ISO_8859_1))
                s.getOutputStream().flush()

                val hs = performHandshake(s)
                log("Respuesta alternativa: ${hs.status}")
                if (hs.isSuccess) {
                    log("✔ Enlace WebSocket alternativo establecido con éxito.")
                    return ConnectedTunnelSocket(hs.socket, hs.inputStream)
                } else {
                    try { s.close() } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                log("Aviso alternativa $serverHost: ${e.message}")
            }

            throw IllegalStateException("Error de servidor: No se pudo conectar con el Payload a través de $serverHost en el puerto $serverPort. Verifica que los datos móviles de la operadora estén activos.")
        }

        // CASO 2: SIN PAYLOAD (Conexión directa WebSocket a CloudFront para redes WiFi o servidores directos)
        if (!targetCf.isNullOrBlank()) {
            val cfEndpoints = resolveCloudFrontEndpoints(targetCf)
            log("Probando enlace WebSocket a CloudFront directo ($targetCf:$serverPort)...")

            for (endpoint in cfEndpoints.take(4)) {
                try {
                    log("Conectando a CloudFront ($endpoint:$serverPort)...")
                    val s = Socket()
                    vpnService.protect(s)
                    s.tcpNoDelay = true
                    s.soTimeout = 8000
                    s.connect(InetSocketAddress(endpoint, serverPort), 4000)

                    val req = StringBuilder()
                    req.append("GET $targetPath HTTP/1.1\r\n")
                    req.append("Host: ").append(targetCf).append("\r\n")
                    if (!serverHost.isNullOrBlank() && !serverHost.contains("cloudfront.net")) {
                        req.append("X-Online-Host: ").append(serverHost).append("\r\n")
                        req.append("X-Forward-Host: ").append(serverHost).append("\r\n")
                    }
                    req.append("Upgrade: websocket\r\n")
                    req.append("Connection: Upgrade\r\n\r\n")

                    s.getOutputStream().write(req.toString().toByteArray(Charsets.ISO_8859_1))
                    s.getOutputStream().flush()

                    val hs = performHandshake(s)
                    log("Respuesta CloudFront: ${hs.status}")
                    if (hs.isSuccess) {
                        log("✔ Enlace WebSocket CloudFront establecido con éxito.")
                        return ConnectedTunnelSocket(hs.socket, hs.inputStream)
                    } else {
                        try { s.close() } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    log("Aviso CloudFront ($endpoint): ${e.message}")
                }
            }

            // RUTA 3.5: Conexión WebSocket Seguro WSS (puerto 443 con TLS/SNI) solo si el servidor está configurado en 443
            if (serverPort == 443) {
                try {
                    log("Probando enlace WSS (TLS puerto 443) hacia $targetCf...")
                    val raw = Socket()
                    vpnService.protect(raw)
                    raw.tcpNoDelay = true
                    raw.soTimeout = 8000
                    raw.connect(InetSocketAddress(targetCf, 443), 4000)

                    val sslFactory = javax.net.ssl.HttpsURLConnection.getDefaultSSLSocketFactory()
                    val sslSocket = sslFactory.createSocket(raw, targetCf, 443, true) as javax.net.ssl.SSLSocket
                    sslSocket.startHandshake()

                    val req = StringBuilder()
                    req.append("GET $targetPath HTTP/1.1\r\n")
                    req.append("Host: ").append(targetCf).append("\r\n")
                    req.append("Upgrade: websocket\r\n")
                    req.append("Connection: Upgrade\r\n\r\n")

                    sslSocket.getOutputStream().write(req.toString().toByteArray(Charsets.ISO_8859_1))
                    sslSocket.getOutputStream().flush()

                    val hs = performHandshake(sslSocket)
                    log("Respuesta CloudFront WSS (443): ${hs.status}")
                    if (hs.isSuccess) {
                        log("✔ Enlace WSS CloudFront establecido con éxito.")
                        return ConnectedTunnelSocket(hs.socket, hs.inputStream)
                    } else {
                        try { sslSocket.close() } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    log("Aviso WSS (443): ${e.message}")
                }
            }
        }

        // CASO 3: Socket directo al host (solo válido si no es un portal web como Personal/Claro en puerto 80)
        val isWebPortal = serverHost.contains("personal.com.ar") || serverHost.contains("claro.com.ar") || serverPort == 80
        if (!isWebPortal) {
            try {
                log("Probando enlace TCP directo a $serverHost:$serverPort...")
                val s = Socket()
                vpnService.protect(s)
                s.tcpNoDelay = true
                s.soTimeout = 8000
                s.connect(InetSocketAddress(serverHost, serverPort), 5000)
                return ConnectedTunnelSocket(s, s.getInputStream())
            } catch (ex: Exception) {
                log("Aviso socket directo: ${ex.message}")
            }
        }

        throw IllegalStateException("Error de servidor: El canal WebSocket hacia el CDN no respondió en el puerto $serverPort. Verifica conexión móvil o host de saldo cero.")
    }

    /**
     * Connects the SSH tunnel synchronously on the caller's thread (should be run in background).
     */
    fun connect(): Boolean {
        if (isConnected.get()) return true
        if (!isConnecting.compareAndSet(false, true)) return false

        try {
            activeInstance = this
            log("Iniciando conexión SSH...")
            log("Destino principal: $serverHost:$serverPort" + if (!cfHost.isNullOrBlank()) " (CloudFront: $cfHost)" else "")

            val effectiveHost = cfHost ?: serverHost
            val isDanael = effectiveHost.contains("d2htdw8fj0jdwe", ignoreCase = true) ||
                    effectiveHost.contains("d3cdy6viklzpq1", ignoreCase = true) ||
                    effectiveHost.contains("danael", ignoreCase = true)

            val rawUser = username.trim().removePrefix("danaelssh-")
            val userLower = rawUser.lowercase()
            val primaryPass = password.trim().ifBlank { "123456" }

            // Construir candidatos de autenticación inteligentes (para resolver problemas de mayúsculas o prefijo de panel)
            val candidates = mutableListOf<AuthCandidate>()

            fun addCandidate(user: String, pass: String, label: String) {
                if (user.isNotBlank() && candidates.none { it.username == user && it.password == pass }) {
                    candidates.add(AuthCandidate(user, pass, label))
                }
            }

            // 1. Con prefijo tal cual lo escribió
            addCandidate("danaelssh-$rawUser", primaryPass, "Con prefijo 'danaelssh-'")
            // 2. Con prefijo en minúsculas (servidores Linux / scripts de Danael a menudo forzan lowercase)
            if (rawUser != userLower) {
                addCandidate("danaelssh-$userLower", primaryPass, "Con prefijo en minúsculas")
            }
            // 3. Sin prefijo (por si el usuario ya registró el nombre completo en la web)
            addCandidate(rawUser, primaryPass, "Sin prefijo")
            if (rawUser != userLower) {
                addCandidate(userLower, primaryPass, "Sin prefijo en minúsculas")
            }
            // 4. Si la clave era distinta de 123456, probar también con la clave por defecto 123456
            if (primaryPass != "123456") {
                addCandidate("danaelssh-$rawUser", "123456", "Clave fija 123456")
                if (rawUser != userLower) {
                    addCandidate("danaelssh-$userLower", "123456", "Clave fija 123456 (minúsculas)")
                }
                addCandidate(rawUser, "123456", "Sin prefijo con clave 123456")
            }

            if (candidates.isEmpty()) {
                addCandidate("danaelssh-user", "123456", "Usuario por defecto")
            }

            // Candidato adicional para pruebas directas estilo HTTP Custom (ej. servidor speedwaypro / teste7)
            if (rawUser == "teste7" || rawUser.isBlank() || rawUser == "test") {
                addCandidate("teste7", "Dawalker", "Cuenta teste7 Dawalker")
            }

            var lastAuthError: String? = null

            for (i in candidates.indices) {
                val candidate = candidates[i]
                var attemptConn: ConnectedTunnelSocket? = null
                var session: Session? = null

                try {
                    log("Conectando socket y canal seguro...")
                    attemptConn = openConnectedSocket()
                    rawSocket = attemptConn.socket

                    log("Autenticando usuario SSH '${candidate.username}'...")
                    val jsch = JSch()
                    val s = jsch.getSession(candidate.username, "127.0.0.1", 22)
                    s.setPassword(candidate.password)
                    s.setConfig("StrictHostKeyChecking", "no")
                    s.setConfig("PreferredAuthentications", "password,keyboard-interactive")
                    s.setConfig("Compression", "no")
                    // Aceleración de cifrado por hardware ARM64 para conexión ultrarrápida y mínimo consumo de batería
                    s.setConfig("CheckCiphers", "aes128-ctr,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes192-ctr,aes256-ctr")
                    s.setConfig("cipher.s2c", "aes128-ctr,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,arcfour128,arcfour,3des-cbc")
                    s.setConfig("cipher.c2s", "aes128-ctr,aes128-gcm@openssh.com,chacha20-poly1305@openssh.com,aes256-ctr,arcfour128,arcfour,3des-cbc")
                    s.timeout = 10000

                    // Keepalive inteligente: 30s mantiene vivas las tablas NAT 4G sin despertar la CPU continuamente
                    s.serverAliveInterval = 30000
                    s.serverAliveCountMax = 3

                    val targetSock = attemptConn.socket
                    try { 
                        targetSock.keepAlive = true
                        targetSock.tcpNoDelay = true
                        targetSock.receiveBufferSize = 65536
                        targetSock.sendBufferSize = 65536
                    } catch (_: Exception) {}
                    val targetIn = attemptConn.inputStream
                    s.setSocketFactory(object : SocketFactory {
                        override fun createSocket(host: String, port: Int): Socket = targetSock
                        override fun getInputStream(sock: Socket): InputStream = targetIn
                        override fun getOutputStream(sock: Socket): OutputStream = targetSock.getOutputStream()
                    })

                    s.connect(10000)
                    session = s
                    sshSession = session
                    isConnected.set(true)

                    log("✔ ¡Túnel SSH establecido con éxito como '${candidate.username}'!")
                    log("✔ Tráfico de internet enrutado y protegido a través de la VPS.")
                    return true
                } catch (e: Exception) {
                    val msg = e.message ?: e.toString()
                    val isAuthFail = msg.contains("Auth fail", ignoreCase = true) || 
                                     msg.contains("USERAUTH", ignoreCase = true)

                    try { session?.disconnect() } catch (_: Exception) {}
                    try { attemptConn?.socket?.close() } catch (_: Exception) {}

                    if (isAuthFail) {
                        lastAuthError = msg
                        if (i < candidates.size - 1) {
                            log("Aviso: Servidor rechazó '${candidate.username}'. Reintentando variante...")
                        }
                    } else {
                        // Error no relacionado con auth (ej. timeout o error de servidor)
                        log("❌ Error de servidor: $msg")
                        disconnect()
                        return false
                    }
                }
            }

            // Si llegó aquí es porque todas las variantes de autenticación fallaron
            log("❌ Error de usuario y contraseña: Auth fail (Credenciales no válidas)")
            log("💡 El servidor rechazó el usuario '$username'. Revisa tu usuario y contraseña.")
            log("👉 Si nunca creaste este usuario, entra en 'CREAR CUENTA' en la web danael.fun/create/cloudfront/ o en Telegram @danaelssh.")
            log("👉 Si ya lo creaste, revisa que la clave coincida o que no haya expirado (cuentas gratis duran 24-72h).")
            disconnect()
            return false
        } catch (e: Exception) {
            val msg = e.message ?: e.toString()
            val isAuth = msg.contains("Auth fail", ignoreCase = true) || msg.contains("USERAUTH", ignoreCase = true)
            if (isAuth) {
                log("❌ Error de usuario y contraseña: $msg")
            } else {
                log("❌ Error de servidor: $msg")
            }
            Log.e(TAG, "SSH connection error", e)
            disconnect()
            return false
        } finally {
            isConnecting.set(false)
        }
    }

    /**
     * Injects the DTunnel/HTTP Custom payload into the socket.
     * Handles [host], [port], [crlf], [lf], [instant_split], [split], and dynamic [cfHost].
     */
    private fun injectPayload(socket: Socket, rawPayload: String, host: String, port: Int, cfHost: String? = null) {
        val out = socket.getOutputStream()
        var formatted = rawPayload
            .replace("[host]", host)
            .replace("[port]", port.toString())
            .replace("[crlf]", "\r\n")
            .replace("[lf]", "\n")
            .replace("[cr]", "\r")
            .replace("[netData]", "HTTP/1.1")
            .replace("[protocol]", "HTTP/1.1")
            .replace("[ua]", "Dalvik/2.1.0 (Linux; U; Android 14; Mobile)")
            .replace("\\r\\n", "\r\n")
            .replace("\\n", "\n")

        if (!cfHost.isNullOrBlank()) {
            formatted = formatted.replace("[cf_host]", cfHost)
        }

        val rotateRegex = Regex("""\[rotate=([^\]]+)\]""")
        formatted = rotateRegex.replace(formatted) { match ->
            val list = match.groupValues[1].split(";").map { it.trim() }
            if (!cfHost.isNullOrBlank() && list.contains(cfHost)) cfHost else list.firstOrNull() ?: ""
        }

        val splitToken = when {
            formatted.contains("[instant_split]") -> "[instant_split]"
            formatted.contains("[split]") -> "[split]"
            else -> null
        }

        if (splitToken != null) {
            val parts = formatted.split(splitToken)
            for ((idx, part) in parts.withIndex()) {
                if (idx > 0 && splitToken == "[split]") {
                    try { Thread.sleep(120) } catch (_: Exception) {}
                }
                out.write(part.toByteArray(Charsets.ISO_8859_1))
                out.flush()
            }
        } else {
            out.write(formatted.toByteArray(Charsets.ISO_8859_1))
            out.flush()
        }
    }

    /**
     * Procesa la respuesta HTTP tras enviar el Upgrade a WebSocket / Payload.
     * Lee la respuesta y localiza el inicio del protocolo SSH ("SSH-") o el fin de cabeceras HTTP.
     * Remueve todas las cabeceras previas para que JSch reciba el flujo SSH binario limpio.
     */
    private fun performHandshake(s: Socket): HandshakeResult {
        s.soTimeout = 8000
        val inStream = s.getInputStream()
        val buffer = ByteArray(16384)
        var totalRead = 0
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < 8000 && totalRead < buffer.size) {
            val r = try {
                inStream.read(buffer, totalRead, buffer.size - totalRead)
            } catch (_: Exception) {
                break
            }
            if (r == -1) break
            totalRead += r

            val text = String(buffer, 0, totalRead, Charsets.ISO_8859_1)
            if (text.contains("SSH-") || (text.contains("101") && (text.contains("\r\n\r\n") || text.contains("\n\n")))) {
                break
            }
        }

        if (totalRead == 0) {
            return HandshakeResult(s, inStream, "Sin respuesta del servidor", false)
        }

        val text = String(buffer, 0, totalRead, Charsets.ISO_8859_1)
        val sshIdx = text.indexOf("SSH-")
        val isSuccess = sshIdx != -1 || text.contains("101") || text.contains("200")

        val streamStartIdx = if (sshIdx != -1) {
            sshIdx
        } else {
            val lastDoubleCrlf = text.lastIndexOf("\r\n\r\n")
            val lastDoubleLf = text.lastIndexOf("\n\n")
            when {
                lastDoubleCrlf != -1 -> lastDoubleCrlf + 4
                lastDoubleLf != -1 -> lastDoubleLf + 2
                else -> totalRead
            }
        }

        val combinedInput: InputStream = if (streamStartIdx < totalRead) {
            val leftoverLength = totalRead - streamStartIdx
            val leftoverBytes = ByteArray(leftoverLength)
            System.arraycopy(buffer, streamStartIdx, leftoverBytes, 0, leftoverLength)
            java.io.SequenceInputStream(
                java.io.ByteArrayInputStream(leftoverBytes),
                inStream
            )
        } else {
            inStream
        }

        val statusSummary = when {
            sshIdx != -1 -> "SSH-2.0 Banner Detectado"
            text.contains("101") -> "HTTP/1.1 101 Switching Protocols"
            else -> text.lines().firstOrNull { it.isNotBlank() } ?: "HTTP/1.1 200 OK"
        }

        return HandshakeResult(s, combinedInput, statusSummary, isSuccess)
    }

    /**
     * Opens a direct-tcpip channel through the SSH tunnel to targetHost:targetPort.
     */
    fun openChannel(targetHost: String, targetPort: Int): ChannelDirectTCPIP? {
        val session = sshSession ?: return null
        if (!session.isConnected) return null

        return try {
            val channel = session.openChannel("direct-tcpip") as ChannelDirectTCPIP
            channel.setHost(targetHost)
            channel.setPort(targetPort)
            channel.setOrgIPAddress("127.0.0.1")
            channel.setOrgPort(1080)
            channel
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open direct-tcpip channel for $targetHost:$targetPort", e)
            null
        }
    }

    /**
     * Verifica de forma real que haya flujo de datos a internet a través del túnel.
     * Envía una consulta DNS ligera por TCP a través del canal SSH.
     * Retorna true si internet responde, false si el canal está caído o mudo.
     */
    fun verifyInternetConnectivity(): Boolean {
        if (!isConnected()) return false
        return try {
            val dummyQuery = byteArrayOf(
                0x12, 0x34,
                0x01, 0x00,
                0x00, 0x01,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x06, 'g'.code.toByte(), 'o'.code.toByte(), 'o'.code.toByte(), 'g'.code.toByte(), 'l'.code.toByte(), 'e'.code.toByte(),
                0x03, 'c'.code.toByte(), 'o'.code.toByte(), 'm'.code.toByte(),
                0x00,
                0x00, 0x01,
                0x00, 0x01
            )
            val resp = resolveDnsOverTcp(dummyQuery)
            resp != null && resp.isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Resolves DNS queries via DNS-over-TCP through the SSH tunnel.
     * Tries 1.1.1.1, 8.8.8.8, 1.0.0.1, 127.0.0.1 as fallback.
     * This bypasses all carrier DNS blocking and zero-rated firewall restrictions!
     */
    fun resolveDnsOverTcp(dnsQuery: ByteArray): ByteArray? {
        val session = sshSession ?: return null
        if (!session.isConnected) return null

        val dnsTargets = listOf("1.1.1.1", "8.8.8.8", "1.0.0.1", "127.0.0.1", "127.0.0.53")
        for (dnsServer in dnsTargets) {
            var channel: ChannelDirectTCPIP? = null
            try {
                channel = session.openChannel("direct-tcpip") as ChannelDirectTCPIP
                channel.setHost(dnsServer)
                channel.setPort(53)
                val channelIn = channel.inputStream
                val channelOut = channel.outputStream
                channel.connect(2500)

                // DNS-over-TCP: 2-byte big-endian length prefix followed by query
                val queryLen = dnsQuery.size
                channelOut.write((queryLen shr 8) and 0xFF)
                channelOut.write(queryLen and 0xFF)
                channelOut.write(dnsQuery)
                channelOut.flush()

                // Read 2-byte big-endian response length
                val b1 = channelIn.read()
                val b2 = channelIn.read()
                if (b1 == -1 || b2 == -1) continue

                val respLen = (b1 shl 8) or (b2 and 0xFF)
                if (respLen <= 0 || respLen > 4096) continue

                val responseBytes = ByteArray(respLen)
                var bytesRead = 0
                while (bytesRead < respLen) {
                    val r = channelIn.read(responseBytes, bytesRead, respLen - bytesRead)
                    if (r == -1) break
                    bytesRead += r
                }

                if (bytesRead == respLen) {
                    return responseBytes
                }
            } catch (e: Exception) {
                Log.d(TAG, "DNS resolution over SSH ($dnsServer) failed: ${e.message}")
            } finally {
                try { channel?.disconnect() } catch (_: Throwable) {}
            }
        }
        return null
    }

    fun disconnect() {
        isConnected.set(false)
        try { sshSession?.disconnect() } catch (_: Throwable) {}
        sshSession = null
        try { rawSocket?.close() } catch (_: Throwable) {}
        rawSocket = null
        if (activeInstance == this) {
            activeInstance = null
        }
    }

    private fun log(msg: String) {
        Log.i(TAG, msg)
        onLog?.invoke(msg)
    }
}
