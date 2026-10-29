package com.example.service.tun2socks

import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Tun2SocksBridge: Boilerplate manager for loading, initializing, and bridging
 * the Android system TUN interface (tun0) with a local SOCKS5 proxy port (127.0.0.1:1080).
 *
 * It manages:
 * 1. Native library discovery and loading (libtun2socks.so / badvpn / hev).
 * 2. JNI initialization boilerplate and signature mapping.
 * 3. Fallback to high-performance managed L3-to-SOCKS5 packet translation.
 * 4. Safe bridge lifecycle management (start/stop/diagnostics).
 */
class Tun2SocksBridge(
    private val vpnService: VpnService,
    private val pfd: ParcelFileDescriptor,
    private val socksHost: String = "127.0.0.1",
    private val socksPort: Int = 1080,
    private val dnsServers: List<String> = listOf("1.1.1.1", "8.8.8.8"),
    private val vpnIp: String = "10.0.0.2",
    private val vpnNetmask: String = "255.255.255.0",
    private val vpnMtu: Int = 1500
) {
    companion object {
        const val TAG = "Tun2SocksBridge"

        // Candidate native library names bundled in Android APKs
        private val NATIVE_LIB_CANDIDATES = listOf("tun2socks", "badvpn-tun2socks", "hev-socks5-tunnel")

        var isNativeLoaded: Boolean = false
            private set
        var loadedLibraryName: String? = null
            private set

        init {
            loadNativeLibrary()
        }

        /**
         * Attempts to load available native tun2socks shared libraries (.so)
         * across supported CPU ABIs.
         */
        @Synchronized
        fun loadNativeLibrary(): Boolean {
            if (isNativeLoaded) return true

            for (libName in NATIVE_LIB_CANDIDATES) {
                try {
                    System.loadLibrary(libName)
                    isNativeLoaded = true
                    loadedLibraryName = libName
                    Log.i(TAG, "Native library '$libName' successfully loaded for ABI: ${Build.SUPPORTED_ABIS.joinToString()}")
                    return true
                } catch (e: UnsatisfiedLinkError) {
                    Log.d(TAG, "Native library '$libName' not found or unsatisfied in APK: ${e.message}")
                } catch (t: Throwable) {
                    Log.w(TAG, "Unexpected error loading native library '$libName': ${t.message}")
                }
            }

            Log.i(TAG, "Native tun2socks library not bundled. Using high-performance ManagedTun2Socks bridge.")
            return false
        }

        // --- Native JNI Boilerplate Declarations ---
        @JvmStatic
        private external fun runTun2Socks(
            vpnFd: Int,
            vpnMtu: Int,
            vpnIp: String,
            vpnNetmask: String,
            socksServer: String,
            udpgwServer: String?,
            udpgwTransparentDns: Int
        ): Int

        @JvmStatic
        private external fun terminateTun2Socks(): Int
    }

    private val isBridgeRunning = AtomicBoolean(false)
    private var nativeThread: Thread? = null
    private var managedEngine: Tun2SocksEngine? = null

    /**
     * Starts the bridge between the system TUN interface and the local SOCKS5 proxy port.
     */
    fun start(): Boolean {
        if (isBridgeRunning.getAndSet(true)) {
            Log.w(TAG, "Bridge is already running.")
            return true
        }

        val tunFd = pfd.fd
        val socksEndpoint = "$socksHost:$socksPort"
        Log.i(TAG, "Establishing TUN-to-SOCKS5 bridge (TUN FD: $tunFd -> $socksEndpoint)")

        if (isNativeLoaded) {
            try {
                Log.i(TAG, "Initializing native bridge via JNI ($loadedLibraryName)...")
                nativeThread = thread(start = true, isDaemon = true, name = "NativeTun2SocksLoop") {
                    try {
                        val result = runTun2Socks(
                            vpnFd = tunFd,
                            vpnMtu = vpnMtu,
                            vpnIp = vpnIp,
                            vpnNetmask = vpnNetmask,
                            socksServer = socksEndpoint,
                            udpgwServer = "127.0.0.1:7300",
                            udpgwTransparentDns = 1
                        )
                        Log.i(TAG, "Native tun2socks loop exited with code: $result")
                    } catch (e: UnsatisfiedLinkError) {
                        Log.w(TAG, "Native JNI symbol mismatch. Switching to ManagedTun2Socks engine: ${e.message}")
                        fallbackToManagedEngine()
                    } catch (t: Throwable) {
                        Log.e(TAG, "Native tun2socks execution failure: ${t.message}", t)
                        fallbackToManagedEngine()
                    }
                }
                return true
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to launch native bridge: ${t.message}. Falling back to managed engine.")
            }
        }

        // Fallback or Primary: Managed Pure-Kotlin Tun2Socks Engine
        return fallbackToManagedEngine()
    }

    private fun fallbackToManagedEngine(): Boolean {
        try {
            Log.i(TAG, "Activating ManagedTun2Socks engine (FD: ${pfd.fd} -> $socksHost:$socksPort, DNS: $dnsServers)")
            val engine = Tun2SocksEngine(
                vpnService = vpnService,
                pfd = pfd,
                socksHost = socksHost,
                socksPort = socksPort,
                dnsServers = dnsServers
            )
            engine.start()
            managedEngine = engine
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ManagedTun2Socks engine: ${e.message}", e)
            isBridgeRunning.set(false)
            return false
        }
    }

    /**
     * Stops the bridge and frees background thread resources.
     */
    fun stop() {
        if (!isBridgeRunning.getAndSet(false)) return
        Log.i(TAG, "Stopping TUN-to-SOCKS5 bridge...")

        // Terminate native loop if active
        if (isNativeLoaded) {
            try {
                terminateTun2Socks()
            } catch (_: Throwable) {}
        }
        nativeThread?.interrupt()
        nativeThread = null

        // Stop managed engine if active
        managedEngine?.stop()
        managedEngine = null

        Log.i(TAG, "TUN-to-SOCKS5 bridge successfully stopped.")
    }

    fun isRunning(): Boolean = isBridgeRunning.get()

    fun getBridgeInfo(): String {
        val mode = if (isNativeLoaded && nativeThread?.isAlive == true) "Native ($loadedLibraryName)" else "Managed Tun2Socks"
        return "Status: ${if (isBridgeRunning.get()) "ACTIVE" else "STOPPED"} | Backend: $mode | Target: $socksHost:$socksPort"
    }
}
