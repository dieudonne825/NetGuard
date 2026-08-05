package com.example.model

enum class ConnectivityState {
    HEALTHY,
    DEGRADED,
    WIFI_LOSS,
    TOTAL_LOSS
}

fun NetworkFaultType.toConnectivityState(): ConnectivityState {
    return when (this) {
        NetworkFaultType.NONE_ONLINE -> ConnectivityState.HEALTHY
        NetworkFaultType.WIFI_INTERFERENCE -> ConnectivityState.WIFI_LOSS
        NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> ConnectivityState.TOTAL_LOSS
        else -> ConnectivityState.DEGRADED
    }
}
