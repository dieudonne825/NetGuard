package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpeedTestScreen(
    uiState: NetworkUiState,
    history: List<SpeedTestRecord>,
    onStartSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedDlSpeed by animateFloatAsState(
        targetValue = uiState.downloadSpeedMbps.toFloat(),
        animationSpec = tween(300),
        label = "animatedDlSpeed"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetDarkBackground)
            .padding(16.dp)
            .testTag("speed_test_screen")
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = NetPrimaryCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Test de Débit & Latence",
                    color = NetTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Mesurez la bande passante exacte et la qualité de ligne",
                    color = NetTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Speedometer Gauge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gauge Canvas
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val strokeWidth = 18f
                        val radius = (w / 2) - strokeWidth

                        // Arc background
                        drawArc(
                            color = NetCardBorder,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = Offset(strokeWidth, strokeWidth),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Filled Arc
                        val currentVal = animatedDlSpeed.coerceIn(0f, 1000f)
                        val sweepAngle = (currentVal / 1000f) * 270f

                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(NetSecondaryBlue, NetPrimaryCyan, NetOnlineGreen)
                            ),
                            startAngle = 135f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(strokeWidth, strokeWidth),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Needle
                        val angleRad = Math.toRadians((135 + sweepAngle).toDouble())
                        val needleLen = radius * 0.75f
                        val endX = (w / 2) + (needleLen * cos(angleRad)).toFloat()
                        val endY = (h / 2) + (needleLen * sin(angleRad)).toFloat()

                        drawLine(
                            color = NetPrimaryCyan,
                            start = Offset(w / 2, h / 2),
                            end = Offset(endX, endY),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        drawCircle(color = NetPrimaryCyan, radius = 8f, center = Offset(w / 2, h / 2))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 40.dp)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", animatedDlSpeed),
                            color = NetTextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Mbps",
                            color = NetPrimaryCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Phase Progress Text
                Text(
                    text = uiState.currentSpeedPhase,
                    color = NetTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                if (uiState.isRunningSpeedTest) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { uiState.speedTestProgress },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        color = NetPrimaryCyan,
                        trackColor = NetCardBorder,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Download & Upload Breakdown Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "DOWNLOAD", color = NetTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.downloadSpeedMbps} Mbps",
                            color = NetOnlineGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(NetCardBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "UPLOAD", color = NetTextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.uploadSpeedMbps} Mbps",
                            color = NetPrimaryCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button
                Button(
                    onClick = onStartSpeedTest,
                    enabled = !uiState.isRunningSpeedTest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("start_speed_test_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetPrimaryCyan,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isRunningSpeedTest) "Test en cours..." else "Lancer le Test de Débit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // History Title
        Text(
            text = "Historique des Tests de Débit :",
            color = NetTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (history.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Aucun test de débit enregistré pour le moment. Cliquez sur 'Lancer le Test de Débit' ci-dessus.",
                    color = NetTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(history) { record ->
                    SpeedTestHistoryItemCard(record = record)
                }
            }
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
                Text(text = dateStr, color = NetTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "↓ ${record.downloadMbps} Mbps",
                        color = NetOnlineGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "↑ ${record.uploadMbps} Mbps",
                        color = NetPrimaryCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.latencyMs} ms",
                    color = if (record.latencyMs < 50) NetOnlineGreen else NetAlertRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(text = record.connectionType, color = NetTextSecondary, fontSize = 10.sp)
            }
        }
    }
}
