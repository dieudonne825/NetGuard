package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.LedIndicatorInfo
import com.example.model.LedState
import com.example.model.NetworkFaultType
import com.example.model.RouterHardwareModel
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Modem3DVisualScreen(
    uiState: NetworkUiState,
    onSelectModem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modemList = listOf(
        "livebox" to "Livebox (Orange)",
        "freebox" to "Freebox Ultra (Free)",
        "bbox" to "Bbox Wi-Fi 6 (Bouygues)",
        "sfrbox" to "SFR Box 8 (SFR)",
        "netguard" to "NetGuard Gateway Pro"
    )

    var viewMode by remember { mutableStateOf(0) } // 0 = 3D Face Avant, 1 = 3D Face Arrière (Ports), 2 = Photo Studio HD
    var rotationY by remember { mutableFloatStateOf(0f) } // -45 deg to +45 deg
    var rotationX by remember { mutableFloatStateOf(0f) } // tilt

    var inspectedLed by remember { mutableStateOf<LedIndicatorInfo?>(null) }
    var selectedPortInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    var isDiagnosticTesting by remember { mutableStateOf(false) }
    var diagnosticProgress by remember { mutableFloatStateOf(0f) }
    var testStepText by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val modemModel = uiState.activeModemModel

    val infiniteTransition = rememberInfiniteTransition(label = "led_blink")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinkAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("modem_3d_visual_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Router,
                contentDescription = null,
                tint = NetSecondaryBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Modèle Visuel & Voyants Modem 3D",
                    color = NetTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Inspection dynamique interactive 360°, voyants et ports de connexion",
                    color = NetTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preload Status Banner if fault exists
        if (uiState.faultType != NetworkFaultType.NONE_ONLINE) {
            val bannerColor = when (uiState.faultType) {
                NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> NetAlertRed
                else -> NetWarningAmber
            }
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = bannerColor.copy(alpha = 0.12f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    bannerColor
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Diagnostic Matériel : " + uiState.faultType.title,
                            color = NetTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.faultType.recommendedFix,
                            color = NetTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Router Selection Filter Chips
        Text(
            text = "Équipement FAI Sélectionné :",
            color = NetTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(modemList) { (id, label) ->
                val isSelected = id == uiState.selectedModemId
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectModem(id) },
                    label = { Text(text = label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NetSecondaryBlue,
                        selectedLabelColor = Color.White,
                        containerColor = NetCardSurface,
                        labelColor = NetTextSecondary
                    ),
                    modifier = Modifier.testTag("modem_chip_$id")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // View Mode Selector Tabs
        TabRow(
            selectedTabIndex = viewMode,
            containerColor = NetCardSurface,
            contentColor = NetSecondaryBlue,
            indicator = { tabPositions ->
                if (viewMode < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[viewMode]),
                        color = NetSecondaryBlue
                    )
                }
            }
        ) {
            Tab(
                selected = viewMode == 0,
                onClick = { viewMode = 0 },
                modifier = Modifier.testTag("tab_3d_front")
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "3D Face Avant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(
                selected = viewMode == 1,
                onClick = { viewMode = 1 },
                modifier = Modifier.testTag("tab_3d_back")
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsInputComponent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Face Arrière (Ports)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(
                selected = viewMode == 2,
                onClick = { viewMode = 2 },
                modifier = Modifier.testTag("tab_photo_hd")
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Studio HD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Visual Display Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotationY = (rotationY + dragAmount.x * 0.4f).coerceIn(-60f, 60f)
                            rotationX = (rotationX + dragAmount.y * 0.2f).coerceIn(-30f, 30f)
                        }
                    }
            ) {
                when (viewMode) {
                    0 -> {
                        // 3D Procedural Front View Canvas with Rotation
                        Canvas3DFrontView(
                            modemModel = modemModel,
                            rotationY = rotationY,
                            rotationX = rotationX,
                            blinkAlpha = blinkAlpha,
                            onLedClick = { led -> inspectedLed = led }
                        )
                    }
                    1 -> {
                        // 3D Back View Canvas (Ports & Cable Connectors)
                        Canvas3DBackView(
                            modemModel = modemModel,
                            rotationY = rotationY,
                            rotationX = rotationX,
                            uiState = uiState,
                            onPortClick = { portName, details ->
                                selectedPortInfo = portName to details
                            }
                        )
                    }
                    2 -> {
                        // High Definition Realistic Photo Studio View
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.modem_visual_1785733624053),
                                contentDescription = "Modem Visual HD Studio",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status Overlay Badges on HD Photo
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (uiState.faultType == NetworkFaultType.NONE_ONLINE) NetOnlineGreen
                                                else if (uiState.faultType == NetworkFaultType.ISP_OUTAGE) NetAlertRed
                                                else NetWarningAmber
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = modemModel.name + " • HD Studio",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Interactive Hotspot Pins
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.60f))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "💡 Glissez avec le doigt pour modifier l'angle de vue 3D",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Top Floating Badge displaying equipment name
                Box(
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NetDarkBackground.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = modemModel.provider + " " + modemModel.name,
                        color = NetTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3D Rotation Controls Bar (If in 3D Canvas views)
        if (viewMode != 2) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.RotateRight, contentDescription = null, tint = NetSecondaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Angle 3D :", color = NetTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = rotationY,
                        onValueChange = { rotationY = it },
                        valueRange = -60f..60f,
                        colors = SliderDefaults.colors(
                            thumbColor = NetSecondaryBlue,
                            activeTrackColor = NetSecondaryBlue
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            rotationY = 0f
                            rotationX = 0f
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(text = "Reset Angle", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Test / Reboot Button Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Auto-Test Diagnostic & Pings Voyants",
                            color = NetTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Simule la réinitialisation matérielle et le test optique",
                            color = NetTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isDiagnosticTesting = true
                                diagnosticProgress = 0.1f
                                testStepText = "1/3 : Test alimentation et condensateurs..."
                                delay(900)
                                diagnosticProgress = 0.5f
                                testStepText = "2/3 : Vérification signal optique WAN / SFP..."
                                delay(900)
                                diagnosticProgress = 0.85f
                                testStepText = "3/3 : Synchronisation pings DNS & Wi-Fi..."
                                delay(800)
                                diagnosticProgress = 1.0f
                                testStepText = "Test terminé avec succès. Tous les voyants sont valides."
                                delay(1200)
                                isDiagnosticTesting = false
                            }
                        },
                        enabled = !isDiagnosticTesting,
                        colors = ButtonDefaults.buttonColors(containerColor = NetSecondaryBlue, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isDiagnosticTesting) "Test..." else "Lancer Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isDiagnosticTesting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = testStepText, color = NetSecondaryBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { diagnosticProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = NetSecondaryBlue,
                        trackColor = NetCardBorder,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Port Details Card (if clicked in Back View)
        selectedPortInfo?.let { (portTitle, portDetails) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetSecondaryBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SettingsInputComponent, contentDescription = null, tint = NetSecondaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Port Sélectionné : $portTitle",
                            color = NetTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = portDetails, color = NetTextSecondary, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Detailed Voyants Inspection Section
        Text(
            text = "États Détaillés des Voyants & Diagnostic :",
            color = NetTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        val allLeds = listOf(
            modemModel.powerLed,
            modemModel.wanLed,
            modemModel.internetLed,
            modemModel.wifiLed,
            modemModel.lanLed
        )

        allLeds.forEach { led ->
            LedDetailCard(
                led = led,
                blinkAlpha = blinkAlpha,
                isSelected = inspectedLed?.name == led.name,
                onClick = { inspectedLed = led }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Inspection Detail Card
        inspectedLed?.let { led ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetSecondaryBlue),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("led_inspector_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NetSecondaryBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guide de Résolution : " + led.name,
                            color = NetTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "État Actuel : " + led.state.label,
                        color = led.state.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fonction : " + led.description,
                        color = NetTextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Action Recommandée : " + led.diagnosticHelp,
                        color = NetSecondaryBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun Canvas3DFrontView(
    modemModel: RouterHardwareModel,
    rotationY: Float,
    rotationX: Float,
    blinkAlpha: Float,
    onLedClick: (LedIndicatorInfo) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val rotYNorm = rotationY / 60f
            val rotXNorm = rotationX / 30f

            val rotYOffset = rotYNorm * (w * 0.14f)
            val rotXOffset = rotXNorm * (h * 0.10f)

            // Depth extrusion thickness (3D Volume)
            val depthX = rotYNorm * 28f
            val depthY = 16f - rotXNorm * 12f

            // 1. Cyber Ambient Backlight Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(NetSecondaryBlue.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w / 2 + rotYOffset * 0.5f, h * 0.5f),
                    radius = w * 0.45f
                ),
                center = Offset(w / 2 + rotYOffset * 0.5f, h * 0.5f),
                radius = w * 0.45f
            )

            // 2. High-Tech Perspective Tech Grid
            for (i in 0..10) {
                val gridX = w * (i / 10f)
                drawLine(
                    color = NetCardBorder.copy(alpha = 0.25f),
                    start = Offset(w / 2 + rotYOffset * 0.4f, h * 0.18f),
                    end = Offset(gridX, h),
                    strokeWidth = 1f
                )
            }

            // 3. Router Main Body Front Face Vertices
            val pTopLeft = Offset(w * 0.14f + rotYOffset, h * 0.32f + rotXOffset)
            val pTopRight = Offset(w * 0.86f + rotYOffset, h * 0.28f - rotXOffset)
            val pBottomRight = Offset(w * 0.89f + rotYOffset, h * 0.74f - rotXOffset)
            val pBottomLeft = Offset(w * 0.11f + rotYOffset, h * 0.78f + rotXOffset)

            // 4. Ground Drop Shadow (Soft Gaussian Blur simulation)
            val shadowPath = Path().apply {
                moveTo(pBottomLeft.x - 25f, pBottomLeft.y + 12f)
                lineTo(pBottomRight.x + 25f, pBottomRight.y + 12f)
                lineTo(pBottomRight.x + 40f + depthX, pBottomRight.y + 45f + depthY)
                lineTo(pBottomLeft.x - 40f + depthX, pBottomLeft.y + 45f + depthY)
                close()
            }
            drawPath(
                path = shadowPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                )
            )

            // 5. 3D Side Depth Face (Extrusion on Left or Right depending on angle)
            if (rotationY < 0) {
                // Right Side Face visible
                val rightSidePath = Path().apply {
                    moveTo(pTopRight.x, pTopRight.y)
                    lineTo(pTopRight.x + depthX, pTopRight.y - depthY)
                    lineTo(pBottomRight.x + depthX, pBottomRight.y - depthY)
                    lineTo(pBottomRight.x, pBottomRight.y)
                    close()
                }
                drawPath(
                    path = rightSidePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF030712))
                    )
                )
                drawPath(path = rightSidePath, color = NetCardBorder.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
            } else if (rotationY > 0) {
                // Left Side Face visible
                val leftSidePath = Path().apply {
                    moveTo(pTopLeft.x, pTopLeft.y)
                    lineTo(pTopLeft.x + depthX, pTopLeft.y - depthY)
                    lineTo(pBottomLeft.x + depthX, pBottomLeft.y - depthY)
                    lineTo(pBottomLeft.x, pBottomLeft.y)
                    close()
                }
                drawPath(
                    path = leftSidePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF030712))
                    )
                )
                drawPath(path = leftSidePath, color = NetCardBorder.copy(alpha = 0.4f), style = Stroke(width = 1.5f))
            }

            // 6. 3D Top Depth Face (Top Panel with Vents)
            val topSidePath = Path().apply {
                moveTo(pTopLeft.x, pTopLeft.y)
                lineTo(pTopRight.x, pTopRight.y)
                lineTo(pTopRight.x + depthX, pTopRight.y - depthY)
                lineTo(pTopLeft.x + depthX, pTopLeft.y - depthY)
                close()
            }
            drawPath(
                path = topSidePath,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF1E293B))
                )
            )
            drawPath(path = topSidePath, color = NetCardBorder.copy(alpha = 0.5f), style = Stroke(width = 1.5f))

            // Engraved Ventilation Grilles on Top Panel
            for (v in 1..6) {
                val fraction = v / 7f
                val v1 = Offset(
                    pTopLeft.x + (pTopRight.x - pTopLeft.x) * fraction,
                    pTopLeft.y + (pTopRight.y - pTopLeft.y) * fraction
                )
                val v2 = Offset(v1.x + depthX * 0.8f, v1.y - depthY * 0.8f)
                drawLine(
                    color = Color.Black.copy(alpha = 0.6f),
                    start = v1,
                    end = v2,
                    strokeWidth = 2.5f
                )
            }

            // 7. Antennas (Aerodynamic Dual High-Gain Antennas)
            val ant1Base = Offset(pTopLeft.x + (pTopRight.x - pTopLeft.x) * 0.22f, pTopLeft.y + (pTopRight.y - pTopLeft.y) * 0.22f)
            val ant2Base = Offset(pTopLeft.x + (pTopRight.x - pTopLeft.x) * 0.78f, pTopLeft.y + (pTopRight.y - pTopLeft.y) * 0.78f)
            val antHeight = h * 0.26f

            // Antenna 1
            drawLine(
                brush = Brush.verticalGradient(colors = listOf(Color(0xFF475569), Color(0xFF0F172A))),
                start = ant1Base,
                end = Offset(ant1Base.x - rotYNorm * 10f, ant1Base.y - antHeight),
                strokeWidth = 10f
            )
            // Antenna 1 Joint Collar
            drawCircle(color = NetCardBorder, radius = 8f, center = ant1Base)
            // Antenna 1 Glowing Tip
            drawCircle(
                color = NetPrimaryCyan,
                radius = 6f,
                center = Offset(ant1Base.x - rotYNorm * 10f, ant1Base.y - antHeight)
            )
            drawCircle(
                color = NetPrimaryCyan.copy(alpha = 0.4f),
                radius = 12f,
                center = Offset(ant1Base.x - rotYNorm * 10f, ant1Base.y - antHeight)
            )

            // Antenna 2
            drawLine(
                brush = Brush.verticalGradient(colors = listOf(Color(0xFF475569), Color(0xFF0F172A))),
                start = ant2Base,
                end = Offset(ant2Base.x - rotYNorm * 10f, ant2Base.y - antHeight),
                strokeWidth = 10f
            )
            // Antenna 2 Joint Collar
            drawCircle(color = NetCardBorder, radius = 8f, center = ant2Base)
            // Antenna 2 Glowing Tip
            drawCircle(
                color = NetPrimaryCyan,
                radius = 6f,
                center = Offset(ant2Base.x - rotYNorm * 10f, ant2Base.y - antHeight)
            )
            drawCircle(
                color = NetPrimaryCyan.copy(alpha = 0.4f),
                radius = 12f,
                center = Offset(ant2Base.x - rotYNorm * 10f, ant2Base.y - antHeight)
            )

            // 8. Main Front Face Body (Matte Obsidian Finish)
            val mainFrontPath = Path().apply {
                moveTo(pTopLeft.x, pTopLeft.y)
                lineTo(pTopRight.x, pTopRight.y)
                lineTo(pBottomRight.x, pBottomRight.y)
                lineTo(pBottomLeft.x, pBottomLeft.y)
                close()
            }
            drawPath(
                path = mainFrontPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                )
            )

            // Metallic Perimeter Bevel
            drawPath(
                path = mainFrontPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(NetSecondaryBlue.copy(alpha = 0.6f), NetPrimaryCyan.copy(alpha = 0.8f), NetSecondaryBlue.copy(alpha = 0.4f))
                ),
                style = Stroke(width = 2.5f)
            )

            // 9. Smoked Acrylic Glass Panel (Inset Center Section for LEDs & Brand)
            val glassTopLeft = Offset(pTopLeft.x + (pBottomLeft.x - pTopLeft.x) * 0.35f + (pTopRight.x - pTopLeft.x) * 0.05f, pTopLeft.y + (pBottomLeft.y - pTopLeft.y) * 0.35f)
            val glassTopRight = Offset(pTopRight.x + (pBottomRight.x - pTopRight.x) * 0.35f - (pTopRight.x - pTopLeft.x) * 0.05f, pTopRight.y + (pBottomRight.y - pTopRight.y) * 0.35f)
            val glassBottomRight = Offset(pBottomRight.x - (pBottomRight.x - pBottomLeft.x) * 0.05f, pBottomRight.y - (pBottomRight.y - pTopRight.y) * 0.12f)
            val glassBottomLeft = Offset(pBottomLeft.x + (pBottomRight.x - pBottomLeft.x) * 0.05f, pBottomLeft.y - (pBottomLeft.y - pTopLeft.y) * 0.12f)

            val glassPanelPath = Path().apply {
                moveTo(glassTopLeft.x, glassTopLeft.y)
                lineTo(glassTopRight.x, glassTopRight.y)
                lineTo(glassBottomRight.x, glassBottomRight.y)
                lineTo(glassBottomLeft.x, glassBottomLeft.y)
                close()
            }

            // Draw Smoked Acrylic Glass Fill
            drawPath(
                path = glassPanelPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0B132B))
                )
            )

            // Glossy Specular Light Reflection (Glass Flare)
            val reflectionPath = Path().apply {
                moveTo(glassTopLeft.x, glassTopLeft.y)
                lineTo(glassTopLeft.x + (glassTopRight.x - glassTopLeft.x) * 0.45f, glassTopLeft.y + (glassTopRight.y - glassTopLeft.y) * 0.45f)
                lineTo(glassBottomLeft.x + (glassBottomRight.x - glassBottomLeft.x) * 0.15f, glassBottomLeft.y)
                lineTo(glassBottomLeft.x, glassBottomLeft.y)
                close()
            }
            drawPath(
                path = reflectionPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)
                )
            )

            // Chrome Trim around Acrylic Glass Panel
            drawPath(
                path = glassPanelPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(NetCardBorder, NetPrimaryCyan.copy(alpha = 0.5f), NetCardBorder)
                ),
                style = Stroke(width = 1.5f)
            )

            // Metallic Brand Engraving Emblem (Top Center of Modem)
            val brandCenterX = pTopLeft.x + (pTopRight.x - pTopLeft.x) * 0.5f
            val brandCenterY = pTopLeft.y + (pBottomLeft.y - pTopLeft.y) * 0.20f
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(NetSecondaryBlue, NetPrimaryCyan)),
                topLeft = Offset(brandCenterX - 35f, brandCenterY - 6f),
                size = androidx.compose.ui.geometry.Size(70f, 12f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }

        // Overlay Interactive High-Tech LED Module Array
        Row(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leds = listOf(
                modemModel.powerLed,
                modemModel.wanLed,
                modemModel.internetLed,
                modemModel.wifiLed,
                modemModel.lanLed
            )

            leds.forEach { led ->
                LedSphereComponent(
                    led = led,
                    blinkAlpha = blinkAlpha,
                    onClick = { onLedClick(led) }
                )
            }
        }
    }
}

