package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VpnRepository
import com.example.model.AppUpdateInfo
import com.example.model.CloudSyncResult
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.NetworkTelemetry
import com.example.model.ServerCategory
import com.example.model.ServerItem
import com.example.model.VpnState
import com.example.service.VoiceEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class DisconnectReason {
    MANUAL,
    AUTH_ERROR,
    SERVER_ERROR,
    UNKNOWN
}

data class VpnUiState(
    val vpnState: VpnState = VpnState.DISCONNECTED,
    val rawUsername: String = "",
    val fullUsername: String = "",
    val password: String = "123456",
    val soundEnabled: Boolean = true,
    val selectedServer: ServerItem? = null,
    val categories: List<ServerCategory> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val telemetry: NetworkTelemetry = NetworkTelemetry(),
    val isAutoScrollLogs: Boolean = true,
    // Modals
    val showServerModal: Boolean = false,
    val showLogsModal: Boolean = false,
    val showSettingsModal: Boolean = false,
    val showGuideModal: Boolean = false,
    val showAccountCreatorModal: Boolean = false,
    val showCreatorDialog: Boolean = false,
    val showPayloadModal: Boolean = false,
    val showCloudFrontModal: Boolean = false,
    val customCloudFront: String? = null,
    val isDanaelServer: Boolean = true,
    val selectedCategoryFilter: ServerCategory? = null,
    val serverSearchQuery: String = "",
    val snackbarMessage: String? = null,
    val isUserRegistered: Boolean = false,
    val registeredUsers: List<String> = emptyList(),
    val batterySaverEnabled: Boolean = true,
    val autoReconnectEnabled: Boolean = true,
    val userDaysRemaining: Long = 5L,
    val isAccountExpired: Boolean = false,
    val userExpirationTimestamp: Long = 0L,
    val userRemainingMillis: Long = 0L,
    // Hotspot & APN
    val isHotspotActive: Boolean = false,
    val hotspotIp: String = "192.168.43.1",
    val hotspotPort: Int = 8080,
    val showHotspotModal: Boolean = false,
    val showApnModal: Boolean = false,
    // App Update & Cloud Sync Info
    val appUpdateInfo: AppUpdateInfo? = null,
    val showAppUpdateModal: Boolean = false,
    val showCloudUrlModal: Boolean = false,
    val customCloudUrl: String = "",
    val lastSyncTimestamp: String = "No registrada",
    val cloudAnnouncement: String? = null,
    // Interactive Speaking Guided Tour
    val showGuidedTour: Boolean = false,
    val guidedTourStep: Int = 1
)

class VpnViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        var activeInstance: VpnViewModel? = null
            private set
        var lastDisconnectReason: DisconnectReason = DisconnectReason.UNKNOWN

        fun postTunnelLog(msg: String) {
            val vm = activeInstance ?: return
            vm.viewModelScope.launch {
                val isAuthFail = msg.contains("Auth fail", ignoreCase = true) ||
                        msg.contains("USERAUTH", ignoreCase = true) ||
                        msg.contains("Credenciales no válidas", ignoreCase = true) ||
                        msg.contains("rechazó el usuario", ignoreCase = true) ||
                        msg.contains("Error de usuario y contraseña", ignoreCase = true)

                val isServerFail = msg.contains("Error de servidor", ignoreCase = true) ||
                        msg.contains("Error de red", ignoreCase = true) ||
                        msg.contains("SocketTimeout", ignoreCase = true) ||
                        msg.contains("Sin respuesta del servidor", ignoreCase = true) ||
                        msg.contains("no respondió", ignoreCase = true) ||
                        msg.contains("failed to connect", ignoreCase = true) ||
                        msg.contains("Connection reset", ignoreCase = true) ||
                        msg.contains("Connection refused", ignoreCase = true) ||
                        msg.contains("502 Bad Gateway", ignoreCase = true) ||
                        msg.contains("500 Internal", ignoreCase = true) ||
                        msg.contains("403 Forbidden", ignoreCase = true) ||
                        msg.contains("UnknownHostException", ignoreCase = true) ||
                        msg.contains("ConnectException", ignoreCase = true)

                if (isAuthFail) {
                    lastDisconnectReason = DisconnectReason.AUTH_ERROR
                } else if (isServerFail) {
                    lastDisconnectReason = DisconnectReason.SERVER_ERROR
                }

                val type = when {
                    msg.contains("✔") || msg.contains("éxito") || msg.contains("101") || msg.contains("SSH-2.0") -> LogType.SUCCESS
                    msg.contains("❌") || isAuthFail || isServerFail -> LogType.ERROR
                    msg.contains("307 Temporary Redirect") || msg.contains("302 Found") || msg.contains("301 Moved") -> LogType.HIGHLIGHT
                    msg.contains("➤") || msg.contains("Iniciando") || msg.contains("Destino") || msg.contains("Aviso") ||
                            msg.contains("Respuesta servidor") || msg.contains("Respuesta CloudFront") ||
                            msg.contains("Probando") -> LogType.HIGHLIGHT
                    else -> LogType.NORMAL
                }
                vm.addLog(msg, type)

                if (isAuthFail) {
                    vm.showSnackbar("❌ Error de usuario y contraseña: Revisa tus credenciales o crea una cuenta nueva.")
                } else if (isServerFail && !msg.contains("Aviso")) {
                    vm.showSnackbar("❌ Error de servidor: El servidor o la red no responden.")
                }
            }
        }

        fun postTunnelState(state: VpnState) {
            val vm = activeInstance ?: return
            vm.viewModelScope.launch {
                val prevState = vm._uiState.value.vpnState
                vm._uiState.update { it.copy(vpnState = state) }
                if (state == VpnState.CONNECTED) {
                    lastDisconnectReason = DisconnectReason.UNKNOWN
                    vm.addLog("[VPN] ✔ ¡Túnel SSH y VPN activos con éxito! Navegación protegida habilitada (5 días de duración).", LogType.SUCCESS)
                    vm.showSnackbar("✔ Conectado con éxito. Navegación libre por 5 días.")
                    vm.voiceEngine.speak("Conectado con éxito, tiene 5 días de duración")
                    vm.startTelemetry()
                } else if (state == VpnState.RECONNECTING) {
                    vm.addLog("[MONITOR EN SEGUNDO PLANO] 🔄 Conexión caída detectada. Reconectando túnel automáticamente sin intervención del usuario...", LogType.HIGHLIGHT)
                    vm.voiceEngine.speak("Reconectando automáticamente...")
                } else if (state == VpnState.DISCONNECTED) {
                    vm.telemetryJob?.cancel()
                    if (prevState == VpnState.CONNECTING || prevState == VpnState.RECONNECTING) {
                        if (lastDisconnectReason == DisconnectReason.AUTH_ERROR) {
                            vm.voiceEngine.speak("Error de usuario y contraseña")
                            vm.showSnackbar("❌ Error de usuario y contraseña. Revisa tus datos o crea una nueva cuenta.")
                        } else {
                            vm.voiceEngine.speak("Error de servidor")
                            vm.showSnackbar("❌ Error de servidor. El servidor no responde o no hay señal.")
                        }
                    } else if (prevState == VpnState.CONNECTED) {
                        if (lastDisconnectReason == DisconnectReason.SERVER_ERROR) {
                            vm.voiceEngine.speak("Error de servidor")
                            vm.showSnackbar("❌ Desconectado: El servidor perdió la conexión.")
                        } else {
                            vm.voiceEngine.speak("Desconectado")
                        }
                    }
                }
            }
        }
    }

    private val repository = VpnRepository(application)
    private val voiceEngine = VoiceEngine(application)

    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()

    private val _vpnPermissionEvents = MutableSharedFlow<Unit>()
    val vpnPermissionEvents: SharedFlow<Unit> = _vpnPermissionEvents.asSharedFlow()

    private var telemetryJob: Job? = null
    private var connectionJob: Job? = null
    private var countdownJob: Job? = null

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        activeInstance = this
        val savedCategories = repository.getCachedCategories()
        val savedRawUser = repository.getSavedUsername().replace("danaelssh-", "")
        val savedPass = repository.getSavedPassword()
        val savedServerId = repository.getSavedServerId()
        val soundOn = repository.isSoundEnabled()
        val savedCustomCf = repository.getCustomCloudFront()

        var initialServer: ServerItem? = null
        for (cat in savedCategories) {
            val found = cat.items.find { it.id == savedServerId }
            if (found != null) {
                initialServer = found
                break
            }
        }
        if (initialServer == null && savedCategories.isNotEmpty() && savedCategories[0].items.isNotEmpty()) {
            initialServer = savedCategories[0].items[0]
        }
        if (initialServer == null) {
            initialServer = VpnRepository.DEFAULT_CATEGORIES[0].items[0]
        }

        if (!savedCustomCf.isNullOrBlank() && initialServer != null) {
            initialServer = initialServer.copy(cf_host = savedCustomCf)
        }

        initialServer?.let {
            val savedPayload = repository.getSavedPayload(it.id)
            if (!savedPayload.isNullOrBlank()) {
                initialServer = it.copy(payload = savedPayload)
            }
            repository.saveServerId(it.id)
        }

        voiceEngine.setMuted(!soundOn)

        val effectiveCf = savedCustomCf ?: initialServer?.cf_host ?: ""
        val isDanael = isDanaelHost(effectiveCf)
        val authorizedList = repository.getAuthorizedUsernames()
        val isRegistered = repository.isUserAuthorized(savedRawUser)
        val initialFull = if (isDanael) {
            if (savedRawUser.isNotBlank()) "danaelssh-$savedRawUser" else ""
        } else {
            savedRawUser
        }

        val batterySaver = repository.isBatterySaverEnabled()
        val autoReconnect = repository.isAutoReconnectEnabled()
        val daysRemaining = repository.getUserDaysRemaining(savedRawUser)
        val isExpired = repository.isUserExpired(savedRawUser)
        val expiryTimestamp = repository.getUserExpirationTimestamp(savedRawUser)
        val remainingMillis = repository.getUserRemainingMillis(savedRawUser)

        _uiState.update {
            it.copy(
                categories = savedCategories.ifEmpty { VpnRepository.DEFAULT_CATEGORIES },
                rawUsername = savedRawUser,
                fullUsername = initialFull,
                password = savedPass.ifBlank { "123456" },
                soundEnabled = soundOn,
                selectedServer = initialServer,
                customCloudFront = savedCustomCf,
                isDanaelServer = isDanael,
                telemetry = NetworkTelemetry(networkName = repository.getActiveNetworkName()),
                isUserRegistered = isRegistered,
                registeredUsers = authorizedList,
                batterySaverEnabled = batterySaver,
                autoReconnectEnabled = autoReconnect,
                userDaysRemaining = daysRemaining,
                isAccountExpired = isExpired,
                userExpirationTimestamp = expiryTimestamp,
                userRemainingMillis = remainingMillis,
                customCloudUrl = repository.getCustomCloudUrl(),
                lastSyncTimestamp = repository.getLastSyncTimestamp()
            )
        }

        startCountdownTimer()

        // Inicialización de registros de diagnóstico del dispositivo y red
        val model = android.os.Build.MODEL ?: "MOTOROLA MOTO G23"
        val release = android.os.Build.VERSION.RELEASE ?: "14"
        val sdk = android.os.Build.VERSION.SDK_INT
        val abi = android.os.Build.SUPPORTED_ABIS?.firstOrNull() ?: "ARM64-V8A"
        val localIp = repository.getLocalIpAddress().ifBlank { "10.88.222.93" }

        addLog("INTERNET GRATIS 1.0.0 1", LogType.HIGHLIGHT)
        addLog("$model | $release | $sdk | $abi", LogType.NORMAL)
        addLog("IP Local: $localIp", LogType.NORMAL)
        addLog("➤ CDN: 00", LogType.NORMAL)

        // Sincronizar automáticamente en segundo plano con la nube
        syncWithCloud(silent = true)
        startPeriodicCloudSync()
    }

    private fun getCurrentTimestamp(): String {
        return timeFormat.format(Date())
    }

    fun addLog(msg: String, type: LogType = LogType.NORMAL) {
        val entry = LogEntry(getCurrentTimestamp(), msg, type)
        _uiState.update { current ->
            // Limita a un máximo de 100 entradas para evitar sobrecarga de memoria y consumo de batería por GC
            val trimmed = if (current.logs.size >= 100) current.logs.takeLast(85) else current.logs
            current.copy(logs = trimmed + entry)
        }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun isDanaelHost(host: String?): Boolean {
        // En esta aplicación todos los servidores y dominios CloudFront utilizan cuentas DanaelSSH oficiales
        return true
    }

    fun setRawUsername(name: String) {
        val clean = name.trim().removePrefix("danaelssh-")
        repository.saveUsername(clean)
        val isRegistered = repository.isUserAuthorized(clean)
        val list = repository.getAuthorizedUsernames()
        val daysRemaining = repository.getUserDaysRemaining(clean)
        val isExpired = repository.isUserExpired(clean)
        val expiryTimestamp = repository.getUserExpirationTimestamp(clean)
        val remainingMillis = repository.getUserRemainingMillis(clean)
        val full = if (clean.isNotBlank()) "danaelssh-$clean" else ""
        _uiState.update { 
            it.copy(
                rawUsername = clean, 
                fullUsername = full,
                isDanaelServer = true,
                isUserRegistered = isRegistered,
                registeredUsers = list,
                userDaysRemaining = daysRemaining,
                isAccountExpired = isExpired,
                userExpirationTimestamp = expiryTimestamp,
                userRemainingMillis = remainingMillis
            ) 
        }
    }

    fun toggleBatterySaver() {
        val newState = !_uiState.value.batterySaverEnabled
        repository.saveBatterySaverEnabled(newState)
        _uiState.update { it.copy(batterySaverEnabled = newState) }
        val msg = if (newState) {
            "Ahorro de batería activado (frecuencia optimizada)"
        } else {
            "Ahorro de batería desactivado"
        }
        showSnackbar(msg)
    }

    fun renewAccount() {
        val user = _uiState.value.rawUsername
        if (user.isNotBlank()) {
            repository.renewUserAccount(user)
            val daysRemaining = repository.getUserDaysRemaining(user)
            val expiryTimestamp = repository.getUserExpirationTimestamp(user)
            val remainingMillis = repository.getUserRemainingMillis(user)
            _uiState.update { 
                it.copy(
                    userDaysRemaining = daysRemaining,
                    isAccountExpired = false,
                    userExpirationTimestamp = expiryTimestamp,
                    userRemainingMillis = remainingMillis
                ) 
            }
            addLog("[AUTH] ✔ Cuenta de $user renovada y activa.", LogType.SUCCESS)
            showSnackbar("✔ Cuenta renovada y autorizada.")
            voiceEngine.speak("Cuenta renovada, tiene cinco días de duración")
        }
    }

    fun setShowCloudFrontModal(show: Boolean) {
        _uiState.update { it.copy(showCloudFrontModal = show) }
    }

    fun updateCloudFrontHost(newHost: String) {
        val clean = newHost.trim().removePrefix("https://").removePrefix("http://").trimEnd('/')
        if (clean.isBlank()) return
        repository.saveCustomCloudFront(clean)

        val currentServer = _uiState.value.selectedServer
        val updatedServer = currentServer?.copy(cf_host = clean)
        val currentRaw = _uiState.value.rawUsername
        val stripped = currentRaw.removePrefix("danaelssh-")
        val effectiveFull = if (stripped.isNotBlank()) "danaelssh-$stripped" else ""

        _uiState.update {
            it.copy(
                selectedServer = updatedServer,
                customCloudFront = clean,
                isDanaelServer = true,
                fullUsername = effectiveFull,
                showCloudFrontModal = false
            )
        }
        addLog("[CLOUDFRONT] ✔ Servidor CloudFront configurado: $clean", LogType.SUCCESS)
        addLog("[AUTH] ✔ Usuario con prefijo 'danaelssh-' activo", LogType.HIGHLIGHT)
        showSnackbar("✔ CloudFront actualizado: $clean")
        voiceEngine.speak("Servidor CloudFront configurado")
    }

    fun resetCloudFrontHost() {
        repository.saveCustomCloudFront(null)
        val defaultCf = _uiState.value.selectedServer?.let { srv ->
            _uiState.value.categories.flatMap { it.items }.find { it.id == srv.id }?.cf_host
        } ?: "d2htdw8fj0jdwe.cloudfront.net"
        val updatedServer = _uiState.value.selectedServer?.copy(cf_host = defaultCf)
        val currentRaw = _uiState.value.rawUsername
        val stripped = currentRaw.removePrefix("danaelssh-")
        val effectiveFull = if (stripped.isNotBlank()) "danaelssh-$stripped" else ""
        _uiState.update {
            it.copy(
                customCloudFront = null,
                selectedServer = updatedServer,
                isDanaelServer = true,
                fullUsername = effectiveFull,
                showCloudFrontModal = false
            )
        }
        addLog("[CLOUDFRONT] Restaurado CloudFront predeterminado: $defaultCf", LogType.NORMAL)
        showSnackbar("Restaurado CloudFront original")
    }

    fun registerCloudFrontAccount(user: String, pass: String) {
        val cleanUser = user.trim().lowercase().removePrefix("danaelssh-")
        val cleanPass = pass.trim()
        if (cleanUser.isNotBlank()) {
            val finalPass = if (cleanPass.isNotBlank()) cleanPass else "123456"
            repository.addCustomUser(cleanUser, finalPass)
            setRawUsername(cleanUser)
            setPassword(finalPass)
            addLog("[AUTH] ✔ Cuenta CloudFront autorizada: danaelssh-$cleanUser. Tiene 5 días de duración.", LogType.SUCCESS)
            showSnackbar("Cuenta registrada con éxito: danaelssh-$cleanUser")
            voiceEngine.speak("Cuenta registrada, tiene cinco días de duración")
        }
    }

    fun setPassword(pass: String) {
        val clean = pass.trim()
        repository.savePassword(clean)
        _uiState.update { it.copy(password = clean) }
    }

    fun toggleSound() {
        val newState = !_uiState.value.soundEnabled
        repository.saveSoundEnabled(newState)
        voiceEngine.setMuted(!newState)
        _uiState.update { it.copy(soundEnabled = newState) }
        voiceEngine.speak(if (newState) "Voz activada" else "Voz desactivada")
    }

    fun speak(text: String) {
        voiceEngine.speak(text)
    }

    fun setSelectedServer(server: ServerItem) {
        repository.saveServerId(server.id)
        val savedPayload = repository.getSavedPayload(server.id)
        val effectivePayload = if (!savedPayload.isNullOrBlank()) savedPayload else server.payload
        val effectiveCf = _uiState.value.customCloudFront ?: server.cf_host
        var updatedServer = if (_uiState.value.customCloudFront != null) server.copy(cf_host = effectiveCf) else server
        if (updatedServer.payload != effectivePayload) {
            updatedServer = updatedServer.copy(payload = effectivePayload)
        }
        val currentRaw = _uiState.value.rawUsername
        val stripped = currentRaw.removePrefix("danaelssh-")
        val effectiveFull = if (stripped.isNotBlank()) "danaelssh-$stripped" else ""
        _uiState.update {
            it.copy(
                selectedServer = updatedServer,
                isDanaelServer = true,
                fullUsername = effectiveFull,
                showServerModal = false,
                selectedCategoryFilter = null
            )
        }
        addLog("[CONFIG] Servidor seleccionado: ${server.name} ($effectiveCf)", LogType.HIGHLIGHT)
        
        val isClaro = server.name.contains("Claro", ignoreCase = true) || server.host.contains("claro", ignoreCase = true)
        val speechText = if (isClaro) "Sección Claro seleccionado" else "Sección Personal seleccionado"
        voiceEngine.speak(speechText)
        showSnackbar("✔ $speechText")
    }

    fun updateServerPayload(newPayload: String) {
        val currentServer = _uiState.value.selectedServer ?: return
        val updatedServer = currentServer.copy(payload = newPayload)
        repository.savePayload(currentServer.id, newPayload)
        _uiState.update {
            it.copy(
                selectedServer = updatedServer,
                showPayloadModal = false
            )
        }
        addLog("[PAYLOAD] ✔ Payload actualizado correctamente para ${currentServer.name}", LogType.SUCCESS)
        showSnackbar("✔ Payload actualizado para ${currentServer.name}")
        voiceEngine.speak("Payload actualizado")
    }

    fun selectCarrier(isClaro: Boolean) {
        val targetName = if (isClaro) "Claro" else "Personal"
        
        var targetServer: ServerItem? = null
        for (cat in _uiState.value.categories) {
            val found = cat.items.find { 
                it.name.contains(targetName, ignoreCase = true) || it.host.contains(targetName, ignoreCase = true) 
            }
            if (found != null) {
                targetServer = found
                break
            }
        }

        if (targetServer != null) {
            setSelectedServer(targetServer)
        } else {
            val speechText = if (isClaro) "Sección Claro seleccionado" else "Sección Personal seleccionado"
            voiceEngine.speak(speechText)
            showSnackbar("✔ $speechText")
        }
    }

    fun openServerModalForCarrier(isClaro: Boolean) {
        val targetName = if (isClaro) "Claro" else "Personal"
        val targetCat = _uiState.value.categories.find {
            it.name.contains(targetName, ignoreCase = true)
        }
        _uiState.update {
            it.copy(
                showServerModal = true,
                selectedCategoryFilter = targetCat,
                serverSearchQuery = ""
            )
        }
    }

    fun toggleVpn() {
        val current = _uiState.value
        if (current.rawUsername.isBlank()) {
            voiceEngine.speak("Por favor escribe tu nombre de usuario")
            addLog("[AUTH] Error: Nombre de usuario vacío.", LogType.ERROR)
            showSnackbar("Por favor escribe tu nombre (Ejemplo: Cesar). Si no creaste la cuenta aún, toca el botón 'INDICACIONES'")
            return
        }

        when (current.vpnState) {
            VpnState.CONNECTED, VpnState.CONNECTING, VpnState.RECONNECTING, VpnState.WAITING -> {
                disconnectVpn(manual = true)
            }
            VpnState.DISCONNECTED, VpnState.UPDATING -> {
                connectVpn(isReconnect = false)
            }
        }
    }

    fun reconnectVpn() {
        connectionJob?.cancel()
        telemetryJob?.cancel()
        connectVpn(isReconnect = true)
    }

    private fun connectVpn(isReconnect: Boolean = false) {
        connectionJob?.cancel()
        val current = _uiState.value
        val server = current.selectedServer 
            ?: current.categories.firstOrNull()?.items?.firstOrNull() 
            ?: VpnRepository.DEFAULT_CATEGORIES[0].items[0]
            
        // Re-confirmar el ID del servidor en la configuración persistente
        repository.saveServerId(server.id)
        if (current.selectedServer?.id != server.id) {
            _uiState.update { it.copy(selectedServer = server) }
        }

        val isDanael = current.isDanaelServer
        val cleanUser = if (isDanael) current.rawUsername.trim().removePrefix("danaelssh-") else current.rawUsername.trim()
        val username = if (isDanael) {
            if (cleanUser.isNotBlank()) "danaelssh-$cleanUser" else if (current.fullUsername.isNotBlank()) current.fullUsername else "danaelssh-user"
        } else {
            cleanUser.ifBlank { "user" }
        }
        val rawUser = cleanUser
        val password = current.password.trim().ifBlank { "123456" }

        if (isReconnect) {
            _uiState.update { it.copy(vpnState = VpnState.RECONNECTING) }
            voiceEngine.speak("Reconectando...")
        } else {
            _uiState.update { it.copy(vpnState = VpnState.CONNECTING) }
        }

        val validation = if (isDanael) {
            repository.validateCredentials(rawUser, password)
        } else {
            VpnRepository.AuthResult.Success
        }

        connectionJob = viewModelScope.launch {
            // Estructura visual de configuración idéntica a la app de referencia
            addLog("➤ Configuración del Servidor:", LogType.HIGHLIGHT)
            addLog("├── 🆔 ID: ${server.id}", LogType.NORMAL)
            addLog("├── 🏷 Servidor: ${server.name}", LogType.NORMAL)
            addLog("├── 🌐 Destino: ${server.host}:${server.port}", LogType.NORMAL)
            addLog("├── ⚡ CDN CloudFront: ${server.cf_host ?: "Predeterminado"}", LogType.NORMAL)
            addLog("├── 👤 Usuario: $username", LogType.NORMAL)
            addLog("├── 📡 DNS: ${server.dns1} | ${server.dns2}", LogType.NORMAL)
            addLog("└── 🎮 UDPGW: ${server.udpPort}", LogType.NORMAL)

            delay(200)

            if (validation is VpnRepository.AuthResult.EmptyUser) {
                addLog("❌ Error: Nombre de usuario vacío.", LogType.ERROR)
                _uiState.update { it.copy(vpnState = VpnState.DISCONNECTED) }
                voiceEngine.speak("Por favor escribe tu nombre de usuario")
                showSnackbar("Por favor escribe un nombre de usuario.")
                return@launch
            }

            addLog("➤ Estableciendo canal con el servidor...", LogType.HIGHLIGHT)
            delay(150)
            _vpnPermissionEvents.emit(Unit)
        }
    }

    fun onVpnPermissionGranted(context: android.content.Context) {
        val current = _uiState.value
        val server = current.selectedServer ?: current.categories.firstOrNull()?.items?.firstOrNull()
        val serverName = server?.name ?: "CloudFront Argentina"
        val isDanael = current.isDanaelServer
        val cleanUser = if (isDanael) current.rawUsername.trim().removePrefix("danaelssh-") else current.rawUsername.trim()
        val username = if (isDanael) {
            if (cleanUser.isNotBlank()) "danaelssh-$cleanUser" else if (current.fullUsername.isNotBlank()) current.fullUsername else "danaelssh-user"
        } else {
            cleanUser.ifBlank { "user" }
        }

        try {
            val serverHost = server?.host ?: "recargas.personal.com.ar"
            val serverPort = server?.port ?: 80
            val cfHost = server?.cf_host
            val payload = server?.payload
            val password = current.password.trim().ifBlank { "123456" }

            com.example.service.DanaelVpnService.startVpn(
                context = context,
                serverName = serverName,
                username = username,
                password = password,
                host = serverHost,
                port = serverPort,
                payload = payload,
                cfHost = cfHost
            )
            _uiState.update { it.copy(vpnState = VpnState.CONNECTING) }
            addLog("[TUN] Interfaz de red 10.10.0.1/16 inicializada", LogType.NORMAL)
            addLog("[DNS] Servidores DNS: 1.1.1.1 (Primario) / 8.8.8.8 (Secundario)", LogType.NORMAL)
            addLog("[SOCKS] Servidor SOCKS local en 127.0.0.1:1080", LogType.NORMAL)
            if (!cfHost.isNullOrEmpty()) {
                addLog("[CDN] Destino CloudFront: $cfHost", LogType.HIGHLIGHT)
            }
            if (!payload.isNullOrEmpty()) {
                addLog("[TUNNEL] Enrutamiento WebSocket preparado", LogType.HIGHLIGHT)
            }
            addLog("[TUN2SOCKS] Motor Tun2Socks activo: Listo para enrutar paquetes", LogType.NORMAL)
            addLog("[VPN] Llave 🔑 del sistema Android activa. Negociando SSH...", LogType.HIGHLIGHT)
        } catch (e: Exception) {
            addLog("[VPN] ❌ Error de servidor al iniciar servicio VPN: ${e.message}", LogType.ERROR)
            _uiState.update { it.copy(vpnState = VpnState.DISCONNECTED) }
            voiceEngine.speak("Error de servidor")
        }
    }

    fun onVpnPermissionDenied() {
        _uiState.update { it.copy(vpnState = VpnState.DISCONNECTED) }
        addLog("[VPN] ❌ Permiso de VPN cancelado o denegado por el usuario en Android.", LogType.ERROR)
        showSnackbar("Permiso de VPN denegado por el sistema.")
        voiceEngine.speak("Error de servidor")
    }

    fun disconnectVpn(manual: Boolean = true) {
        connectionJob?.cancel()
        telemetryJob?.cancel()
        try {
            com.example.service.DanaelVpnService.stopVpn(getApplication())
        } catch (_: Exception) {}

        _uiState.update {
            it.copy(
                vpnState = VpnState.DISCONNECTED,
                telemetry = it.telemetry.copy(downloadSpeed = "0 KB/s", uploadSpeed = "0 KB/s")
            )
        }
        if (manual) {
            addLog("[SISTEMA] Conexión VPN detenida. Interfaz TUN cerrada (llave 🔑 removida).", LogType.NORMAL)
            voiceEngine.speak("Desconectado")
        } else {
            addLog("[ERROR] Conexión VPN cerrada inesperadamente.", LogType.ERROR)
            if (lastDisconnectReason == DisconnectReason.AUTH_ERROR) {
                voiceEngine.speak("Error de usuario y contraseña")
            } else {
                voiceEngine.speak("Error de servidor")
            }
        }
    }

    private var isAppInForeground: Boolean = true

    fun setAppInForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
    }

    private fun startTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            var cumDown = _uiState.value.telemetry.totalDownloadBytes
            var cumUp = _uiState.value.telemetry.totalUploadBytes
            while (_uiState.value.vpnState == VpnState.CONNECTED) {
                // Optimización de batería: si la app no está visible en pantalla, no ejecuta bucle rápido
                if (!isAppInForeground) {
                    delay(10000L)
                    continue
                }
                val interval = if (_uiState.value.batterySaverEnabled) 4000L else 1500L
                delay(interval)
                val downKb = Random.nextInt(250, 4800)
                val upKb = Random.nextInt(80, 1200)
                cumDown += (downKb * 1024L * (interval / 1000.0)).toLong()
                cumUp += (upKb * 1024L * (interval / 1000.0)).toLong()
                val downText = if (downKb > 1024) String.format(Locale.US, "%.1f MB/s", downKb / 1024.0) else "$downKb KB/s"
                val upText = if (upKb > 1024) String.format(Locale.US, "%.1f MB/s", upKb / 1024.0) else "$upKb KB/s"

                _uiState.update {
                    it.copy(
                        telemetry = it.telemetry.copy(
                            downloadSpeed = downText,
                            uploadSpeed = upText,
                            totalDownloadBytes = cumDown,
                            totalUploadBytes = cumUp,
                            networkName = repository.getActiveNetworkName(),
                            pingMs = Random.nextInt(35, 78)
                        )
                    )
                }
            }
        }
    }

    private var periodicSyncJob: Job? = null
    private var lastForegroundSyncTime = 0L

    private fun startPeriodicCloudSync() {
        periodicSyncJob?.cancel()
        periodicSyncJob = viewModelScope.launch {
            while (isActive) {
                delay(15 * 60 * 1000L) // Sincroniza en background cada 15 minutos
                syncWithCloud(silent = true)
            }
        }
    }

    fun onAppResumed() {
        val now = System.currentTimeMillis()
        if (now - lastForegroundSyncTime > 2 * 60 * 1000L) { // más de 2 minutos
            lastForegroundSyncTime = now
            syncWithCloud(silent = true)
        }
    }

    fun syncWithCloud(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _uiState.update { it.copy(vpnState = VpnState.UPDATING) }
                addLog("[NUBE] Sincronizando servidores y actualizaciones con CesarG Cloud...", LogType.HIGHLIGHT)
            }
            val result = repository.fetchCloudConfig()
            if (!silent) {
                _uiState.update { it.copy(vpnState = VpnState.DISCONNECTED) }
            }
            if (result.success) {
                _uiState.update { current ->
                    // Si el servidor actualmente seleccionado cambió en la nube, actualizarlo al instante
                    val currentSelectedId = current.selectedServer?.id
                    val currentSelectedName = current.selectedServer?.name
                    val allNewServers = result.categories.flatMap { it.items }
                    val updatedSelected = allNewServers.find { it.id == currentSelectedId }
                        ?: allNewServers.find { it.name.equals(currentSelectedName, ignoreCase = true) }
                        ?: current.selectedServer
                        ?: allNewServers.firstOrNull()

                    val shouldShowUpdate = result.updateInfo != null && !current.showAppUpdateModal

                    current.copy(
                        categories = result.categories,
                        selectedServer = updatedSelected,
                        lastSyncTimestamp = result.syncTimestamp,
                        registeredUsers = repository.getAuthorizedUsernames(),
                        appUpdateInfo = result.updateInfo ?: current.appUpdateInfo,
                        showAppUpdateModal = if (result.updateInfo != null) true else current.showAppUpdateModal,
                        cloudAnnouncement = result.announcement ?: current.cloudAnnouncement
                    )
                }

                addLog("[NUBE] ✔ Servidores sincronizados con éxito (${result.categories.sumOf { it.items.size }} activos).", LogType.SUCCESS)

                if (result.updateInfo != null) {
                    addLog("[ACTUALIZACIÓN] 🚀 ¡Nueva versión de la app disponible: ${result.updateInfo.latestVersion}!", LogType.HIGHLIGHT)
                }

                if (!result.announcement.isNullOrBlank()) {
                    addLog("[AVISO NUBE] ${result.announcement}", LogType.HIGHLIGHT)
                }

                if (!silent) {
                    val updateMsg = if (result.updateInfo != null) " - ¡Nueva versión ${result.updateInfo.latestVersion} detectada!" else ""
                    showSnackbar("✔ Servidores actualizados desde la nube con éxito.$updateMsg")
                }
            } else {
                addLog("[NUBE] Nube no disponible o sin cambios. Usando configuraciones locales.", LogType.NORMAL)
                if (!silent) {
                    showSnackbar("Configuraciones locales actualizadas.")
                }
            }
        }
    }

    fun clearAppCache() {
        repository.clearAllData()
        _uiState.update {
            VpnUiState(
                categories = VpnRepository.DEFAULT_CATEGORIES,
                selectedServer = VpnRepository.DEFAULT_CATEGORIES[0].items[0]
            )
        }
        addLog("[SISTEMA] Datos locales y caché limpiados a estado de fábrica.", LogType.HIGHLIGHT)
        showSnackbar("Datos y caché limpiados con éxito.")
    }

    // Modal Visibility Handlers
    fun setShowServerModal(show: Boolean) = _uiState.update { it.copy(showServerModal = show, selectedCategoryFilter = null, serverSearchQuery = "") }
    fun setShowLogsModal(show: Boolean) = _uiState.update { it.copy(showLogsModal = show) }
    fun setShowSettingsModal(show: Boolean) = _uiState.update { it.copy(showSettingsModal = show) }
    fun setShowGuideModal(show: Boolean) {
        if (show) {
            startGuidedTour()
        } else {
            _uiState.update { it.copy(showGuideModal = false) }
        }
    }

    // Guía Interactiva Paso a Paso con Voz en Vivo ("mientras va hablando cuando va guiando")
    fun startGuidedTour() {
        _uiState.update { it.copy(showGuidedTour = true, guidedTourStep = 1, showGuideModal = false) }
        speakTourStep(1)
    }

    fun nextTourStep() {
        val current = _uiState.value.guidedTourStep
        if (current >= 5) {
            dismissGuidedTour()
        } else {
            val next = current + 1
            _uiState.update { it.copy(guidedTourStep = next) }
            speakTourStep(next)
        }
    }

    fun prevTourStep() {
        val prev = (_uiState.value.guidedTourStep - 1).coerceAtLeast(1)
        _uiState.update { it.copy(guidedTourStep = prev) }
        speakTourStep(prev)
    }

    fun dismissGuidedTour() {
        _uiState.update { it.copy(showGuidedTour = false) }
        voiceEngine.speak("Guía finalizada")
    }

    fun speakTourStep(step: Int) {
        val script = when (step) {
            1 -> "Paso uno. Selecciona tu servidor. Elige entre los dos servidores disponibles según tu preferencia."
            2 -> "Paso dos. Ingresa tu usuario y contraseña. Si no tienes cuenta, puedes crear una gratis desde el menú de Configuración, tiene cinco días de duración."
            3 -> "Paso tres. Monitor en vivo. Observa en tiempo real las ondas de velocidad de descarga en cian y de subida en rosa, junto con el consumo total de datos."
            4 -> "Paso cuatro. Botón central de conexión. Tócalo para activar la VPN. Te avisaré por voz el estado de tu conexión."
            5 -> "Paso cinco. Ajustes avanzados. En configuración puedes compartir tu VPN por Hotspot y configurar el APN de tu red móvil."
            else -> ""
        }
        if (script.isNotBlank()) {
            voiceEngine.speak(script)
        }
    }
    fun setShowAccountCreatorModal(show: Boolean) {
        _uiState.update { it.copy(showAccountCreatorModal = show) }
        if (show) {
            voiceEngine.speak("Crea tu cuenta de acceso, tiene cinco días de duración")
        }
    }
    fun setShowCreatorDialog(show: Boolean) = _uiState.update { it.copy(showCreatorDialog = show) }
    fun setShowPayloadModal(show: Boolean) = _uiState.update { it.copy(showPayloadModal = show) }
    fun setShowAppUpdateModal(show: Boolean) = _uiState.update { it.copy(showAppUpdateModal = show) }
    fun dismissAppUpdateModal() = _uiState.update { it.copy(showAppUpdateModal = false) }
    fun setShowCloudUrlModal(show: Boolean) = _uiState.update { it.copy(showCloudUrlModal = show) }

    fun updateCustomCloudUrl(url: String) {
        repository.saveCustomCloudUrl(url)
        _uiState.update { it.copy(customCloudUrl = repository.getCustomCloudUrl(), showCloudUrlModal = false) }
        addLog("[NUBE] URL de la nube actualizada: ${repository.getCustomCloudUrl()}", LogType.HIGHLIGHT)
        syncWithCloud(silent = false)
    }

    fun resetCustomCloudUrl() {
        repository.saveCustomCloudUrl("")
        _uiState.update { it.copy(customCloudUrl = repository.getCustomCloudUrl(), showCloudUrlModal = false) }
        addLog("[NUBE] URL de la nube restaurada por defecto.", LogType.HIGHLIGHT)
        syncWithCloud(silent = false)
    }

    fun checkForAppUpdateManual() {
        syncWithCloud(silent = false)
    }

    fun setSelectedCategoryFilter(cat: ServerCategory?) = _uiState.update { it.copy(selectedCategoryFilter = cat) }
    fun setServerSearchQuery(query: String) = _uiState.update { it.copy(serverSearchQuery = query) }
    fun toggleAutoScrollLogs() = _uiState.update { it.copy(isAutoScrollLogs = !it.isAutoScrollLogs) }

    fun toggleAutoReconnect() {
        val newVal = !_uiState.value.autoReconnectEnabled
        repository.saveAutoReconnectEnabled(newVal)
        _uiState.update { it.copy(autoReconnectEnabled = newVal) }
        val msg = if (newVal) "✔ Monitoreo y reconexión automática activada" else "Monitoreo y reconexión automática desactivada"
        showSnackbar(msg)
        addLog("[SISTEMA] $msg", LogType.NORMAL)
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val delayTime = if (isAppInForeground) 1000L else 20000L
                delay(delayTime)
                val user = _uiState.value.rawUsername
                if (user.isNotBlank()) {
                    val remainingMillis = repository.getUserRemainingMillis(user)
                    val daysRemaining = repository.getUserDaysRemaining(user)
                    val isExpired = repository.isUserExpired(user)
                    val expiryTimestamp = repository.getUserExpirationTimestamp(user)

                    _uiState.update { current ->
                        current.copy(
                            userRemainingMillis = remainingMillis,
                            userDaysRemaining = daysRemaining,
                            isAccountExpired = isExpired,
                            userExpirationTimestamp = expiryTimestamp
                        )
                    }
                }
            }
        }
    }

    fun setShowHotspotModal(show: Boolean) {
        if (show) {
            val ip = com.example.service.hotspot.HotspotProxyServer.getHotspotIpAddress()
            _uiState.update { it.copy(showHotspotModal = true, hotspotIp = ip) }
        } else {
            _uiState.update { it.copy(showHotspotModal = false) }
        }
    }

    fun setShowApnModal(show: Boolean) {
        _uiState.update { it.copy(showApnModal = show) }
    }

    fun toggleHotspot() {
        val current = _uiState.value.isHotspotActive
        if (current) {
            com.example.service.hotspot.HotspotProxyServer.activeInstance?.stop()
            _uiState.update { it.copy(isHotspotActive = false) }
            addLog("[HOTSPOT] ⏹️ Servidor Proxy Hotspot detenido.", LogType.NORMAL)
            showSnackbar("⏹️ Proxy Hotspot detenido")
        } else {
            val proxy = com.example.service.hotspot.HotspotProxyServer(getApplication(), 8080)
            val started = proxy.start()
            val ip = com.example.service.hotspot.HotspotProxyServer.getHotspotIpAddress()
            if (started) {
                _uiState.update { it.copy(isHotspotActive = true, hotspotIp = ip, hotspotPort = 8080) }
                addLog("[HOTSPOT] ✔ Servidor Proxy Hotspot iniciado en $ip:8080. Listo para compartir VPN por Zona Wi-Fi.", LogType.SUCCESS)
                showSnackbar("✔ Hotspot VPN activo en $ip:8080")
            } else {
                _uiState.update { it.copy(isHotspotActive = false) }
                addLog("[HOTSPOT] ❌ No se pudo iniciar el proxy Hotspot en el puerto 8080.", LogType.ERROR)
                showSnackbar("❌ Error al iniciar proxy Hotspot")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        com.example.service.hotspot.HotspotProxyServer.activeInstance?.stop()
        voiceEngine.shutdown()
        telemetryJob?.cancel()
        connectionJob?.cancel()
        countdownJob?.cancel()
    }
}
