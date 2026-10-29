package com.example.model

enum class VpnState(val label: String) {
    DISCONNECTED("DESCONECTADO"),
    CONNECTING("CONECTANDO..."),
    RECONNECTING("RECONECTANDO..."),
    WAITING("ESPERANDO..."),
    CONNECTED("CONECTADO"),
    UPDATING("ACTUALIZANDO...")
}

enum class LogType {
    NORMAL,
    SUCCESS,
    ERROR,
    HIGHLIGHT
}

data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: LogType = LogType.NORMAL
)

data class ServerItem(
    val id: Int,
    val name: String,
    val host: String,
    val cf_host: String = "",
    val port: Int = 80,
    val mode: String = "SSH_DIRECT",
    val payload: String = "",
    val icon: String = "",
    val description: String = "EEUU🇺🇸",
    val udpPort: Int = 7300,
    val dns1: String = "8.8.8.8",
    val dns2: String = "8.8.4.4"
)

data class ServerCategory(
    val id: Int,
    val name: String,
    val items: List<ServerItem> = emptyList()
)

data class NetworkTelemetry(
    val downloadSpeed: String = "0.0 KB/s",
    val uploadSpeed: String = "0.0 KB/s",
    val totalDownloadBytes: Long = 0L,
    val totalUploadBytes: Long = 0L,
    val networkName: String = "ARGENTINA",
    val pingMs: Int = 45
)

data class AppUpdateInfo(
    val latestVersion: String,
    val versionCode: Int = 0,
    val downloadUrl: String = "",
    val changelog: String = "",
    val forceUpdate: Boolean = false,
    val announcement: String = ""
)

data class CloudSyncResult(
    val success: Boolean,
    val categories: List<ServerCategory>,
    val updateInfo: AppUpdateInfo? = null,
    val announcement: String? = null,
    val syncTimestamp: String = ""
)
