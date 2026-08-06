package com.example.model

data class ClientProfileData(
    val clientName: String = "",
    val city: String = "Douala",
    val neighborhood: String = "Akwa",
    val phone: String = "",
    val latitude: Double = 4.0511,
    val longitude: Double = 9.7679,
    val clientCode: String = "",
    val djangoBackendUrl: String = "https://netguard-admin.example.com/"
)
