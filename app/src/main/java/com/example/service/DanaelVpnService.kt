package com.example.service

import android.content.Context

/**
 * DanaelVpnService extends LocalVPNService for backward compatibility and specialized routing.
 */
class DanaelVpnService : LocalVPNService() {

    companion object {
        fun startVpn(
            context: Context,
            serverName: String,
            username: String,
            password: String = "123456",
            host: String = "recargas.personal.com.ar",
            port: Int = 80,
            payload: String? = null,
            cfHost: String? = null
        ) {
            LocalVPNService.startVpn(
                context = context,
                serverName = serverName,
                username = username,
                password = password,
                host = host,
                port = port,
                payload = payload,
                cfHost = cfHost
            )
        }

        fun stopVpn(context: Context) {
            LocalVPNService.stopVpn(context)
        }
    }
}
