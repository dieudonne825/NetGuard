package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary
import com.example.ui.theme.NetWarningAmber
import com.example.util.LocationUtils

data class ClientProfileData(
    val clientName: String,
    val city: String,
    val neighborhood: String,
    val phone: String,
    val latitude: Double,
    val longitude: Double,
    val clientCode: String,
    val djangoBackendUrl: String
)

@Composable
fun DjangoRegistrationDialog(
    initialProfile: ClientProfileData?,
    onCompleteRegistration: (ClientProfileData) -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var currentStep by remember { mutableIntStateOf(1) }

    // Form inputs
    var clientName by remember { mutableStateOf(initialProfile?.clientName ?: "") }
    var city by remember { mutableStateOf(initialProfile?.city ?: "Douala") }
    var neighborhood by remember { mutableStateOf(initialProfile?.neighborhood ?: "Akwa") }
    var phone by remember { mutableStateOf(initialProfile?.phone ?: "+237 6") }

    var latitudeText by remember { mutableStateOf(initialProfile?.latitude?.toString() ?: "4.0511") }
    var longitudeText by remember { mutableStateOf(initialProfile?.longitude?.toString() ?: "9.7679") }
    var locationSource by remember { mutableStateOf("Coordonnées GPS") }

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

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            val loc = LocationUtils.getCurrentLocation(context)
            latitudeText = String.format("%.5f", loc.latitude).replace(',', '.')
            longitudeText = String.format("%.5f", loc.longitude).replace(',', '.')
            locationSource = "GPS Détecté (${loc.source})"
            Toast.makeText(context, "Position GPS capturée !", Toast.LENGTH_SHORT).show()
        } else {
            errorMessage = "Permission GPS refusée. Saisie manuelle activée."
        }
    }

    fun requestGpsLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            val loc = LocationUtils.getCurrentLocation(context)
            latitudeText = String.format("%.5f", loc.latitude).replace(',', '.')
            longitudeText = String.format("%.5f", loc.longitude).replace(',', '.')
            locationSource = "GPS Détecté (${loc.source})"
            Toast.makeText(context, "Position GPS capturée !", Toast.LENGTH_SHORT).show()
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

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, NetSecondaryBlue.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .testTag("django_registration_dialog"),
            color = NetCardSurface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NetSecondaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$currentStep/3",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enregistrement Client 📋",
                                color = NetTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = when (currentStep) {
                                    1 -> "Coordonnées de l'Abonné"
                                    2 -> "GPS & Code Unique"
                                    else -> "Validation Backend Django"
                                },
                                color = NetTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = NetTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Dots Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { stepIdx ->
                        val active = stepIdx + 1 <= currentStep
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) NetSecondaryBlue else NetCardBorder)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(NetWarningAmber.copy(alpha = 0.15f))
                            .border(1.dp, NetWarningAmber, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = NetWarningAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                when (currentStep) {
                    1 -> {
                        // STEP 1: IDENTITY
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = {
                                    clientName = it
                                    errorMessage = null
                                },
                                label = { Text("Nom Client / Raison Sociale *", color = NetTextMuted, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NetSecondaryBlue) },
                                trailingIcon = {
                                    if (clientName.isNotBlank()) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NetOnlineGreen)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_client_name"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NetSecondaryBlue,
                                    unfocusedBorderColor = NetCardBorder,
                                    focusedTextColor = NetTextPrimary,
                                    unfocusedTextColor = NetTextPrimary
                                )
                            )

                            // Quick City Filter
                            Column {
                                Text("Ville rapide :", fontSize = 11.sp, color = NetTextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    popularCities.take(4).forEach { popCity ->
                                        val isSel = city.equals(popCity, ignoreCase = true)
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { city = popCity },
                                            label = { Text(popCity, fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = NetSecondaryBlue,
                                                selectedLabelColor = Color.White,
                                                containerColor = NetDarkBackground,
                                                labelColor = NetTextPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Ville", color = NetTextMuted, fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = NetSecondaryBlue) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_city"),
                                    shape = RoundedCornerShape(14.dp),
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
                                        .testTag("input_neighborhood"),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NetSecondaryBlue,
                                        unfocusedBorderColor = NetCardBorder,
                                        focusedTextColor = NetTextPrimary,
                                        unfocusedTextColor = NetTextPrimary
                                    )
                                )
                            }

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Téléphone / WhatsApp", color = NetTextMuted, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NetSecondaryBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_phone"),
                                shape = RoundedCornerShape(14.dp),
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

                    2 -> {
                        // STEP 2: GPS & CODE
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Button(
                                onClick = { requestGpsLocation() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetOnlineGreen,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("fetch_gps_button")
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Détecter la Position GPS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = latitudeText,
                                    onValueChange = { latitudeText = it },
                                    label = { Text("Latitude", color = NetTextMuted, fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = NetOnlineGreen) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_latitude"),
                                    shape = RoundedCornerShape(14.dp),
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
                                    label = { Text("Longitude", color = NetTextMuted, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_longitude"),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NetOnlineGreen,
                                        unfocusedBorderColor = NetCardBorder,
                                        focusedTextColor = NetTextPrimary,
                                        unfocusedTextColor = NetTextPrimary
                                    )
                                )
                            }

                            // Code Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = NetDarkBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NetSecondaryBlue.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Code Client Unique :", color = NetTextMuted, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = clientCode,
                                            color = NetSecondaryBlue,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp
                                        )
                                    }

                                    Row {
                                        IconButton(onClick = {
                                            clipboardManager.setText(AnnotatedString(clientCode))
                                            Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = NetSecondaryBlue, modifier = Modifier.size(20.dp))
                                        }

                                        IconButton(onClick = { clientCode = LocationUtils.generateUniqueClientCode() }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Nouveau", tint = NetSecondaryBlue, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // STEP 3: BACKEND & RECAP
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = djangoBackendUrl,
                                onValueChange = { djangoBackendUrl = it },
                                label = { Text("URL du Serveur Django Admin API", color = NetTextMuted, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, tint = NetSecondaryBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_django_url"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NetSecondaryBlue,
                                    unfocusedBorderColor = NetCardBorder,
                                    focusedTextColor = NetTextPrimary,
                                    unfocusedTextColor = NetTextPrimary
                                )
                            )

                            // Recap Pass
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = NetDarkBackground),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Client :", color = NetTextMuted, fontSize = 12.sp)
                                        Text(clientName.ifBlank { "Client Anonyme" }, color = NetTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Code Unique :", color = NetTextMuted, fontSize = 12.sp)
                                        Text(clientCode, color = NetSecondaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Localisation :", color = NetTextMuted, fontSize = 12.sp)
                                        Text("$city ($neighborhood)", color = NetTextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("GPS :", color = NetTextMuted, fontSize = 12.sp)
                                        Text("$latitudeText, $longitudeText", color = NetOnlineGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetSecondaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NetCardSurface,
                                contentColor = NetSecondaryBlue
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("registration_prev_button")
                        ) {
                            Text("Précédent", color = NetSecondaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep == 1) {
                                if (clientName.trim().isBlank()) {
                                    errorMessage = "Veuillez indiquer le nom du client."
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NetSecondaryBlue,
                            contentColor = Color.White,
                            disabledContainerColor = NetCardBorder,
                            disabledContentColor = NetTextMuted
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("registration_next_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (currentStep < 3) "Continuer →" else "Valider & Enregistrer 🚀",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
