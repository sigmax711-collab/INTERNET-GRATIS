package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.service.tun2socks.LocalSocksProxyServer
import com.example.service.tun2socks.Tun2SocksBridge
import com.example.service.tun2socks.Tun2SocksEngine
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * LocalVPNService handles low-level TUN interface (tun0) and IP packet routing.
 * Extends android.net.VpnService to route network packets through the secure tunnel.
 * Incorpora un servicio de monitoreo en segundo plano continuo que detecta caídas
 * inesperadas de red o túnel y reconecta automáticamente sin intervención del usuario.
 */
open class LocalVPNService : VpnService(), Runnable {

    protected var vpnInterface: ParcelFileDescriptor? = null
    protected val isTunnelRunning = AtomicBoolean(false)
    private val isManualDisconnect = AtomicBoolean(false)
    private val isReconnecting = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)

    private var tun2SocksBridge: Tun2SocksBridge? = null
    private var socksProxyServer: LocalSocksProxyServer? = null
    private var sshTunnelManager: com.example.service.ssh.SshTunnelManager? = null

    // Parámetros guardados para reconexión automática en segundo plano
    private var savedServerName: String = "Personal CloudFront"
    private var savedServerHost: String = "recargas.personal.com.ar"
    private var savedServerPort: Int = 80
    private var savedCfHost: String? = null
    private var savedUsername: String = "danaelssh"
    private var savedPassword: String = "123456"
    private var savedPayload: String? = null

    private var monitorThread: Thread? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    companion object {
        const val TAG = "LocalVPNService"
        const val ACTION_CONNECT = "com.example.service.LocalVPNService.CONNECT"
        const val ACTION_DISCONNECT = "com.example.service.LocalVPNService.DISCONNECT"
        const val EXTRA_SERVER_NAME = "extra_server_name"
        const val EXTRA_SERVER_HOST = "extra_server_host"
        const val EXTRA_SERVER_PORT = "extra_server_port"
        const val EXTRA_CF_HOST = "extra_cf_host"
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_PASSWORD = "extra_password"
        const val EXTRA_PAYLOAD = "extra_payload"

        const val CHANNEL_ID = "internet_gratis_vpn_channel"
        const val NOTIFICATION_ID = 1001

        private const val VPN_ADDRESS = "10.10.0.1"
        private const val VPN_ROUTE = "0.0.0.0"
        private const val VPN_DNS_PRIMARY = "1.1.1.1"
        private const val VPN_DNS_SECONDARY = "8.8.8.8"
        private const val VPN_MTU = 1500

        const val SOCKS_LOCAL_HOST = "127.0.0.1"
        const val SOCKS_LOCAL_PORT = 1080

        var isRunning: Boolean = false
            protected set

        fun startVpn(
            context: Context,
            serverName: String = "CloudFront Personal",
            username: String = "danaelssh-user",
            password: String = "123456",
            host: String = "recargas.personal.com.ar",
            port: Int = 80,
            payload: String? = null,
            cfHost: String? = null
        ) {
            val intent = Intent(context, LocalVPNService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_SERVER_NAME, serverName)
                putExtra(EXTRA_SERVER_HOST, host)
                putExtra(EXTRA_SERVER_PORT, port)
                putExtra(EXTRA_CF_HOST, cfHost)
                putExtra(EXTRA_USERNAME, username)
                putExtra(EXTRA_PASSWORD, password)
                putExtra(EXTRA_PAYLOAD, payload)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopVpn(context: Context) {
            val intent = Intent(context, LocalVPNService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            isManualDisconnect.set(true)
            disconnectTunnel()
            return START_NOT_STICKY
        }

        isManualDisconnect.set(false)
        reconnectAttempts.set(0)

        val serverName = intent?.getStringExtra(EXTRA_SERVER_NAME) ?: "Personal CloudFront"
        val serverHost = intent?.getStringExtra(EXTRA_SERVER_HOST) ?: "recargas.personal.com.ar"
        val serverPort = intent?.getIntExtra(EXTRA_SERVER_PORT, 80) ?: 80
        val cfHost = intent?.getStringExtra(EXTRA_CF_HOST)
        val username = intent?.getStringExtra(EXTRA_USERNAME) ?: "danaelssh"
        val password = intent?.getStringExtra(EXTRA_PASSWORD) ?: "123456"
        val payload = intent?.getStringExtra(EXTRA_PAYLOAD)

        savedServerName = serverName
        savedServerHost = serverHost
        savedServerPort = serverPort
        savedCfHost = cfHost
        savedUsername = username
        savedPassword = password
        savedPayload = payload

        startForeground(NOTIFICATION_ID, buildNotification(serverName, username))
        startTunnel(serverName, serverHost, serverPort, cfHost, username, password, payload)

        return START_STICKY
    }

    /**
     * Configures the TUN interface (tun0) and launches Tun2Socks engine and SOCKS proxy.
     */
    private fun startTunnel(
        serverName: String,
        serverHost: String,
        serverPort: Int,
        cfHost: String?,
        username: String,
        password: String,
        payload: String? = null
    ) {
        if (isTunnelRunning.get()) {
            Log.d(TAG, "Tunnel is already running.")
            return
        }

        try {
            vpnInterface?.close()

            val builder = Builder()
                .setSession("DTunnel - $serverName")
                .addAddress(VPN_ADDRESS, 16)
                .addRoute(VPN_ROUTE, 0) // Enruta todo el tráfico de internet
                .addDnsServer(VPN_DNS_PRIMARY) // 1.1.1.1 Obligatorio para resolver dominios
                .addDnsServer(VPN_DNS_SECONDARY) // 8.8.8.8
                .setMtu(VPN_MTU)
                .setBlocking(true)

            try {
                // Exclude self application so local sockets, telemetry and local SOCKS bypass TUN loop
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not disallow own package: ${e.message}")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            vpnInterface = builder.establish()
            if (vpnInterface != null) {
                isTunnelRunning.set(true)
                isRunning = true
                Log.i(TAG, "TUN interface established on fd: ${vpnInterface?.fd} with routes 0.0.0.0/0 and DNS (1.1.1.1, 8.8.8.8)")

                registerNetworkMonitor()

                // 1. Inicia el SSH Tunnel Manager para la conexión real a CloudFront y VPS
                val ssh = com.example.service.ssh.SshTunnelManager(
                    vpnService = this,
                    serverHost = serverHost,
                    serverPort = serverPort,
                    cfHost = cfHost,
                    payload = payload,
                    username = username,
                    password = password,
                    onLog = { msg ->
                        Log.i(TAG, "[SSH] $msg")
                        com.example.viewmodel.VpnViewModel.postTunnelLog(msg)
                    }
                ).apply {
                    onConnectionDropped = {
                        if (isTunnelRunning.get() && !isManualDisconnect.get()) {
                            com.example.viewmodel.VpnViewModel.postTunnelLog("⚠️ [MONITOR EN SEGUNDO PLANO] Caída repentina de conexión detectada.")
                            com.example.viewmodel.VpnViewModel.postTunnelLog("🔄 [MONITOR EN SEGUNDO PLANO] Reconectando automáticamente sin intervención del usuario...")
                            triggerBackgroundReconnect()
                        }
                    }
                }
                sshTunnelManager = ssh

                // 2. Levanta el servidor SOCKS local en 127.0.0.1:1080 conectado al SSH Tunnel
                socksProxyServer = LocalSocksProxyServer(this, SOCKS_LOCAL_PORT, payload, ssh).apply {
                    start()
                }

                // 3. Inicia Tun2SocksBridge para conectar TUN -> SOCKS5 127.0.0.1:1080
                tun2SocksBridge = Tun2SocksBridge(
                    vpnService = this,
                    pfd = vpnInterface!!,
                    socksHost = SOCKS_LOCAL_HOST,
                    socksPort = SOCKS_LOCAL_PORT,
                    dnsServers = listOf(VPN_DNS_PRIMARY, VPN_DNS_SECONDARY),
                    vpnIp = VPN_ADDRESS,
                    vpnNetmask = "255.255.255.0",
                    vpnMtu = VPN_MTU
                ).apply {
                    start()
                }
                Log.i(TAG, "Tun2Socks bridge active: ${tun2SocksBridge?.getBridgeInfo()}")

                thread(name = "SshTunnelConnectWorker", isDaemon = true) {
                    val success = ssh.connect()
                    if (success) {
                        com.example.viewmodel.VpnViewModel.postTunnelLog("Verificando flujo de datos a internet...")
                        val hasInternet = ssh.verifyInternetConnectivity()
                        if (hasInternet) {
                            com.example.viewmodel.VpnViewModel.postTunnelLog("✔ ¡Internet verificado y activo con éxito!")
                        } else {
                            com.example.viewmodel.VpnViewModel.postTunnelLog("✔ Túnel SSH establecido. Canal de datos listo.")
                        }
                        com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.CONNECTED)
                        updateNotification("Conectado y seguro 🇦🇷", "Túnel activo: $savedServerName")
                        startBackgroundConnectionMonitor()
                    } else {
                        com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.DISCONNECTED)
                        disconnectTunnel()
                        stopSelf()
                    }
                }
            } else {
                Log.e(TAG, "Failed to establish TUN interface (returned null)")
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while setting up TUN interface", e)
            disconnectTunnel()
        }
    }

    private fun registerNetworkMonitor() {
        try {
            if (connectivityManager == null) {
                connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            }
            unregisterNetworkMonitor()
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onLost(network: Network) {
                    if (isTunnelRunning.get() && !isManualDisconnect.get()) {
                        Log.w(TAG, "[MONITOR] ⚠️ Red celular/WiFi perdida temporalmente.")
                        com.example.viewmodel.VpnViewModel.postTunnelLog("[MONITOR] ⚠️ Red celular/WiFi perdida temporalmente. Esperando señal...")
                        com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.RECONNECTING)
                        updateNotification("Reconectando automáticamente...", "Esperando señal de red móvil...")
                    }
                }

                override fun onAvailable(network: Network) {
                    if (isTunnelRunning.get() && !isManualDisconnect.get()) {
                        Log.i(TAG, "[MONITOR] 📶 Señal de red restablecida.")
                        val ssh = sshTunnelManager
                        if (ssh == null || !ssh.isConnected()) {
                            com.example.viewmodel.VpnViewModel.postTunnelLog("[MONITOR] 📶 Red disponible detectada. Iniciando reconexión automática...")
                            triggerBackgroundReconnect()
                        }
                    }
                }
            }
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo registrar NetworkCallback: ${e.message}")
        }
    }

    private fun unregisterNetworkMonitor() {
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        } catch (_: Exception) {}
        networkCallback = null
    }

    private fun triggerBackgroundReconnect() {
        if (!isTunnelRunning.get() || isManualDisconnect.get()) return
        if (isReconnecting.compareAndSet(false, true)) {
            thread(name = "AutoReconnectWorker", isDaemon = true) {
                performAutoReconnectLoop()
            }
        }
    }

    private fun performAutoReconnectLoop() {
        try {
            com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.RECONNECTING)
            updateNotification("Reconectando automáticamente...", "$savedServerName (Reintentando...)")

            var attempt = 1
            while (isTunnelRunning.get() && !isManualDisconnect.get()) {
                val attemptNum = reconnectAttempts.incrementAndGet()
                com.example.viewmodel.VpnViewModel.postTunnelLog("🔄 [MONITOR EN SEGUNDO PLANO] Intento #$attemptNum: reconectando túnel automáticamente sin intervención del usuario...")
                updateNotification("Reconectando automáticamente...", "Caída detectada. Restaurando túnel sin intervención (Intento #$attemptNum)...")

                // 1. Limpieza de socket anterior si quedó cerrado
                try {
                    sshTunnelManager?.disconnect()
                } catch (_: Exception) {}

                // 2. Crear nueva instancia de SshTunnelManager limpia
                val newSsh = com.example.service.ssh.SshTunnelManager(
                    vpnService = this,
                    serverHost = savedServerHost,
                    serverPort = savedServerPort,
                    cfHost = savedCfHost,
                    payload = savedPayload,
                    username = savedUsername,
                    password = savedPassword,
                    onLog = { msg ->
                        Log.i(TAG, "[SSH] $msg")
                        com.example.viewmodel.VpnViewModel.postTunnelLog(msg)
                    }
                ).apply {
                    onConnectionDropped = {
                        if (isTunnelRunning.get() && !isManualDisconnect.get()) {
                            com.example.viewmodel.VpnViewModel.postTunnelLog("⚠️ [MONITOR EN SEGUNDO PLANO] Caída repentina de conexión detectada.")
                            com.example.viewmodel.VpnViewModel.postTunnelLog("🔄 [MONITOR EN SEGUNDO PLANO] Reconectando automáticamente sin intervención del usuario...")
                            triggerBackgroundReconnect()
                        }
                    }
                }

                val success = newSsh.connect()
                if (success) {
                    sshTunnelManager = newSsh
                    socksProxyServer?.updateSshTunnelManager(newSsh)
                    val hasInternet = newSsh.verifyInternetConnectivity()
                    if (hasInternet) {
                        com.example.viewmodel.VpnViewModel.postTunnelLog("✔ [MONITOR EN SEGUNDO PLANO] ¡Internet restablecido y activo con éxito!")
                    } else {
                        com.example.viewmodel.VpnViewModel.postTunnelLog("✔ [MONITOR EN SEGUNDO PLANO] Túnel SSH reconectado con éxito.")
                    }
                    com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.CONNECTED)
                    updateNotification("Conectado y seguro 🇦🇷", "Túnel activo: $savedServerName")
                    reconnectAttempts.set(0)
                    isReconnecting.set(false)
                    return
                }

                // Backoff progresivo antes del siguiente reintento: 2s, 4s, 7s, 10s... hasta max 15s
                val backoffMs = when {
                    attempt == 1 -> 2000L
                    attempt == 2 -> 4000L
                    attempt == 3 -> 7000L
                    attempt == 4 -> 10000L
                    else -> 15000L
                }
                attempt++
                com.example.viewmodel.VpnViewModel.postTunnelLog("⏳ Próximo intento en ${backoffMs / 1000}s...")
                try {
                    Thread.sleep(backoffMs)
                } catch (_: InterruptedException) {
                    break
                }
            }
        } finally {
            isReconnecting.set(false)
        }
    }

    /**
     * Monitor en tiempo real continuo optimizado para bajo consumo de batería.
     * Supervisa la salud del túnel SSH y probe de internet. Si detecta una caída inesperada,
     * dispara la reconexión automática en segundo plano sin intervención del usuario.
     */
    private fun startBackgroundConnectionMonitor() {
        monitorThread?.interrupt()
        monitorThread = thread(name = "VpnBackgroundMonitor", isDaemon = true) {
            var consecutiveFails = 0
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager

            while (isTunnelRunning.get() && !isManualDisconnect.get() && !Thread.interrupted()) {
                val isInteractive = powerManager?.isInteractive ?: true
                val checkInterval = if (isInteractive) 8_000L else 20_000L

                try {
                    Thread.sleep(checkInterval)
                } catch (_: InterruptedException) {
                    break
                }

                if (!isTunnelRunning.get() || isManualDisconnect.get()) break
                if (isReconnecting.get()) continue

                val ssh = sshTunnelManager
                if (ssh == null || !ssh.isConnected()) {
                    Log.w(TAG, "[MONITOR EN SEGUNDO PLANO] SSH Tunnel desconectado.")
                    com.example.viewmodel.VpnViewModel.postTunnelLog("⚠️ [MONITOR EN SEGUNDO PLANO] Caída inesperada de socket detectada.")
                    com.example.viewmodel.VpnViewModel.postTunnelLog("🔄 [MONITOR EN SEGUNDO PLANO] Reconectando automáticamente sin intervención del usuario...")
                    triggerBackgroundReconnect()
                    continue
                } else {
                    val pingOk = ssh.verifyInternetConnectivity()
                    if (!pingOk) {
                        consecutiveFails++
                        Log.w(TAG, "[MONITOR EN SEGUNDO PLANO] Verificación de internet fallida ($consecutiveFails/2)")
                    } else {
                        consecutiveFails = 0
                    }
                }

                if (consecutiveFails >= 2) {
                    consecutiveFails = 0
                    com.example.viewmodel.VpnViewModel.postTunnelLog("⚠️ [MONITOR EN SEGUNDO PLANO] Caída de conexión inesperada detectada.")
                    com.example.viewmodel.VpnViewModel.postTunnelLog("🔄 [MONITOR EN SEGUNDO PLANO] Iniciando reconexión automática en segundo plano...")
                    triggerBackgroundReconnect()
                }
            }
        }
    }

    override fun run() {
        // Kept for Runnable interface compatibility
    }

    /**
     * Closes the tun0 interface and terminates Tun2Socks engine and background threads.
     */
    private fun disconnectTunnel() {
        isTunnelRunning.set(false)
        isRunning = false
        isManualDisconnect.set(true)
        unregisterNetworkMonitor()
        monitorThread?.interrupt()
        monitorThread = null
        try {
            sshTunnelManager?.disconnect()
            sshTunnelManager = null
            tun2SocksBridge?.stop()
            tun2SocksBridge = null
            socksProxyServer?.stop()
            socksProxyServer = null
            vpnInterface?.close()
            vpnInterface = null
            Log.i(TAG, "TUN interface and Tun2Socks bridge closed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN tunnel", e)
        } finally {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onRevoke() {
        Log.i(TAG, "VPN service revoked by Android system or settings")
        isManualDisconnect.set(true)
        com.example.viewmodel.VpnViewModel.postTunnelState(com.example.model.VpnState.DISCONNECTED)
        disconnectTunnel()
        super.onRevoke()
    }

    override fun onDestroy() {
        disconnectTunnel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Internet Gratis VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificación de estado del túnel VPN"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(serverName: String, username: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Personal CloudFront VPN 🇦🇷")
            .setContentText("Túnel activo: $serverName | $username")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(title: String, text: String) {
        try {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            val manager = getSystemService(NotificationManager::class.java)
            manager?.notify(NOTIFICATION_ID, notification)
        } catch (_: Exception) {}
    }
}
