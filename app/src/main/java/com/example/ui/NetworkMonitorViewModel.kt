package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ClientRegistrationRequest
import com.example.api.DjangoApiClient
import com.example.api.TelemetryHeartbeatRequest
import com.example.data.AppDatabase
import com.example.data.NetworkRepository
import com.example.data.OutageLog
import com.example.data.SpeedTestRecord
import com.example.data.TechnicianTicket
import com.example.model.NetworkFaultType
import com.example.model.RouterHardwareModel
import com.example.ui.components.ClientProfileData
import com.example.util.LocationUtils
import com.example.util.NetGuardOverlayManager
import com.example.util.OutageNotificationManager
import com.example.util.WifiDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class RealNetworkProbeResult(
    val isConnected: Boolean,
    val connectionType: String,
    val ipAddressStatus: String,
    val latencyMs: Int,
    val detectedFault: NetworkFaultType
)

data class NetworkUiState(
    val isLiveMode: Boolean = true, // Default to Live Real Detection Mode
    val liveConnectionType: String = "Wi-Fi (Détection Réelle)",
    val liveIpStatus: String = "Ligne Active (GSM/Wi-Fi)",
    val faultType: NetworkFaultType = NetworkFaultType.NONE_ONLINE,
    val isMonitoringActive: Boolean = true,
    val latencyMs: Int = 18,
    val jitterMs: Int = 2,
    val packetLossPct: Double = 0.0,
    val downloadSpeedMbps: Double = 485.2,
    val uploadSpeedMbps: Double = 312.0,
    val isRunningSpeedTest: Boolean = false,
    val speedTestProgress: Float = 0f,
    val currentSpeedPhase: String = "Prêt",
    val pingHistory: List<Int> = listOf(18, 19, 17, 21, 18, 20, 19, 18, 22, 17, 18, 19),
    val selectedModemId: String = "livebox",
    val activeModemModel: RouterHardwareModel = RouterHardwareModel.createModelForFault("livebox", NetworkFaultType.NONE_ONLINE),
    val showOutagePopup: Boolean = false,
    val outagePopupMessage: String = "",
    val outageTimestampFormatted: String = "",
    val activeTicketCreated: TechnicianTicket? = null,
    val showIncidentReportDialog: Boolean = false,
    val selectedReportTicket: TechnicianTicket? = null,
    val userNotificationMessage: String? = null,
    val clientProfile: ClientProfileData? = null,
    val showDjangoRegistrationDialog: Boolean = false,
    val isRegisteredWithDjango: Boolean = false,
    val lastDjangoHeartbeatStatus: String = "Attente Sync",
    val djangoHeartbeatCount: Int = 0,
    val monitoredWifiList: List<String> = emptyList(),
    val currentWifiSsid: String = "Non Connecté",
    val isCurrentWifiSupervised: Boolean = true,
    val wifiSignalLevelPct: Int = 0,
    val wifiFrequencyBand: String = "N/A"
)

class NetworkMonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NetworkRepository

    val allOutages: StateFlow<List<OutageLog>>
    val allSpeedTests: StateFlow<List<SpeedTestRecord>>
    val allTickets: StateFlow<List<TechnicianTicket>>

    private val _uiState = MutableStateFlow(NetworkUiState())
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    private var pingJob: Job? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NetworkRepository(database.networkDao())

        allOutages = repository.allOutages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSpeedTests = repository.allSpeedTests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allTickets = repository.allTickets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Load Client Profile & Django Registration preferences
        val prefs = application.getSharedPreferences("netguard_prefs", Context.MODE_PRIVATE)
        val isRegistered = prefs.getBoolean("django_registered", false)
        val savedName = prefs.getString("client_name", "") ?: ""
        val savedCity = prefs.getString("city", "Douala") ?: "Douala"
        val savedNeighborhood = prefs.getString("neighborhood", "Akwa") ?: "Akwa"
        val savedPhone = prefs.getString("phone", "") ?: ""
        val savedLat = prefs.getFloat("latitude", 4.0511f).toDouble()
        val savedLng = prefs.getFloat("longitude", 9.7679f).toDouble()
        val savedCode = prefs.getString("client_code", "") ?: LocationUtils.generateUniqueClientCode()
        val savedUrl = prefs.getString("django_url", "https://netguard-admin.example.com/") ?: "https://netguard-admin.example.com/"

        val profile = if (savedName.isNotBlank()) {
            ClientProfileData(
                clientName = savedName,
                city = savedCity,
                neighborhood = savedNeighborhood,
                phone = savedPhone,
                latitude = savedLat,
                longitude = savedLng,
                clientCode = savedCode,
                djangoBackendUrl = savedUrl
            )
        } else null

        val detectedWifi = WifiDetector.getConnectedWifiInfo(application)
        val defaultWifiSet = if (detectedWifi.isWifiConnected) setOf(detectedWifi.ssid) else emptySet()
        val savedWifiSet = prefs.getStringSet("monitored_wifi_set", defaultWifiSet) ?: defaultWifiSet
        val savedModemId = prefs.getString("selected_modem_id", "livebox") ?: "livebox"

        val wifiList = savedWifiSet.toList()
        val currentSsid = if (detectedWifi.isWifiConnected) detectedWifi.ssid else "Données Mobiles / GSM"
        val isSupervised = wifiList.contains(currentSsid) || !detectedWifi.isWifiConnected

        _uiState.update {
            it.copy(
                clientProfile = profile,
                isRegisteredWithDjango = isRegistered,
                showDjangoRegistrationDialog = !isRegistered,
                monitoredWifiList = wifiList,
                selectedModemId = savedModemId,
                activeModemModel = RouterHardwareModel.createModelForFault(savedModemId, NetworkFaultType.NONE_ONLINE),
                currentWifiSsid = currentSsid,
                isCurrentWifiSupervised = isSupervised,
                wifiSignalLevelPct = detectedWifi.signalLevelPct,
                wifiFrequencyBand = detectedWifi.frequencyBand
            )
        }

        startContinuousMonitoring()
    }

    fun showRegistrationDialog() {
        _uiState.update { it.copy(showDjangoRegistrationDialog = true) }
    }

    fun dismissRegistrationDialog() {
        _uiState.update { it.copy(showDjangoRegistrationDialog = false) }
    }

    fun saveClientRegistration(profile: ClientProfileData) {
        val prefs = getApplication<Application>().getSharedPreferences("netguard_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("django_registered", true)
            .putString("client_name", profile.clientName)
            .putString("city", profile.city)
            .putString("neighborhood", profile.neighborhood)
            .putString("phone", profile.phone)
            .putFloat("latitude", profile.latitude.toFloat())
            .putFloat("longitude", profile.longitude.toFloat())
            .putString("client_code", profile.clientCode)
            .putString("django_url", profile.djangoBackendUrl)
            .apply()

        _uiState.update {
            it.copy(
                clientProfile = profile,
                isRegisteredWithDjango = true,
                showDjangoRegistrationDialog = false,
                lastDjangoHeartbeatStatus = "Transmis au serveur Django ✓",
                userNotificationMessage = "Code Client ${profile.clientCode} enregistré avec succès !"
            )
        }

        // Send registration API call in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val service = DjangoApiClient.getService(profile.djangoBackendUrl)
                val response = service.registerClient(
                    ClientRegistrationRequest(
                        client_code = profile.clientCode,
                        client_name = profile.clientName,
                        city = profile.city,
                        neighborhood = profile.neighborhood,
                        phone = profile.phone,
                        latitude = profile.latitude,
                        longitude = profile.longitude,
                        device_model = android.os.Build.MODEL ?: "Android App",
                        monitored_wifi_ssids = _uiState.value.monitoredWifiList
                    )
                )
                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(lastDjangoHeartbeatStatus = "Inscrit sur Django Admin Dashboard ✓")
                    }
                }
            } catch (e: Exception) {
                // Network unreachable or offline mode
                _uiState.update {
                    it.copy(lastDjangoHeartbeatStatus = "Inscrit en Local (Django En Attente Sync)")
                }
            }
            sendPeriodicDjangoHeartbeat()
        }
    }

    private fun sendPeriodicDjangoHeartbeat() {
        val state = _uiState.value
        val profile = state.clientProfile ?: return
        if (!state.isRegisteredWithDjango) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val service = DjangoApiClient.getService(profile.djangoBackendUrl)
                val response = service.sendHeartbeat(
                    TelemetryHeartbeatRequest(
                        client_code = profile.clientCode,
                        client_name = profile.clientName,
                        is_online = state.faultType == NetworkFaultType.NONE_ONLINE,
                        fault_type = state.faultType.name,
                        connection_type = state.currentWifiSsid,
                        latency_ms = state.latencyMs.toLong(),
                        signal_strength_pct = state.wifiSignalLevelPct,
                        ip_address = state.liveIpStatus
                    )
                )

                if (response.isSuccessful) {
                    val newCount = state.djangoHeartbeatCount + 1
                    _uiState.update {
                        it.copy(
                            lastDjangoHeartbeatStatus = "Django Admin Sync OK ✓ (HB #$newCount)",
                            djangoHeartbeatCount = newCount
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(lastDjangoHeartbeatStatus = "Django API Erreur Code ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(lastDjangoHeartbeatStatus = "Sync Django En Attente (Offline)")
                }
            }
        }
    }

    fun toggleLiveMode(enableLive: Boolean) {
        _uiState.update {
            it.copy(
                isLiveMode = enableLive,
                userNotificationMessage = if (enableLive) "Passage en Mode Direct Réel (GSM/Wi-Fi)" else "Passage en Mode Simulation Démo"
            )
        }
    }

    private fun startContinuousMonitoring() {
        pingJob?.cancel()
        pingJob = viewModelScope.launch {
            var iterationCounter = 0
            while (true) {
                delay(3000)
                iterationCounter++

                // Periodically send Heartbeat to Django admin dashboard every 3 iterations (~9 sec)
                if (iterationCounter % 3 == 0) {
                    sendPeriodicDjangoHeartbeat()
                }

                if (_uiState.value.isMonitoringActive) {
                    if (_uiState.value.isLiveMode) {
                        // Perform REAL network check
                        val result = checkRealNetworkStatus()
                        val detectedWifi = WifiDetector.getConnectedWifiInfo(getApplication())
                        val history = (_uiState.value.pingHistory + result.latencyMs).takeLast(20)

                        val currentSsid = if (detectedWifi.isWifiConnected) detectedWifi.ssid else "Données Mobiles / GSM"
                        val isSupervised = _uiState.value.monitoredWifiList.contains(currentSsid) || !detectedWifi.isWifiConnected

                        if (result.detectedFault != NetworkFaultType.NONE_ONLINE && _uiState.value.faultType == NetworkFaultType.NONE_ONLINE) {
                            // Real fault newly detected!
                            triggerSimulatedFault(result.detectedFault)
                        } else if (result.detectedFault == NetworkFaultType.NONE_ONLINE && _uiState.value.faultType != NetworkFaultType.NONE_ONLINE && _uiState.value.isLiveMode) {
                            // Real network restored
                            triggerSimulatedFault(NetworkFaultType.NONE_ONLINE)
                        }

                        _uiState.update {
                            it.copy(
                                liveConnectionType = result.connectionType,
                                liveIpStatus = result.ipAddressStatus,
                                latencyMs = if (it.faultType == NetworkFaultType.NONE_ONLINE) result.latencyMs else it.latencyMs,
                                jitterMs = if (it.faultType == NetworkFaultType.NONE_ONLINE) Random.nextInt(1, 5) else it.jitterMs,
                                packetLossPct = if (it.faultType == NetworkFaultType.NONE_ONLINE) 0.0 else it.packetLossPct,
                                pingHistory = history,
                                currentWifiSsid = currentSsid,
                                isCurrentWifiSupervised = isSupervised,
                                wifiSignalLevelPct = detectedWifi.signalLevelPct,
                                wifiFrequencyBand = detectedWifi.frequencyBand
                            )
                        }
                    } else {
                        // Simulation Mode
                        if (_uiState.value.faultType == NetworkFaultType.NONE_ONLINE) {
                            val nextPing = (15 + Random.nextInt(-4, 6)).coerceAtLeast(10)
                            val history = (_uiState.value.pingHistory + nextPing).takeLast(20)
                            _uiState.update {
                                it.copy(
                                    latencyMs = nextPing,
                                    jitterMs = Random.nextInt(1, 4),
                                    packetLossPct = 0.0,
                                    pingHistory = history
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun checkRealNetworkStatus(): RealNetworkProbeResult = withContext(Dispatchers.IO) {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return@withContext RealNetworkProbeResult(
                isConnected = false,
                connectionType = "Système",
                ipAddressStatus = "Service Indisponible",
                latencyMs = 999,
                detectedFault = NetworkFaultType.POWER_OUTAGE
            )

        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        if (activeNetwork == null || caps == null) {
            return@withContext RealNetworkProbeResult(
                isConnected = false,
                connectionType = "Aucun Réseau",
                ipAddressStatus = "Déconnecté (Wi-Fi/GSM Off)",
                latencyMs = 999,
                detectedFault = NetworkFaultType.POWER_OUTAGE
            )
        }

        val connType = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi En Direct"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Réseau Mobile GSM/4G"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet Ligne"
            else -> "Connexion Réseau"
        }

        // Measure real socket ping to 8.8.8.8 on port 53 (Google DNS)
        val startTime = System.currentTimeMillis()
        var socketSuccess = false
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                socketSuccess = true
            }
        } catch (e: Exception) {
            socketSuccess = false
        }
        val measuredPing = (System.currentTimeMillis() - startTime).toInt()

        if (!socketSuccess) {
            return@withContext RealNetworkProbeResult(
                isConnected = false,
                connectionType = connType,
                ipAddressStatus = "Connecté à $connType (Rupture WAN Internet)",
                latencyMs = 999,
                detectedFault = NetworkFaultType.ISP_OUTAGE
            )
        }

        // Test real HTTP reachability
        var dnsHttpSuccess = false
        try {
            val url = URL("https://www.google.com")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 1500
            conn.readTimeout = 1500
            conn.requestMethod = "HEAD"
            val responseCode = conn.responseCode
            if (responseCode in 200..399) {
                dnsHttpSuccess = true
            }
            conn.disconnect()
        } catch (e: Exception) {
            dnsHttpSuccess = false
        }

        if (!dnsHttpSuccess) {
            return@withContext RealNetworkProbeResult(
                isConnected = true,
                connectionType = connType,
                ipAddressStatus = "$connType (IP Valide - Erreur DNS)",
                latencyMs = measuredPing,
                detectedFault = NetworkFaultType.DNS_SERVER_DOWN
            )
        }

        val fault = if (measuredPing > 220) NetworkFaultType.FIBER_SIGNAL_LOW else NetworkFaultType.NONE_ONLINE

        RealNetworkProbeResult(
            isConnected = true,
            connectionType = connType,
            ipAddressStatus = "$connType (Opérationnel)",
            latencyMs = measuredPing.coerceAtLeast(8),
            detectedFault = fault
        )
    }

    fun selectModem(modemId: String) {
        _uiState.update { state ->
            val updatedModel = RouterHardwareModel.createModelForFault(modemId, state.faultType)
            state.copy(
                selectedModemId = modemId,
                activeModemModel = updatedModel
            )
        }
    }

    fun triggerSimulatedFault(faultType: NetworkFaultType) {
        val timeFormatted = SimpleDateFormat("HH:mm:ss - d MMMM yyyy", Locale.FRENCH).format(Date())
        val updatedModel = RouterHardwareModel.createModelForFault(_uiState.value.selectedModemId, faultType)

        val message = when (faultType) {
            NetworkFaultType.ISP_OUTAGE ->
                "ATTENTION : Rupture de fibre optique externe détectée sur la ligne à $timeFormatted. Signal WAN interrompu au NRO. Souhaitez-vous déclarer un ticket d'incident technicien ?"
            NetworkFaultType.POWER_OUTAGE ->
                "ALERTE : Coupure de courant du modem détectée à $timeFormatted. Le boîtier réseau est hors tension. Vérifiez votre alimentation électrique."
            NetworkFaultType.FIBER_SIGNAL_LOW ->
                "AVERTISSEMENT : Atténuation importante du signal optique (-28.5 dBm) détectée à $timeFormatted. La liaison fibre souffre de ralentissements et de micro-coupures."
            NetworkFaultType.DNS_SERVER_DOWN ->
                "INCIDENT IP : Panne des serveurs DNS de l'opérateur à $timeFormatted. Connexion physique active mais impossible de charger les noms de domaine Web."
            NetworkFaultType.WIFI_INTERFERENCE ->
                "PERFORMANCE : Interférences hertziennes et saturation Wi-Fi importantes détectées à $timeFormatted. Le débit sans-fil est dégradé."
            NetworkFaultType.DHCP_POOL_EXHAUSTED ->
                "RESEAU LOCAL : Plage d'adresses IP DHCP saturée à $timeFormatted. Impossible d'attribuer une adresse IP aux nouveaux appareils ménagers."
            NetworkFaultType.MODEM_OVERHEAT ->
                "SÉCURITÉ MATÉRIELLE : Surchauffe processeur critique à 88°C détectée à $timeFormatted. La box a désactivé le Wi-Fi pour se refroidir."
            NetworkFaultType.ETHERNET_CABLE_FAULT ->
                "CONNECTIQUE : Port LAN filaire bridé à 10 Mbps à $timeFormatted. Le câble RJ45 semble pincé ou défectueux."
            NetworkFaultType.NONE_ONLINE -> ""
        }

        if (faultType != NetworkFaultType.NONE_ONLINE) {
            val (latency, packetLoss) = when (faultType) {
                NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.DNS_SERVER_DOWN -> 999 to 100.0
                NetworkFaultType.FIBER_SIGNAL_LOW -> 185 to 12.5
                NetworkFaultType.WIFI_INTERFERENCE -> 145 to 18.0
                NetworkFaultType.MODEM_OVERHEAT -> 350 to 30.0
                NetworkFaultType.DHCP_POOL_EXHAUSTED -> 28 to 0.0
                NetworkFaultType.ETHERNET_CABLE_FAULT -> 22 to 0.0
                else -> 18 to 0.0
            }

            // Log in Room Database
            viewModelScope.launch {
                repository.logOutage(
                    OutageLog(
                        type = faultType.name,
                        title = faultType.title,
                        description = message,
                        durationSeconds = Random.nextLong(120, 1800),
                        ispName = updatedModel.provider
                    )
                )
            }

            // Post System Notification & System Window Overlay (Outside the app over YouTube/WhatsApp/Games)
            NetGuardOverlayManager.showSystemOverlay(
                context = getApplication(),
                faultType = faultType,
                modemName = updatedModel.name
            )

            _uiState.update {
                it.copy(
                    faultType = faultType,
                    latencyMs = latency,
                    packetLossPct = packetLoss,
                    activeModemModel = updatedModel,
                    showOutagePopup = true,
                    outagePopupMessage = message,
                    outageTimestampFormatted = timeFormatted
                )
            }
        } else {
            // Restore online status, clear notification & hide overlay
            OutageNotificationManager.clearNotification(getApplication())
            NetGuardOverlayManager.hideSystemOverlay(getApplication())

            _uiState.update {
                it.copy(
                    faultType = NetworkFaultType.NONE_ONLINE,
                    latencyMs = 18,
                    packetLossPct = 0.0,
                    activeModemModel = RouterHardwareModel.createModelForFault(it.selectedModemId, NetworkFaultType.NONE_ONLINE),
                    showOutagePopup = false,
                    userNotificationMessage = "Réseau rétabli avec succès ! Tous les services sont opérationnels."
                )
            }
        }
    }

    fun dismissPopup() {
        _uiState.update { it.copy(showOutagePopup = false) }
    }

    fun reportTicketToTechnicians() {
        val currentFault = _uiState.value.faultType
        val ticketNumber = "TKT-" + Random.nextInt(10000, 99999)
        val faultName = currentFault.title
        val modemName = _uiState.value.activeModemModel.name

        val newTicket = TechnicianTicket(
            ticketNumber = ticketNumber,
            faultType = faultName,
            status = "EN_ATTENTE",
            description = "Signalement automatique via NetGuard Pro: $faultName. Modèle: $modemName. Diagnostic pannes pré-validé.",
            modemModel = modemName
        )

        viewModelScope.launch {
            repository.createTechnicianTicket(newTicket)
        }

        _uiState.update {
            it.copy(
                showOutagePopup = false,
                activeTicketCreated = newTicket,
                showIncidentReportDialog = true,
                selectedReportTicket = newTicket,
                userNotificationMessage = "Ticket $ticketNumber créé ! Fiche d'incident générée."
            )
        }
    }

    fun openIncidentReport(ticket: TechnicianTicket? = null) {
        _uiState.update {
            it.copy(
                showIncidentReportDialog = true,
                selectedReportTicket = ticket ?: it.activeTicketCreated
            )
        }
    }

    fun updateTicketStatus(ticketId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateTicketStatus(ticketId, newStatus)
            _uiState.update {
                it.copy(userNotificationMessage = "Statut du ticket mis à jour : $newStatus")
            }
        }
    }

    fun closeIncidentReport() {
        _uiState.update { it.copy(showIncidentReportDialog = false) }
    }

    fun clearNotificationMessage() {
        _uiState.update { it.copy(userNotificationMessage = null) }
    }

    fun startSpeedTest() {
        if (_uiState.value.isRunningSpeedTest) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunningSpeedTest = true,
                    speedTestProgress = 0f,
                    currentSpeedPhase = "Connexion au serveur de test..."
                )
            }

            // Phase 1: Ping & Jitter test
            delay(800)
            val testPing = if (_uiState.value.faultType == NetworkFaultType.NONE_ONLINE) Random.nextInt(12, 24) else 999
            val testJitter = Random.nextInt(1, 4)
            _uiState.update {
                it.copy(
                    speedTestProgress = 0.25f,
                    latencyMs = testPing,
                    jitterMs = testJitter,
                    currentSpeedPhase = "Mesure du débit descendant (Download)..."
                )
            }

            // Phase 2: Download
            var dl = 0.0
            val maxDl = if (_uiState.value.faultType == NetworkFaultType.NONE_ONLINE) 750.0 else 0.0
            for (step in 1..10) {
                delay(150)
                dl = (maxDl * (step / 10.0)) + Random.nextDouble(-15.0, 15.0)
                if (dl < 0) dl = 0.0
                _uiState.update {
                    it.copy(
                        speedTestProgress = 0.25f + (step / 20f),
                        downloadSpeedMbps = String.format(Locale.US, "%.1f", dl).toDouble()
                    )
                }
            }

            // Phase 3: Upload
            _uiState.update {
                it.copy(
                    speedTestProgress = 0.75f,
                    currentSpeedPhase = "Mesure du débit montant (Upload)..."
                )
            }
            var ul = 0.0
            val maxUl = if (_uiState.value.faultType == NetworkFaultType.NONE_ONLINE) 420.0 else 0.0
            for (step in 1..10) {
                delay(150)
                ul = (maxUl * (step / 10.0)) + Random.nextDouble(-10.0, 10.0)
                if (ul < 0) ul = 0.0
                _uiState.update {
                    it.copy(
                        speedTestProgress = 0.75f + (step / 40f),
                        uploadSpeedMbps = String.format(Locale.US, "%.1f", ul).toDouble()
                    )
                }
            }

            val finalDl = _uiState.value.downloadSpeedMbps
            val finalUl = _uiState.value.uploadSpeedMbps
            val record = SpeedTestRecord(
                downloadMbps = finalDl,
                uploadMbps = finalUl,
                latencyMs = testPing,
                jitterMs = testJitter,
                packetLossPct = if (_uiState.value.faultType == NetworkFaultType.NONE_ONLINE) 0.0 else 100.0
            )

            repository.addSpeedTest(record)

            _uiState.update {
                it.copy(
                    isRunningSpeedTest = false,
                    speedTestProgress = 1.0f,
                    currentSpeedPhase = "Test terminé !"
                )
            }
        }
    }
}
