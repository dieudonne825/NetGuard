package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ClientProfileData
import com.example.ui.theme.NetAccentBlue
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
import com.example.util.LocationUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DjangoRegistrationScreen(
    initialProfile: ClientProfileData?,
    isAlreadyRegistered: Boolean,
    onCompleteRegistration: (ClientProfileData) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var currentStep by remember { mutableIntStateOf(1) }

    // Form states
    var clientName by remember { mutableStateOf(initialProfile?.clientName ?: "") }
    var city by remember { mutableStateOf(initialProfile?.city ?: "Douala") }
    var neighborhood by remember { mutableStateOf(initialProfile?.neighborhood ?: "Akwa") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "+237 6") }

    var latitudeText by remember { mutableStateOf(initialProfile?.latitude?.toString() ?: "4.0511") }
    var longitudeText by remember { mutableStateOf(initialProfile?.longitude?.toString() ?: "9.7679") }
    var locationSource by remember { mutableStateOf("Coordonnées GPS Par Défaut") }

    var clientCode by remember {
        mutableStateOf(
            if (!initialProfile?.clientCode.isNullOrBlank()) initialProfile!!.clientCode
            else LocationUtils.generateUniqueClientCode()
        )
    }

    var djangoBackendUrl by remember {
        mutableStateOf(initialProfile?.djangoBackendUrl ?: "https://netguard-admin.example.com/")
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // GPS Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            val loc = LocationUtils.getCurrentLocation(context)
            latitudeText = String.format("%.5f", loc.latitude).replace(',', '.')
            longitudeText = String.format("%.5f", loc.longitude).replace(',', '.')
            locationSource = "GPS Capturé (${loc.source})"
            Toast.makeText(context, "Position GPS capturée avec succès !", Toast.LENGTH_SHORT).show()
        } else {
            errorMessage = "Permission GPS non accordée. Saisie manuelle activée."
        }
    }

    fun requestGpsLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val loc = LocationUtils.getCurrentLocation(context)
            latitudeText = String.format("%.5f", loc.latitude).replace(',', '.')
            longitudeText = String.format("%.5f", loc.longitude).replace(',', '.')
            locationSource = "GPS Capturé (${loc.source})"
            Toast.makeText(context, "Position GPS capturée avec succès !", Toast.LENGTH_SHORT).show()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val popularCities = remember { listOf("Douala", "Yaoundé", "Bafoussam", "Garoua", "Kribi") }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(NetSecondaryBlue, NetPrimaryCyan)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Fiche Abonné NetGuard",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NetTextPrimary
                                )
                                Text(
                                    text = "Synchronisation Serveur Django Admin v2.4",
                                    fontSize = 11.sp,
                                    color = NetSecondaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (currentStep > 1) {
                            IconButton(onClick = { currentStep-- }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Précédent", tint = NetTextPrimary)
                            }
                        } else if (isAlreadyRegistered) {
                            IconButton(onClick = onDismissRequest) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = NetTextPrimary)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = NetTextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NetCardSurface)
                )

                // Animated Top Progress Indicator
                LinearProgressIndicator(
                    progress = { currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = NetPrimaryCyan,
                    trackColor = NetCardBorder
                )
            }
        },
        bottomBar = {
            Surface(
                color = NetCardSurface,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetSecondaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NetCardSurface,
                                contentColor = NetSecondaryBlue,
                                disabledContainerColor = NetDarkBackground,
                                disabledContentColor = NetTextMuted
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("registration_screen_prev_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NetSecondaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Précédent", color = NetSecondaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep == 1) {
                                if (clientName.trim().isBlank()) {
                                    errorMessage = "Veuillez préciser le nom complet du client ou de l'entreprise."
                                } else {
                                    errorMessage = null
                                    currentStep = 2
                                }
                            } else if (currentStep == 2) {
                                errorMessage = null
                                currentStep = 3
                            } else {
                                isSubmitting = true
                                val lat = latitudeText.toDoubleOrNull() ?: 4.0511
                                val lng = longitudeText.toDoubleOrNull() ?: 9.7679

                                val profile = ClientProfileData(
                                    clientName = clientName.trim(),
                                    city = city.trim(),
                                    neighborhood = neighborhood.trim(),
                                    phone = phone.trim(),
                                    latitude = lat,
                                    longitude = lng,
                                    clientCode = clientCode,
                                    djangoBackendUrl = djangoBackendUrl.trim()
                                )
                                onCompleteRegistration(profile)
                            }
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NetSecondaryBlue,
                            contentColor = Color.White,
                            disabledContainerColor = NetCardBorder,
                            disabledContentColor = NetTextMuted
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(54.dp)
                            .testTag("registration_screen_next_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (currentStep < 3) "Continuer →" else "Valider & Transmettre 🚀",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = NetDarkBackground,
        modifier = modifier.testTag("django_registration_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-End Spacious Stepper Bar
            Surface(
                color = NetCardSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf("Identité", "GPS & Pass", "Validation")
                    steps.forEachIndexed { index, title ->
                        val stepNum = index + 1
                        val isCompleted = stepNum < currentStep
                        val isCurrent = stepNum == currentStep

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> NetOnlineGreen
                                            isCurrent -> NetSecondaryBlue
                                            else -> NetDarkBackground
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 2.dp else 1.dp,
                                        color = when {
                                            isCompleted -> NetOnlineGreen
                                            isCurrent -> NetSecondaryBlue
                                            else -> NetCardBorder
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                } else {
                                    Text(
                                        text = "$stepNum",
                                        color = if (isCurrent) Color.White else NetTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                                color = when {
                                    isCurrent -> NetTextPrimary
                                    isCompleted -> NetOnlineGreen
                                    else -> NetTextMuted
                                }
                            )

                            if (index < steps.size - 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(2.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (stepNum < currentStep) NetOnlineGreen
                                            else NetCardBorder
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }

            // Main Scrollable Form Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NetWarningAmber.copy(alpha = 0.15f))
                            .border(1.dp, NetWarningAmber, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = NetWarningAmber, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = errorMessage!!,
                                color = NetWarningAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "step_page_transition"
                ) { targetStep ->
                    when (targetStep) {
                        1 -> {
                            // PAGE 1: CLIENT IDENTITY & CONTACT
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                // Onboarding Banner
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        NetSecondaryBlue.copy(alpha = 0.12f),
                                                        NetPrimaryCyan.copy(alpha = 0.04f)
                                                    )
                                                )
                                            )
                                            .padding(22.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(NetSecondaryBlue),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Coordonnées Abonné 👤",
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NetTextPrimary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Identifiez l'abonné ou le site client pour associer son modem à la carte d'incident Django.",
                                                    fontSize = 12.sp,
                                                    color = NetTextSecondary,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // Form Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(22.dp)) {
                                        OutlinedTextField(
                                            value = clientName,
                                            onValueChange = {
                                                clientName = it
                                                errorMessage = null
                                            },
                                            label = { Text("Nom Complet Abonné / Raison Sociale *", color = NetTextMuted, fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NetSecondaryBlue) },
                                            trailingIcon = {
                                                if (clientName.isNotBlank()) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NetOnlineGreen)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("screen_input_client_name"),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NetSecondaryBlue,
                                                unfocusedBorderColor = NetCardBorder,
                                                focusedTextColor = NetTextPrimary,
                                                unfocusedTextColor = NetTextPrimary
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Quick city selector chips
                                        Text(
                                            text = "Sélection Rapide de la Ville :",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NetTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            popularCities.forEach { popCity ->
                                                val isSelected = city.equals(popCity, ignoreCase = true)
                                                FilterChip(
                                                    selected = isSelected,
                                                    onClick = { city = popCity },
                                                    label = { Text(popCity, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = NetSecondaryBlue,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = NetDarkBackground,
                                                        labelColor = NetTextPrimary
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = city,
                                                onValueChange = { city = it },
                                                label = { Text("Ville", color = NetTextMuted, fontSize = 12.sp) },
                                                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = NetSecondaryBlue) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("screen_input_city"),
                                                shape = RoundedCornerShape(16.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = NetSecondaryBlue,
                                                    unfocusedBorderColor = NetCardBorder,
                                                    focusedTextColor = NetTextPrimary,
                                                    unfocusedTextColor = NetTextPrimary
                                                )
                                            )

                                            OutlinedTextField(
                                                value = neighborhood,
                                                onValueChange = { neighborhood = it },
                                                label = { Text("Quartier", color = NetTextMuted, fontSize = 12.sp) },
                                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = NetSecondaryBlue) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("screen_input_neighborhood"),
                                                shape = RoundedCornerShape(16.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = NetSecondaryBlue,
                                                    unfocusedBorderColor = NetCardBorder,
                                                    focusedTextColor = NetTextPrimary,
                                                    unfocusedTextColor = NetTextPrimary
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        OutlinedTextField(
                                            value = phone,
                                            onValueChange = { phone = it },
                                            label = { Text("Téléphone Mobile / WhatsApp (Support)", color = NetTextMuted, fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NetSecondaryBlue) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("screen_input_phone"),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NetSecondaryBlue,
                                                unfocusedBorderColor = NetCardBorder,
                                                focusedTextColor = NetTextPrimary,
                                                unfocusedTextColor = NetTextPrimary
                                            )
                                        )
                                    }
                                }

                                // Security Notice Badge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(NetCardSurface)
                                        .border(1.dp, NetCardBorder, RoundedCornerShape(18.dp))
                                        .padding(18.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Shield, contentDescription = null, tint = NetOnlineGreen, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Text(
                                            text = "Garantie NetGuard : Vos coordonnées sont sécurisées et réservées exclusivement au support technique d'urgence.",
                                            fontSize = 12.sp,
                                            color = NetTextSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // PAGE 2: GPS GEOLOCATION & PASS CODE
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(22.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(NetOnlineGreen),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Géolocalisation & Carte 📍",
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NetTextPrimary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Coordonnées GPS précises pour guider l'équipe technique d'intervention.",
                                                    fontSize = 12.sp,
                                                    color = NetTextSecondary,
                                                    lineHeight = 16.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(22.dp))

                                        // Auto GPS Capture Button
                                        Button(
                                            onClick = { requestGpsLocation() },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = NetOnlineGreen,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp)
                                                .testTag("screen_fetch_gps_button")
                                        ) {
                                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Capturer ma Position GPS Exacte", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                        }

                                        Spacer(modifier = Modifier.height(18.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = latitudeText,
                                                onValueChange = { latitudeText = it },
                                                label = { Text("Latitude", color = NetTextMuted, fontSize = 12.sp) },
                                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = NetOnlineGreen) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("screen_input_latitude"),
                                                shape = RoundedCornerShape(16.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = NetOnlineGreen,
                                                    unfocusedBorderColor = NetCardBorder,
                                                    focusedTextColor = NetTextPrimary,
                                                    unfocusedTextColor = NetTextPrimary
                                                )
                                            )

                                            OutlinedTextField(
                                                value = longitudeText,
                                                onValueChange = { longitudeText = it },
                                                label = { Text("Longitude", color = NetTextMuted, fontSize = 12.sp) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("screen_input_longitude"),
                                                shape = RoundedCornerShape(16.dp),
                                                singleLine = true,
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = NetOnlineGreen,
                                                    unfocusedBorderColor = NetCardBorder,
                                                    focusedTextColor = NetTextPrimary,
                                                    unfocusedTextColor = NetTextPrimary
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(NetOnlineGreen)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Source : $locationSource",
                                                color = NetOnlineGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                // Unique Subscriber Code Pass Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NetSecondaryBlue)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(22.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Code Identifiant Unique Abonné :", color = NetTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = clientCode,
                                                    color = NetSecondaryBlue,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 24.sp,
                                                    letterSpacing = 2.sp
                                                )
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(clientCode))
                                                        Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier le code", tint = NetSecondaryBlue, modifier = Modifier.size(22.dp))
                                                }

                                                IconButton(
                                                    onClick = { clientCode = LocationUtils.generateUniqueClientCode() },
                                                    modifier = Modifier.testTag("screen_regenerate_code_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Refresh,
                                                        contentDescription = "Régénérer le code",
                                                        tint = NetSecondaryBlue,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Ce code unique est automatiquement injecté dans chaque rapport de panne transmis à la console Django d'administration.",
                                            fontSize = 11.sp,
                                            color = NetTextMuted,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        3 -> {
                            // PAGE 3: DJANGO BACKEND & FINAL VALIDATION
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(22.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .clip(CircleShape)
                                                    .background(NetPrimaryCyan),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Serveur Django Admin 🌐",
                                                    fontSize = 19.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = NetTextPrimary
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Adresse Endpoint de l'API de supervision télécom",
                                                    fontSize = 12.sp,
                                                    color = NetTextSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(22.dp))

                                        OutlinedTextField(
                                            value = djangoBackendUrl,
                                            onValueChange = { djangoBackendUrl = it },
                                            label = { Text("Endpoint API Django", color = NetTextMuted, fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = NetSecondaryBlue) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("screen_input_django_url"),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NetSecondaryBlue,
                                                unfocusedBorderColor = NetCardBorder,
                                                focusedTextColor = NetTextPrimary,
                                                unfocusedTextColor = NetTextPrimary
                                            )
                                        )
                                    }
                                }

                                // Digital Pass / Fiche Recap Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(22.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Fiche Abonné Prête à Transmettre 📄",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = NetTextPrimary
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(NetOnlineGreen.copy(alpha = 0.15f))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text("🟢 Prêt à valider", color = NetOnlineGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(NetDarkBackground)
                                                .padding(16.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Client :", color = NetTextMuted, fontSize = 12.sp)
                                                    Text(clientName.ifBlank { "Non renseigné" }, color = NetTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Code Identifiant :", color = NetTextMuted, fontSize = 12.sp)
                                                    Text(clientCode, color = NetSecondaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Localisation :", color = NetTextMuted, fontSize = 12.sp)
                                                    Text("$city, $neighborhood", color = NetTextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Téléphone :", color = NetTextMuted, fontSize = 12.sp)
                                                    Text(phone, color = NetTextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Coordonnées GPS :", color = NetTextMuted, fontSize = 12.sp)
                                                    Text("$latitudeText, $longitudeText", color = NetOnlineGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
