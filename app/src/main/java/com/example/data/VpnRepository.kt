package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.model.AppUpdateInfo
import com.example.model.CloudSyncResult
import com.example.model.ServerCategory
import com.example.model.ServerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VpnRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("vpn_argentina_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    init {
        // Asegurar que use la lista limpia con categorías separadas para Personal y Claro
        prefs.edit().remove("cloud_servers_cache").putString("installed_app_version", "1.2.0").apply()
    }

    companion object {
        const val CLOUD_CONFIG_URL = "https://api.jsonbin.io/v3/b/6a9335beda38895dfe210990/latest"
        const val CLOUD_API_KEY = "\$2a\$10\$ImZG4IP10q1LP9KQmgeZi.hS54yx2Hxo7.R8Q995FXw1iP0CPU23W"

        val DEFAULT_CATEGORIES = listOf(
            ServerCategory(
                id = 24,
                name = "Servidores Personal 🩵",
                items = listOf(
                    ServerItem(
                        id = 41,
                        name = "Personal WebSocket (HTTP Custom) 🩵",
                        host = "recargas.personal.com.ar",
                        cf_host = "d3axktnpdrjmnq.cloudfront.net",
                        port = 80,
                        mode = "SSH_DIRECT",
                        payload = "COPY / HTTP/1.3[crlf]Host: [host][crlf][crlf]\n[lf][lf][instant_split][lf][lf]\nX / HTTP/1.2[crlf]Host: [host][crlf][lf][crlf]GET /speedwaypro2 HTTP/1.1[crlf]Host: [rotate=d3axktnpdrjmnq.cloudfront.net][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        icon = "https://flagcdn.com/w40/ar.png",
                        description = "Argentina 🇦🇷 (Personal Sin Saldo)",
                        udpPort = 7300,
                        dns1 = "8.8.8.8",
                        dns2 = "8.8.4.4"
                    ),
                    ServerItem(
                        id = 42,
                        name = "Personal EEUU 🩵",
                        host = "recargas.personal.com.ar",
                        cf_host = "d3cdy6viklzpq1.cloudfront.net",
                        port = 80,
                        mode = "SSH_DIRECT",
                        payload = "GET /.rar HTTP/1.1[crlf]Host: [cf_host][crlf]X-Online-Host: [host][crlf]X-Forward-Host: [host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        icon = "https://flagcdn.com/w40/us.png",
                        description = "EEUU 🇺🇲 (Personal)",
                        udpPort = 7300,
                        dns1 = "8.8.8.8",
                        dns2 = "8.8.4.4"
                    )
                )
            ),
            ServerCategory(
                id = 25,
                name = "Servidores Claro 🔴",
                items = listOf(
                    ServerItem(
                        id = 51,
                        name = "Claro Argentina (Sin Saldo) 🔴",
                        host = "simple.claro.com.ar",
                        cf_host = "d3axktnpdrjmnq.cloudfront.net",
                        port = 80,
                        mode = "SSH_DIRECT",
                        payload = "GET / HTTP/1.1[crlf]Host: [cf_host][crlf]X-Online-Host: [host][crlf]X-Forward-Host: [host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        icon = "https://flagcdn.com/w40/ar.png",
                        description = "Argentina 🇦🇷 (Claro Sin Saldo)",
                        udpPort = 7300,
                        dns1 = "8.8.8.8",
                        dns2 = "8.8.4.4"
                    ),
                    ServerItem(
                        id = 52,
                        name = "Claro WebSocket (Redes) 🔴",
                        host = "claromusica.com",
                        cf_host = "d3cdy6viklzpq1.cloudfront.net",
                        port = 80,
                        mode = "SSH_DIRECT",
                        payload = "GET / HTTP/1.1[crlf]Host: [cf_host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]",
                        icon = "https://flagcdn.com/w40/ar.png",
                        description = "Argentina 🇦🇷 (Claro Redes)",
                        udpPort = 7300,
                        dns1 = "8.8.8.8",
                        dns2 = "8.8.4.4"
                    )
                )
            )
        )
    }

    sealed class AuthResult {
        object Success : AuthResult()
        object EmptyUser : AuthResult()
        object UserNotFound : AuthResult()
        object InvalidPassword : AuthResult()
    }

    private val defaultAccounts = mapOf(
        "cesar" to "123456",
        "cesarg" to "123456",
        "danael" to "123456",
        "admin" to "123456",
        "familia" to "123456",
        "hboarg" to "123456",
        "hernan" to "123456",
        "usuario" to "123456",
        "personal" to "123456",
        "claro" to "123456",
        "test" to "123456",
        "teste7" to "Dawalker",
        "liam0408" to "123456"
    )

    fun isUserAuthorized(rawUser: String): Boolean {
        val clean = rawUser.trim().lowercase().removePrefix("danaelssh-")
        if (clean.isBlank() || clean.length < 2) return false
        val authorized = getAuthorizedUsernames()
        return authorized.contains(clean)
    }

    fun validateCredentials(rawUser: String, pass: String): AuthResult {
        val userClean = rawUser.trim()
        val passClean = pass.trim()

        if (userClean.isBlank()) {
            return AuthResult.EmptyUser
        }
        if (passClean.isBlank()) {
            return AuthResult.InvalidPassword
        }

        // Permite la conexión a los servidores con el usuario ingresado
        return AuthResult.Success
    }

    private fun getExpectedPassword(user: String): String {
        defaultAccounts[user]?.let { return it }
        val customUsersJson = prefs.getString("custom_users_list", null)
        if (customUsersJson != null) {
            try {
                val json = JSONObject(customUsersJson)
                if (json.has(user)) return json.getString(user)
            } catch (_: Exception) {}
        }
        return "123456"
    }

    fun getAuthorizedUsernames(): List<String> {
        val list = defaultAccounts.keys.toMutableList()
        val customUsersJson = prefs.getString("custom_users_list", null)
        if (customUsersJson != null) {
            try {
                val json = JSONObject(customUsersJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next().lowercase()
                    if (!list.contains(k)) list.add(k)
                }
            } catch (_: Exception) {}
        }
        return list
    }

    fun addCustomUser(user: String, pass: String) {
        val cleanUser = user.trim().lowercase()
        val currentJson = prefs.getString("custom_users_list", "{}") ?: "{}"
        try {
            val json = JSONObject(currentJson)
            json.put(cleanUser, pass.trim())
            prefs.edit().putString("custom_users_list", json.toString()).apply()
        } catch (_: Exception) {}
        // Guardar timestamp de creación para la validez de 5 días
        prefs.edit().putLong("user_created_at_$cleanUser", System.currentTimeMillis()).apply()
    }

    /**
     * Retorna el timestamp exacto de expiración (creación + 5 días en milisegundos).
     */
    fun getUserExpirationTimestamp(user: String): Long {
        val clean = user.trim().lowercase().removePrefix("danaelssh-")
        if (clean.isBlank()) return 0L
        val key = "user_created_at_$clean"
        var createdAt = prefs.getLong(key, 0L)
        if (createdAt == 0L) {
            createdAt = System.currentTimeMillis()
            prefs.edit().putLong(key, createdAt).apply()
        }
        val fiveDaysMillis = 5L * 24L * 60L * 60L * 1000L
        return createdAt + fiveDaysMillis
    }

    /**
     * Retorna los milisegundos restantes de validez de la cuenta (máximo 5 días).
     * Retorna 0L si ya expiró.
     */
    fun getUserRemainingMillis(user: String): Long {
        val expiry = getUserExpirationTimestamp(user)
        if (expiry == 0L) return 0L
        val diff = expiry - System.currentTimeMillis()
        return if (diff > 0L) diff else 0L
    }

    /**
     * Retorna los días restantes de validez de la cuenta (máximo 5 días desde la creación).
     * Si no existe fecha registrada previa, se registra la fecha actual otorgando 5 días completos.
     * Retorna 0 o negativo si ha expirado.
     */
    fun getUserDaysRemaining(user: String): Long {
        val clean = user.trim().lowercase().removePrefix("danaelssh-")
        if (clean.isBlank()) return 0L
        val remainingMillis = getUserRemainingMillis(clean)
        return if (remainingMillis <= 0L) 0L else (remainingMillis / (24L * 60L * 60L * 1000L)) + 1L
    }

    fun isUserExpired(user: String): Boolean {
        // Sin expiración de 5 días
        return false
    }

    fun renewUserAccount(user: String) {
        val clean = user.trim().lowercase().removePrefix("danaelssh-")
        if (clean.isNotBlank()) {
            prefs.edit().putLong("user_created_at_$clean", System.currentTimeMillis()).apply()
        }
    }

    fun isBatterySaverEnabled(): Boolean {
        return prefs.getBoolean("battery_saver_enabled", true)
    }

    fun saveBatterySaverEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("battery_saver_enabled", enabled).apply()
    }

    fun getSavedUsername(): String {
        return prefs.getString("family_user", "") ?: ""
    }

    fun saveUsername(username: String) {
        prefs.edit().putString("family_user", username).apply()
    }

    fun getSavedPassword(): String {
        return prefs.getString("family_pass", "123456") ?: "123456"
    }

    fun savePassword(password: String) {
        prefs.edit().putString("family_pass", password).apply()
    }

    fun getSavedServerId(): Int {
        return prefs.getInt("saved_server_id", 101)
    }

    fun saveServerId(serverId: Int) {
        prefs.edit().putInt("saved_server_id", serverId).apply()
    }

    fun getCustomCloudFront(): String? {
        return prefs.getString("saved_custom_cf", null)
    }

    fun saveCustomCloudFront(cf: String?) {
        if (cf.isNullOrBlank()) {
            prefs.edit().remove("saved_custom_cf").apply()
        } else {
            prefs.edit().putString("saved_custom_cf", cf.trim()).apply()
        }
    }

    fun getSavedPayload(serverId: Int): String? {
        return prefs.getString("custom_payload_$serverId", null)
    }

    fun savePayload(serverId: Int, payload: String?) {
        if (payload.isNullOrBlank()) {
            prefs.edit().remove("custom_payload_$serverId").apply()
        } else {
            prefs.edit().putString("custom_payload_$serverId", payload).apply()
        }
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean("sound_enabled", true)
    }

    fun saveSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isAutoReconnectEnabled(): Boolean {
        return prefs.getBoolean("auto_reconnect_enabled", true)
    }

    fun saveAutoReconnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_reconnect_enabled", enabled).apply()
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }

    fun getCachedCategories(): List<ServerCategory> {
        val cachedJson = prefs.getString("cloud_servers_cache", null) ?: return DEFAULT_CATEGORIES
        return try {
            parseCategoriesFromJson(cachedJson)
        } catch (e: Exception) {
            Log.e("VpnRepository", "Error parsing cached categories", e)
            DEFAULT_CATEGORIES
        }
    }

    fun getCustomCloudUrl(): String {
        return prefs.getString("custom_cloud_url", CLOUD_CONFIG_URL) ?: CLOUD_CONFIG_URL
    }

    fun saveCustomCloudUrl(url: String) {
        if (url.isBlank() || url == CLOUD_CONFIG_URL) {
            prefs.edit().remove("custom_cloud_url").apply()
        } else {
            prefs.edit().putString("custom_cloud_url", url.trim()).apply()
        }
    }

    fun getLastSyncTimestamp(): String {
        return prefs.getString("last_cloud_sync_time", "No registrada") ?: "No registrada"
    }

    fun saveLastSyncTimestamp(ts: String) {
        prefs.edit().putString("last_cloud_sync_time", ts).apply()
    }

    private fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
        return try {
            val remoteParts = remoteVersion.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
            val localParts = localVersion.replace(Regex("[^0-9.]"), "").split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val l = localParts.getOrElse(i) { 0 }
                if (r > l) return true
                if (r < l) return false
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun extractUpdateInfo(obj: JSONObject): AppUpdateInfo? {
        val verName = obj.optString("app_version",
            obj.optString("version",
                obj.optString("latest_version",
                    obj.optString("version_name", "")
                )
            )
        ).trim()
        val verCode = obj.optInt("version_code", obj.optInt("build", 0))
        val downloadUrl = obj.optString("download_url",
            obj.optString("apk_url",
                obj.optString("update_url",
                    obj.optString("link", "https://t.me/CesarG")
                )
            )
        ).trim()
        val changelog = obj.optString("changelog",
            obj.optString("notes",
                obj.optString("notas",
                    obj.optString("changes", "Nuevos servidores y optimizaciones de velocidad.")
                )
            )
        ).trim()
        val forceUpdate = obj.optBoolean("force_update", obj.optBoolean("obligatorio", false))
        val announcement = obj.optString("announcement", obj.optString("mensaje", "")).trim()

        val currentVersion = "1.0.0"
        val currentCode = 1

        val isNewer = (verCode > currentCode) || (verName.isNotBlank() && isVersionNewer(verName, currentVersion))

        return if (isNewer && downloadUrl.isNotBlank()) {
            AppUpdateInfo(
                latestVersion = if (verName.isNotBlank()) verName else "v$verCode",
                versionCode = verCode,
                downloadUrl = downloadUrl,
                changelog = changelog,
                forceUpdate = forceUpdate,
                announcement = announcement
            )
        } else null
    }

    suspend fun fetchCloudConfig(): CloudSyncResult = withContext(Dispatchers.IO) {
        val currentUrl = getCustomCloudUrl()
        try {
            val reqBuilder = Request.Builder().url(currentUrl).get()
            if (currentUrl.contains("jsonbin.io")) {
                reqBuilder.header("X-Master-Key", CLOUD_API_KEY)
            }
            val request = reqBuilder.build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val trimmed = body.trim()
                    var recordArray: JSONArray? = null
                    var updateInfo: AppUpdateInfo? = null
                    var announcement: String? = null

                    if (trimmed.startsWith("{")) {
                        val rootObj = JSONObject(trimmed)
                        if (rootObj.has("record")) {
                            val recordVal = rootObj.get("record")
                            if (recordVal is JSONArray) {
                                recordArray = recordVal
                            } else if (recordVal is JSONObject) {
                                updateInfo = extractUpdateInfo(recordVal)
                                announcement = recordVal.optString("announcement", recordVal.optString("mensaje", null))
                                recordArray = recordVal.optJSONArray("servers")
                                    ?: recordVal.optJSONArray("categories")
                                    ?: recordVal.optJSONArray("items")
                                    ?: recordVal.optJSONArray("config")

                                val accountsObj = recordVal.optJSONObject("accounts") ?: recordVal.optJSONObject("users")
                                accountsObj?.let { accs ->
                                    val keys = accs.keys()
                                    while (keys.hasNext()) {
                                        val u = keys.next()
                                        val p = accs.optString(u, "123456")
                                        addCustomUser(u, p)
                                    }
                                }
                            }
                        } else {
                            updateInfo = extractUpdateInfo(rootObj)
                            announcement = rootObj.optString("announcement", rootObj.optString("mensaje", null))
                            recordArray = rootObj.optJSONArray("servers")
                                ?: rootObj.optJSONArray("categories")
                                ?: rootObj.optJSONArray("items")
                                ?: rootObj.optJSONArray("config")

                            val accountsObj = rootObj.optJSONObject("accounts") ?: rootObj.optJSONObject("users")
                            accountsObj?.let { accs ->
                                val keys = accs.keys()
                                while (keys.hasNext()) {
                                    val u = keys.next()
                                    val p = accs.optString(u, "123456")
                                    addCustomUser(u, p)
                                }
                            }
                        }
                    } else if (trimmed.startsWith("[")) {
                        recordArray = JSONArray(trimmed)
                    }

                    val nowTime = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

                    if (recordArray != null && recordArray.length() > 0) {
                        val categories = parseCategoriesFromJSONArray(recordArray).toMutableList()

                        if (categories.isNotEmpty()) {
                            prefs.edit().putString("cloud_servers_cache", recordArray.toString()).apply()
                            saveLastSyncTimestamp(nowTime)
                            return@withContext CloudSyncResult(
                                success = true,
                                categories = categories,
                                updateInfo = updateInfo,
                                announcement = announcement,
                                syncTimestamp = nowTime
                            )
                        }
                    } else if (updateInfo != null) {
                        saveLastSyncTimestamp(nowTime)
                        return@withContext CloudSyncResult(
                            success = true,
                            categories = getCachedCategories(),
                            updateInfo = updateInfo,
                            announcement = announcement,
                            syncTimestamp = nowTime
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("VpnRepository", "Cloud sync exception: ${e.message}")
        }
        return@withContext CloudSyncResult(
            success = false,
            categories = getCachedCategories(),
            syncTimestamp = getLastSyncTimestamp()
        )
    }

    private fun parseCategoriesFromJson(jsonStr: String): List<ServerCategory> {
        val trimmed = jsonStr.trim()
        val jsonArray = if (trimmed.startsWith("{")) {
            val rootObj = JSONObject(trimmed)
            if (rootObj.has("content")) {
                val b64 = rootObj.getString("content")
                val decoded = String(android.util.Base64.decode(b64, android.util.Base64.DEFAULT), Charsets.UTF_8)
                JSONArray(decoded)
            } else if (rootObj.has("record")) {
                rootObj.getJSONArray("record")
            } else {
                JSONArray().put(rootObj)
            }
        } else {
            JSONArray(trimmed)
        }
        return parseCategoriesFromJSONArray(jsonArray)
    }

    private fun parseCategoriesFromJSONArray(jsonArray: JSONArray): List<ServerCategory> {
        if (jsonArray.length() == 0) return emptyList()

        val firstObj = jsonArray.getJSONObject(0)
        // Check if this is the native DTunnel / TS Tunnel server array format
        if (firstObj.has("category_id") || firstObj.has("config_payload")) {
            val categoryMap = mutableMapOf<Int, MutableList<ServerItem>>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val catId = obj.optInt("category_id", 24)
                val serverObj = obj.optJSONObject("server")
                val payloadObj = obj.optJSONObject("config_payload")
                val dnsObj = obj.optJSONObject("dns_server")
                val udpArray = obj.optJSONArray("udp_ports")

                val payloadText = payloadObj?.optString("payload") ?: ""
                val host = serverObj?.optString("host") ?: "recargas.personal.com.ar"
                val port = serverObj?.optInt("port", 80) ?: 80
                val cfHostRegex = Regex("""Host:\s*([a-zA-Z0-9.\-]+\.cloudfront\.net)""")
                val cfHost = cfHostRegex.find(payloadText)?.groupValues?.get(1) ?: "d3cdy6viklzpq1.cloudfront.net"

                val item = ServerItem(
                    id = obj.optInt("id", 40 + i),
                    name = obj.optString("name", "Personal 🩵"),
                    host = host,
                    cf_host = cfHost,
                    port = port,
                    mode = obj.optString("mode", "SSH_DIRECT"),
                    payload = payloadText,
                    icon = "https://flagcdn.com/w40/us.png",
                    description = obj.optString("description", "EEUU🇺🇲"),
                    udpPort = udpArray?.optInt(0) ?: 7300,
                    dns1 = dnsObj?.optString("dns1", "8.8.8.8") ?: "8.8.8.8",
                    dns2 = dnsObj?.optString("dns2", "8.8.4.4") ?: "8.8.4.4"
                )
                categoryMap.getOrPut(catId) { mutableListOf() }.add(item)
            }

            return categoryMap.map { (catId, items) ->
                val catName = when (catId) {
                    24 -> "Servidores Personal 🩵"
                    25 -> "Servidores Claro 🔴"
                    else -> if (items.any { it.name.contains("Claro", ignoreCase = true) || it.host.contains("claro", ignoreCase = true) }) "Servidores Claro 🔴" else "Servidores Personal 🩵"
                }
                ServerCategory(id = catId, name = catName, items = items)
            }
        }

        // Standard category array format
        val result = mutableListOf<ServerCategory>()
        for (i in 0 until jsonArray.length()) {
            val catObj = jsonArray.getJSONObject(i)
            val catId = catObj.optInt("id", (i + 1) * 100)
            val catName = catObj.optString("name", "Categoría")
            val itemsList = mutableListOf<ServerItem>()

            if (catObj.has("items")) {
                val itemsArray = catObj.getJSONArray("items")
                for (j in 0 until itemsArray.length()) {
                    val itemObj = itemsArray.getJSONObject(j)
                    itemsList.add(
                        ServerItem(
                            id = itemObj.optInt("id", catId + j + 1),
                            name = itemObj.optString("name", "Servidor"),
                            host = itemObj.optString("host", ""),
                            cf_host = itemObj.optString("cf_host", ""),
                            port = itemObj.optInt("port", 80),
                            mode = itemObj.optString("mode", "SSH_DIRECT"),
                            payload = itemObj.optString("payload", ""),
                            icon = itemObj.optString("icon", "")
                        )
                    )
                }
            }
            result.add(ServerCategory(id = catId, name = catName, items = itemsList))
        }
        return result
    }

    fun getLocalIpAddress(): String {
        return try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
            "10.88.222.93"
        } catch (_: Exception) {
            "10.88.222.93"
        }
    }

    fun getActiveNetworkName(): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return "ARGENTINA"
            val capabilities = cm.getNetworkCapabilities(network) ?: return "ARGENTINA"

            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI CLOUD"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "PERSONAL / CLARO"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "ARGENTINA 4G/5G"
            }
        } catch (e: Exception) {
            "ARGENTINA"
        }
    }
}
