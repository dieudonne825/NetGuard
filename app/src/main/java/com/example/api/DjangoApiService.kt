package com.example.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class ClientRegistrationRequest(
    val client_code: String,
    val client_name: String,
    val city: String,
    val neighborhood: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val device_model: String,
    val monitored_wifi_ssids: List<String> = emptyList()
)

data class ClientRegistrationResponse(
    val status: String,
    val message: String,
    val client_code: String?,
    val is_active: Boolean = true
)

data class TelemetryHeartbeatRequest(
    val client_code: String,
    val client_name: String,
    val is_online: Boolean,
    val fault_type: String,
    val connection_type: String,
    val latency_ms: Long,
    val signal_strength_pct: Int,
    val ip_address: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TelemetryHeartbeatResponse(
    val status: String,
    val ack: Boolean,
    val server_timestamp: Long = System.currentTimeMillis()
)

interface DjangoApiService {
    @POST("api/v1/clients/register/")
    suspend fun registerClient(
        @Body request: ClientRegistrationRequest
    ): Response<ClientRegistrationResponse>

    @POST("api/v1/telemetry/heartbeat/")
    suspend fun sendHeartbeat(
        @Body request: TelemetryHeartbeatRequest
    ): Response<TelemetryHeartbeatResponse>

    @GET("api/v1/clients/{code}/status/")
    suspend fun getClientStatus(
        @Path("code") clientCode: String
    ): Response<ClientRegistrationResponse>
}
