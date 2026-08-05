package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

data class WifiConnectionDetails(
    val isWifiConnected: Boolean,
    val ssid: String,
    val bssid: String?,
    val signalLevelPct: Int,
    val frequencyBand: String
)

object WifiDetector {
    fun getConnectedWifiInfo(context: Context): WifiConnectionDetails {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        
        if (!isWifi || wifiManager == null) {
            return WifiConnectionDetails(
                isWifiConnected = false,
                ssid = "Données Mobiles / Non Connecté",
                bssid = null,
                signalLevelPct = 0,
                frequencyBand = "N/A"
            )
        }

        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
        var ssid = wifiInfo?.ssid?.replace("\"", "") ?: "Wi-Fi Détecté"
        if (ssid == "<unknown ssid>" || ssid.isBlank()) {
            ssid = "Wi-Fi Principal"
        }

        val rssi = wifiInfo?.rssi ?: -65
        val signalLevel = WifiManager.calculateSignalLevel(rssi, 100)
        val freq = wifiInfo?.frequency ?: 5000
        val band = if (freq > 4900) "5 GHz" else "2.4 GHz"

        return WifiConnectionDetails(
            isWifiConnected = true,
            ssid = ssid,
            bssid = wifiInfo?.bssid,
            signalLevelPct = signalLevel.coerceIn(20, 100),
            frequencyBand = band
        )
    }
}
