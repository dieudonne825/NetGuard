package com.example.ui.registration

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ClientProfileData
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
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
            RegistrationHeaderStepper(
                currentStep = currentStep,
                totalSteps = 3,
                onDismissRequest = onDismissRequest
            )
        },
        bottomBar = {
            Surface(
                color = NetCardSurface,
                shadowElevation = 16.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetSecondaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NetCardSurface,
                                contentColor = NetSecondaryBlue
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("registration_screen_prev_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NetSecondaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Précédent", color = NetSecondaryBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep == 1) {
                                if (clientName.trim().isBlank()) {
                                    errorMessage = "Veuillez préciser le nom complet de l'abonné."
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
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NetSecondaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp)
                            .testTag("registration_screen_next_button")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (currentStep < 3) "Étape Suivante →" else "Enregistrer l'Abonné 🚀",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NetWarningAmber.copy(alpha = 0.15f))
                        .border(1.dp, NetWarningAmber, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = NetWarningAmber, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = errorMessage!!,
                            color = NetWarningAmber,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    1 -> RegistrationStepIdentity(
                        clientName = clientName,
                        onClientNameChange = {
                            clientName = it
                            errorMessage = null
                        },
                        city = city,
                        onCityChange = { city = it },
                        neighborhood = neighborhood,
                        onNeighborhoodChange = { neighborhood = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        popularCities = popularCities
                    )

                    2 -> RegistrationStepGps(
                        latitudeText = latitudeText,
                        onLatitudeChange = { latitudeText = it },
                        longitudeText = longitudeText,
                        onLongitudeChange = { longitudeText = it },
                        locationSource = locationSource,
                        onRequestGpsLocation = { requestGpsLocation() },
                        clientCode = clientCode,
                        onClientCodeChange = { clientCode = it }
                    )

                    3 -> RegistrationStepValidation(
                        clientName = clientName,
                        city = city,
                        neighborhood = neighborhood,
                        phone = phone,
                        latitudeText = latitudeText,
                        longitudeText = longitudeText,
                        clientCode = clientCode,
                        djangoBackendUrl = djangoBackendUrl,
                        onDjangoBackendUrlChange = { djangoBackendUrl = it }
                    )
                }
            }
        }
    }
}
