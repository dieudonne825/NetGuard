package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val source: String
)

object LocationUtils {
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context): LocationCoordinates {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (gpsLoc != null) {
                    return LocationCoordinates(gpsLoc.latitude, gpsLoc.longitude, "GPS Direct")
                }
                val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (netLoc != null) {
                    return LocationCoordinates(netLoc.latitude, netLoc.longitude, "Réseau Triangulation")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Fallback default coordinates (e.g. Douala / Yaoundé region defaults if GPS not ready)
        return LocationCoordinates(3.8480, 11.5021, "Position Estimée")
    }

    fun generateUniqueClientCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        return "CLI-$randomPart"
    }
}
