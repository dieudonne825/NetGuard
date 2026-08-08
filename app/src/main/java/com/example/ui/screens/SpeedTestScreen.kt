package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpeedTestRecord
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeedTestScreen(
    uiState: NetworkUiState,
    history: List<SpeedTestRecord>,
    onStartSpeedTest: () -> Unit,
    onSelectServer: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showServerBottomSheet by remember { mutableStateOf(false) }

    val availableServers = listOf(
        "CAMTEL Datacenter (Douala, CM)" to "Fibre Optique Backbone (14ms)",
        "Orange IPX Gateway (Yaoundé, CM)" to "Tier-1 Carrier Node (16ms)",
        "MTN Cloud IXP (Douala, CM)" to "Node B Broadband (18ms)",
        "Canalbox Giga Server (Douala, CM)" to "FTTH Local Peering (15ms)",
        "MainOne Subsea Gateway (Kribi, CM)" to "International Cable Landing (22ms)"
    )

    // Animated Speed Value
    val displaySpeed = when (uiState.speedTestStage) {
        "DOWNLOAD" -> uiState.downloadSpeedMbps
        "UPLOAD" -> uiState.uploadSpeedMbps
        "FINISHED" -> uiState.downloadSpeedMbps
        else -> 0.0
    }

    val animatedSpeed by animateFloatAsState(
        targetValue = displaySpeed.toFloat(),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "animatedSpeed"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("speed_test_screen")
    ) {
        // ── TOP BAR TITLE ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = NetPrimaryCyan,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SPEEDTEST® NETGUARD",
                        color = NetTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Test de débit ultra-précis & qualité de réseau",
                        color = NetTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Connection Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(NetSecondaryBlue.copy(alpha = 0.2f))
                    .border(1.dp, NetSecondaryBlue, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (uiState.isLiveMode) "Wi-Fi 5 GHz" else "Fibre FTTH",
                    color = NetPrimaryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── SERVER SELECTOR CARD ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showServerBottomSheet = true },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(NetPrimaryCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = NetPrimaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SERVEUR DE TEST",
                            color = NetTextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.selectedSpeedTestServer,
                            color = NetTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Changer Serveur",
                    tint = NetTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── MAIN SPEEDTEST GAUGE / GO BUTTON CONTAINER ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.isRunningSpeedTest && uiState.speedTestStage == "IDLE") {
                    // ════════════════════════════════════════════════════════
                    // STATE 1: ICONIC GO BUTTON (SPEEDTEST.NET STYLE)
                    // ════════════════════════════════════════════════════════
                    val infiniteTransition = rememberInfiniteTransition(label = "pulsingGo")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    val glowAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "glowAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .testTag("start_speed_test_go_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Pulsing Glow Outer Ring
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        NetPrimaryCyan.copy(alpha = glowAlpha),
                                        NetSecondaryBlue.copy(alpha = glowAlpha * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                radius = (size.width / 2) * pulseScale
                            )
                        }

                        // Giant Interactive GO Button
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(NetPrimaryCyan, NetSecondaryBlue)
                                    )
                                )
                                .border(3.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .clickable { onStartSpeedTest() },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "GO",
                                    color = Color.White,
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = "TEST DÉBIT",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Appuyez sur 'GO' pour mesurer la bande passante réelle",
                        color = NetTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                } else {
                    // ════════════════════════════════════════════════════════
                    // STATE 2: LIVE SPEEDOMETER GAUGE & WAVEFORM GRAPH
                    // ════════════════════════════════════════════════════════
                    Box(
                        modifier = Modifier.size(230.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val strokeWidth = 20f
                            val radius = (w / 2) - strokeWidth

                            // Background Arch (270 degrees)
                            drawArc(
                                color = NetCardBorder,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                topLeft = Offset(strokeWidth, strokeWidth),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Ticks around dial
                            val numTicks = 20
                            for (i in 0..numTicks) {
                                val tickAngle = 135f + (i * (270f / numTicks))
                                val tickRad = Math.toRadians(tickAngle.toDouble())
                                val innerR = radius - 12f
                                val outerR = radius + 6f
                                val startX = (w / 2) + (innerR * cos(tickRad)).toFloat()
                                val startY = (h / 2) + (innerR * sin(tickRad)).toFloat()
                                val endX = (w / 2) + (outerR * cos(tickRad)).toFloat()
                                val endY = (h / 2) + (outerR * sin(tickRad)).toFloat()

                                drawLine(
                                    color = if (i % 5 == 0) NetPrimaryCyan else NetTextMuted.copy(alpha = 0.4f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = if (i % 5 == 0) 4f else 2f
                                )
                            }

                            // Dynamic Active Speed Arc
                            val currentVal = animatedSpeed.coerceIn(0f, 1000f)
                            val sweepAngle = (currentVal / 1000f) * 270f

                            val arcColors = when (uiState.speedTestStage) {
                                "DOWNLOAD" -> listOf(NetSecondaryBlue, NetPrimaryCyan, NetOnlineGreen)
                                "UPLOAD" -> listOf(NetPrimaryCyan, NetWarningAmber, NetOnlineGreen)
                                else -> listOf(NetSecondaryBlue, NetPrimaryCyan)
                            }

                            drawArc(
                                brush = Brush.sweepGradient(colors = arcColors),
                                startAngle = 135f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(strokeWidth, strokeWidth),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Animated Needle
                            val angleRad = Math.toRadians((135 + sweepAngle).toDouble())
                            val needleLen = radius * 0.78f
                            val endX = (w / 2) + (needleLen * cos(angleRad)).toFloat()
                            val endY = (h / 2) + (needleLen * sin(angleRad)).toFloat()

                            drawLine(
                                color = NetPrimaryCyan,
                                start = Offset(w / 2, h / 2),
                                end = Offset(endX, endY),
                                strokeWidth = 7f,
                                cap = StrokeCap.Round
                            )

                            drawCircle(color = NetPrimaryCyan, radius = 10f, center = Offset(w / 2, h / 2))
                            drawCircle(color = Color.White, radius = 4f, center = Offset(w / 2, h / 2))
                        }

                        // Center Speed Text Readout
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 45.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", animatedSpeed),
                                color = NetTextPrimary,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Mbps",
                                color = NetPrimaryCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Waveform Speed Chart during test
                    if (uiState.speedTestSamples.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NetDarkBackground.copy(alpha = 0.5f))
                                .padding(4.dp)
                        ) {
                            val samples = uiState.speedTestSamples
                            val maxSample = (samples.maxOrNull() ?: 1f).coerceAtLeast(10f)

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = Path()
                                val stepX = size.width / (samples.size - 1).coerceAtLeast(1)

                                samples.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = size.height - ((value / maxSample) * size.height)
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = if (uiState.speedTestStage == "DOWNLOAD") NetOnlineGreen else NetPrimaryCyan,
                                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Status Phase & Progress
                    Text(
                        text = uiState.currentSpeedPhase,
                        color = NetTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.isRunningSpeedTest) {
                        LinearProgressIndicator(
                            progress = { uiState.speedTestProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = NetPrimaryCyan,
                            trackColor = NetCardBorder
                        )
                    } else {
                        // Re-test button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NetPrimaryCyan)
                                .clickable { onStartSpeedTest() }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RÉESSAYER LE TEST",
                                    color = Color.Black,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ════════════════════════════════════════════════════════
                // METRICS DASHBOARD ROW (PING, DOWNLOAD, UPLOAD, LOSS)
                // ════════════════════════════════════════════════════════
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NetDarkBackground.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .border(1.dp, NetCardBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // PING / LATENCE
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PING", color = NetTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${uiState.latencyMs}",
                                color = if (uiState.latencyMs < 30) NetOnlineGreen else NetWarningAmber,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(" ms", color = NetTextSecondary, fontSize = 10.sp)
                        }
                        Text("Jitter ${uiState.jitterMs}ms", color = NetTextMuted, fontSize = 9.sp)
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(38.dp)
                            .background(NetCardBorder)
                    )

                    // DOWNLOAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = NetOnlineGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("DOWNLOAD", color = NetTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.downloadSpeedMbps),
                                color = NetOnlineGreen,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(" Mbps", color = NetTextSecondary, fontSize = 10.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(38.dp)
                            .background(NetCardBorder)
                    )

                    // UPLOAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = NetPrimaryCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("UPLOAD", color = NetTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", uiState.uploadSpeedMbps),
                                color = NetPrimaryCyan,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(" Mbps", color = NetTextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── APPLICATION QUALITY SCORES (GAMING, STREAMING, VISIO) ──
        Text(
            text = "Analyse de la Qualité par Usage :",
            color = NetTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gaming Score
            QualityScoreCard(
                icon = Icons.Default.Gamepad,
                title = "Gaming",
                score = if (uiState.latencyMs < 25) "98/100" else "65/100",
                status = if (uiState.latencyMs < 25) "Ultra Basse Latence" else "Ping Moyen",
                color = if (uiState.latencyMs < 25) NetOnlineGreen else NetWarningAmber,
                modifier = Modifier.weight(1f)
            )

            // Streaming 4K Score
            QualityScoreCard(
                icon = Icons.Default.Tv,
                title = "Streaming 4K",
                score = if (uiState.downloadSpeedMbps > 25.0) "100/100" else "40/100",
                status = if (uiState.downloadSpeedMbps > 25.0) "Fluide 8K HDR" else "Buffering possible",
                color = if (uiState.downloadSpeedMbps > 25.0) NetOnlineGreen else NetAlertRed,
                modifier = Modifier.weight(1f)
            )

            // Visio Call Score
            QualityScoreCard(
                icon = Icons.Default.Videocam,
                title = "Visio / Zoom",
                score = if (uiState.uploadSpeedMbps > 10.0 && uiState.jitterMs < 5) "96/100" else "70/100",
                status = if (uiState.uploadSpeedMbps > 10.0) "HD Sans Coupures" else "Standard",
                color = NetPrimaryCyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── HISTORY SECTION ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NetTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Historique des Mesures (${history.size})",
                    color = NetTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (history.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aucun test enregistré pour l'instant. Lancez un test de débit pour alimenter votre journal.",
                    color = NetTextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                history.take(6).forEach { record ->
                    SpeedTestHistoryItemCard(record = record)
                }
            }
        }
    }

    // ── MODAL BOTTOM SHEET: SERVER SELECTION ──
    if (showServerBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showServerBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = NetCardSurface,
            contentColor = NetTextPrimary
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Changer le Serveur de Test Speedtest",
                    color = NetTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sélectionnez le nœud réseau d'opérateur le plus proche de votre emplacement",
                    color = NetTextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                availableServers.forEach { (serverName, detail) ->
                    val isSelected = uiState.selectedSpeedTestServer == serverName

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NetPrimaryCyan.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                onSelectServer(serverName)
                                showServerBottomSheet = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = serverName,
                                color = if (isSelected) NetPrimaryCyan else NetTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = detail,
                                color = NetTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onSelectServer(serverName)
                                showServerBottomSheet = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = NetPrimaryCyan,
                                unselectedColor = NetTextMuted
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun QualityScoreCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    score: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NetCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = NetTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = score,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = status,
                color = NetTextMuted,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SpeedTestHistoryItemCard(record: SpeedTestRecord) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NetCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = dateStr, color = NetTextMuted, fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "↓ ${String.format(Locale.US, "%.1f", record.downloadMbps)} Mbps",
                        color = NetOnlineGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "↑ ${String.format(Locale.US, "%.1f", record.uploadMbps)} Mbps",
                        color = NetPrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.latencyMs} ms",
                    color = if (record.latencyMs < 50) NetOnlineGreen else NetAlertRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                Text(text = record.connectionType, color = NetTextSecondary, fontSize = 10.sp)
            }
        }
    }
}