@Composable
fun Canvas3DBackView(
    modemModel: RouterHardwareModel,
    rotationY: Float,
    rotationX: Float,
    uiState: NetworkUiState,
    onPortClick: (String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val rotYOffset = (rotationY / 60f) * (w * 0.10f)

            // Dark Chassis Surface
            val bodyPath = Path().apply {
                moveTo(w * 0.10f + rotYOffset, h * 0.25f)
                lineTo(w * 0.90f + rotYOffset, h * 0.25f)
                lineTo(w * 0.90f + rotYOffset, h * 0.85f)
                lineTo(w * 0.10f + rotYOffset, h * 0.85f)
                close()
            }

            drawPath(
                path = bodyPath,
                brush = Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
            )
            drawPath(path = bodyPath, color = NetCardBorder, style = Stroke(width = 2f))
        }

        // Interactive Ports Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PANNEAU ARRIÈRE (PORTS & CONNECTIQUE)",
                color = NetTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fiber SFP+ / WAN Port
                BackPortItem(
                    title = "WAN (Fibre)",
                    color = if (uiState.faultType == NetworkFaultType.ISP_OUTAGE) NetAlertRed else NetOnlineGreen,
                    statusText = if (uiState.faultType == NetworkFaultType.ISP_OUTAGE) "PERTE SIGNAL" else "SYNCHRO 10G",
                    onClick = {
                        onPortClick(
                            "WAN Optique (Fibre SC/APC)",
                            "Connecteur de la jarretière optique relié au boîtier PTO/ONT. " +
                                if (uiState.faultType == NetworkFaultType.ISP_OUTAGE) "Signal d'entrée optique interrompu sur le réseau FAI."
                                else "Signal laser optimal à -19.4 dBm."
                        )
                    }
                )

                // LAN Ports 1 to 4
                BackPortItem(
                    title = "LAN 1 - 4 (RJ45)",
                    color = NetOnlineGreen,
                    statusText = "GIGABIT OK",
                    onClick = {
                        onPortClick(
                            "Ports LAN 1 à 4 (Ethernet RJ45)",
                            "Ports réseau locaux Gigabit Ethernet (1000 Mbps). À utiliser pour connecter votre ordinateur, décodeur TV ou console."
                        )
                    }
                )

                // Power Jack 12V
                BackPortItem(
                    title = "ALIM 12V DC",
                    color = if (uiState.faultType == NetworkFaultType.POWER_OUTAGE) NetWarningAmber else NetOnlineGreen,
                    statusText = if (uiState.faultType == NetworkFaultType.POWER_OUTAGE) "COUPURE" else "12V / 2.5A OK",
                    onClick = {
                        onPortClick(
                            "Prise d'Alimentation 12V DC",
                            "Connecteur jack du bloc secteur. " +
                                if (uiState.faultType == NetworkFaultType.POWER_OUTAGE) "Aucune tension électrique détectée sur le transformateur !"
                                else "Tension secteur 230V stabilisée."
                        )
                    }
                )

                // Reset Pin Hole
                BackPortItem(
                    title = "RESET PIN",
                    color = NetTextMuted,
                    statusText = "BOUTON",
                    onClick = {
                        onPortClick(
                            "Bouton Réinitialisation (Reset)",
                            "Maintenir enfoncé 10 secondes avec un trombone pour restaurer les paramètres d'usine du modem."
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun BackPortItem(
    title: String,
    color: Color,
    statusText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = NetDarkBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp, 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.5.dp, color, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = NetTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = statusText, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LedSphereComponent(
    led: LedIndicatorInfo,
    blinkAlpha: Float,
    onClick: () -> Unit
) {
    val isOff = led.state == LedState.OFF
    val baseColor = led.state.color
    val activeAlpha = if (led.state.isBlinking) blinkAlpha else 1.0f
    val displayColor = if (isOff) Color(0xFF334155) else baseColor.copy(alpha = activeAlpha)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("led_sphere_${led.name.lowercase().replace(" ", "_")}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(34.dp)
        ) {
            // 1. Soft Radial Bloom / Glow Halo (Visible when LED is ON)
            if (!isOff) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    baseColor.copy(alpha = 0.65f * activeAlpha),
                                    baseColor.copy(alpha = 0.20f * activeAlpha),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // 2. Metallic Bezel Outer Ring
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF475569), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        1.5.dp,
                        if (isOff) NetCardBorder else Color.White.copy(alpha = 0.9f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 3. Inner LED Glass Lens
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(displayColor),
                    contentAlignment = Alignment.Center
                ) {
                    // Glass Highlight Reflection Dot
                    if (!isOff) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.85f))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Text Indicator Label
        Text(
            text = led.name.split("/")[0].trim(),
            color = if (isOff) NetTextMuted else NetTextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LedDetailCard(
    led: LedIndicatorInfo,
    blinkAlpha: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val ledColor = if (led.state.isBlinking) led.state.color.copy(alpha = blinkAlpha) else led.state.color

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NetSecondaryBlue.copy(alpha = 0.08f) else NetCardSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) NetSecondaryBlue else NetCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(ledColor)
                        .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = led.name,
                        color = NetTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = led.description,
                        color = NetTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(led.state.color.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = led.state.label,
                    color = led.state.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

