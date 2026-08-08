package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import java.util.Locale

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val source: String
)

data class AddressInfo(
    val city: String,
    val neighborhood: String,
    val fullAddress: String
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
        // Fallback default coordinates (e.g. Yaoundé region defaults if GPS not ready)
        return LocationCoordinates(3.8480, 11.5021, "Position Estimée")
    }

    fun getAddressFromLocation(context: Context, latitude: Double, longitude: Double): AddressInfo {
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val detectedCity = address.locality 
                        ?: address.subAdminArea 
                        ?: address.adminArea 
                        ?: "Yaoundé"
                    val detectedNeighborhood = address.subLocality 
                        ?: address.thoroughfare 
                        ?: address.featureName 
                        ?: "Bastos"
                    
                    val full = listOfNotNull(detectedNeighborhood, detectedCity, address.countryName)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                    return AddressInfo(
                        city = detectedCity,
                        neighborhood = detectedNeighborhood,
                        fullAddress = full
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback pour la détection hors-ligne ou si le service Geocoder est indisponible
        return AddressInfo(
            city = "Yaoundé",
            neighborhood = "Bastos",
            fullAddress = "Bastos, Yaoundé, Cameroun"
        )
    }

    fun generateUniqueClientCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val randomPart = (1..6).map { chars.random() }.joinToString("")
        return "CLI-$randomPart"
    }
}

