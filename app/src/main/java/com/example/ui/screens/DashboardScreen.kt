package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.model.ConnectivityState
import com.example.model.toConnectivityState
import com.example.ui.components.HeartbeatIndicator
import com.example.util.NetGuardOverlayManager
import com.example.util.OutageNotificationManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkFaultType
import com.example.ui.NetworkUiState
import com.example.ui.theme.NetAlertRed
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetPrimaryCyan
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary
import com.example.ui.theme.NetWarningAmber

private data class StatusCardConfig(
    val color: Color,
    val bg: Brush,
    val headerText: String,
    val subtitleText: String
)

@Composable
fun DashboardScreen(
    uiState: NetworkUiState,
    onNavigateToSpeedTest: () -> Unit,
    onNavigateToModem3D: () -> Unit,
    onReportTechnician: () -> Unit,
    onSimulateFault: (NetworkFaultType) -> Unit = {},
    onOpenIncidentReport: () -> Unit = {},
    onToggleLiveMode: (Boolean) -> Unit = {},
    onOpenRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        val profile = uiState.clientProfile

        // Main Real-Time Status Card with Heartbeat Indicator
        val connectivityState = uiState.faultType.toConnectivityState()
        val cardConfig = when (connectivityState) {
            ConnectivityState.HEALTHY -> StatusCardConfig(
                color = NetOnlineGreen,
                bg = Brush.linearGradient(listOf(Color(0xFFF0FFF4), Color(0xFFDCFCE7))),
                headerText = "OPÉRATIONNEL",
                subtitleText = "Votre connexion est parfaite."
            )
            ConnectivityState.DEGRADED -> StatusCardConfig(
                color = NetWarningAmber,
                bg = Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))),
                headerText = "DÉGRADÉ",
                subtitleText = "Signal ou routage perturbé."
            )
            ConnectivityState.WIFI_LOSS -> StatusCardConfig(
                color = NetWarningAmber,
                bg = Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))),
                headerText = "WI-FI PERDU",
                subtitleText = "Interférences radio détectées."
            )
            ConnectivityState.TOTAL_LOSS -> StatusCardConfig(
                color = NetAlertRed,
                bg = Brush.linearGradient(listOf(Color(0xFFFEF2F2), Color(0xFFFEE2E2))),
                headerText = "PANNE TOTALE",
                subtitleText = "Aucun accès Internet."
            )
        }
        val statusColor = cardConfig.color
        val statusBg = cardConfig.bg
        val headerText = cardConfig.headerText
        val subtitleText = cardConfig.subtitleText

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(statusBg)
                .border(1.5.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Informations texte à gauche
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.faultType.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NetTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitleText,
                            fontSize = 13.sp,
                            color = NetTextSecondary
                        )
                    }

                    // Composant Heartbeat animé à droite
                    HeartbeatIndicator(
                        state = connectivityState,
                        sizeDp = 48.dp
                    )
                }

                if (uiState.faultType != NetworkFaultType.NONE_ONLINE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NetCardSurface.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔍 Diagnostic Matériel (${uiState.faultType.severity}) :",
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.faultType.recommendedFix,
                                color = NetTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Latency & Quality Metrics Grid
        Text(
            text = "Métriques de Latence & Stabilité :",
            color = NetTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricMetricCard(
                title = "Latence (Ping)",
                value = if (uiState.faultType == NetworkFaultType.NONE_ONLINE) "${uiState.latencyMs} ms" else "ÉCHEC",
                color = if (uiState.faultType == NetworkFaultType.NONE_ONLINE) NetPrimaryCyan else NetAlertRed,
                modifier = Modifier.weight(1f)
            )
            MetricMetricCard(
                title = "Gigotage (Jitter)",
                value = if (uiState.faultType == NetworkFaultType.NONE_ONLINE) "${uiState.jitterMs} ms" else "--",
                color = NetSecondaryBlue,
                modifier = Modifier.weight(1f)
            )
            MetricMetricCard(
                title = "Perte Paquets",
                value = "${uiState.packetLossPct.toInt()}%",
                color = if (uiState.packetLossPct > 0) NetAlertRed else NetOnlineGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Ping History Line Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Graphique Temps Réel de Latence (ms)",
                        color = NetTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MAJ 3s",
                        color = NetPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Ping Line Chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val pings = uiState.pingHistory
                    if (pings.isNotEmpty()) {
                        val maxPing = (pings.maxOrNull() ?: 50).coerceAtLeast(40).toFloat()
                        val w = size.width
                        val h = size.height

                        val stepX = w / (pings.size - 1).coerceAtLeast(1)

                        val path = Path()
                        pings.forEachIndexed { index, pingVal ->
                            val x = index * stepX
                            val y = h - ((pingVal / maxPing) * h * 0.8f)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        // Gradient fill under line
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NetPrimaryCyan.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )

                        // Line
                        drawPath(
                            path = path,
                            color = NetPrimaryCyan,
                            style = Stroke(width = 3.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speed Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dernier Débit Mesuré",
                        color = NetTextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↓ ${uiState.downloadSpeedMbps} Mbps",
                            color = NetOnlineGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "↑ ${uiState.uploadSpeedMbps} Mbps",
                            color = NetPrimaryCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Button(
                    onClick = onNavigateToSpeedTest,
                    colors = ButtonDefaults.buttonColors(containerColor = NetPrimaryCyan, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("launch_speed_test_button")
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Tester", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions Row
        Text(
            text = "Actions Rapides :",
            color = NetTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToModem3D,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("nav_modem_3d_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NetCardSurface,
                    contentColor = NetTextPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Router, contentDescription = null, tint = NetPrimaryCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Voyants 3D", color = NetTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onOpenIncidentReport,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("nav_export_report_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = NetCardSurface,
                    contentColor = NetPrimaryCyan
                )
            ) {
                Text(text = "📄 Fiche FAI", color = NetPrimaryCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onReportTechnician,
                modifier = Modifier
                    .weight(1.1f)
                    .height(50.dp)
                    .testTag("nav_report_technician_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NetSecondaryBlue, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Signaler", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Django Admin Telemetry & Client Profile Card Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (uiState.isRegisteredWithDjango) NetPrimaryCyan.copy(alpha = 0.6f) else NetWarningAmber.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isRegisteredWithDjango) NetPrimaryCyan.copy(alpha = 0.15f) else NetWarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isRegisteredWithDjango) Icons.Default.CloudDone else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (uiState.isRegisteredWithDjango) NetPrimaryCyan else NetWarningAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.clientName?.ifBlank { "Abonné Non Enregistré" } ?: "Enregistrement Requis",
                                color = NetTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (profile != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NetPrimaryCyan.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = profile.clientCode,
                                        color = NetPrimaryCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (uiState.isRegisteredWithDjango) {
                                "${profile?.city} (${profile?.neighborhood}) • ${uiState.lastDjangoHeartbeatStatus}"
                            } else "Inscrivez-vous pour la supervision Django Admin",
                            color = NetTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onOpenRegistration,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = NetPrimaryCyan.copy(alpha = 0.15f),
                        contentColor = NetPrimaryCyan
                    ),
                    modifier = Modifier.testTag("open_django_registration_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = NetPrimaryCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isRegisteredWithDjango) "Profil" else "S'inscrire",
                        color = NetPrimaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fault Simulation Suite Section (Moved to Bottom)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = NetPrimaryCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulateur de Pannes Réseau (9 Scénarios)",
                            color = NetTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NetworkFaultType.values().forEach { fault ->
                        val isSelected = uiState.faultType == fault
                        val chipColor = when(fault) {
                            NetworkFaultType.NONE_ONLINE -> NetOnlineGreen
                            NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> NetAlertRed
                            else -> NetWarningAmber
                        }

                        Surface(
                            onClick = { onSimulateFault(fault) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) chipColor.copy(alpha = 0.25f) else NetDarkBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) chipColor else NetCardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim_fault_chip_${fault.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(chipColor)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fault.title,
                                            color = NetTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${fault.category} • ${fault.severity}",
                                            color = NetTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(chipColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isSelected) "ACTIF" else "TESTER",
                                        color = chipColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricMetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NetCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = NetTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = color,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
