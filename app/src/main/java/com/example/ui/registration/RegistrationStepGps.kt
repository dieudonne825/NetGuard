package com.example.ui.registration

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary
import com.example.util.LocationUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegistrationStepGps(
    latitudeText: String,
    onLatitudeChange: (String) -> Unit,
    longitudeText: String,
    onLongitudeChange: (String) -> Unit,
    locationSource: String,
    onRequestGpsLocation: () -> Unit,
    clientCode: String,
    onClientCodeChange: (String) -> Unit,
    isGpsLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var isCodeCopied by remember { mutableStateOf(false) }
    var isRegenerating by remember { mutableStateOf(false) }

    val isLatValid = latitudeText.toDoubleOrNull()?.let { it in -90.0..90.0 } ?: false
    val isLngValid = longitudeText.toDoubleOrNull()?.let { it in -180.0..180.0 } ?: false
    val areCoordsValid = isLatValid && isLngValid && latitudeText.isNotBlank() && longitudeText.isNotBlank()

    val latBorderColor by animateColorAsState(
        targetValue = when {
            latitudeText.isBlank() -> NetCardBorder
            isLatValid -> NetOnlineGreen
            else -> Color(0xFFCF6679)
        },
        label = "latBorder"
    )
    val lngBorderColor by animateColorAsState(
        targetValue = when {
            longitudeText.isBlank() -> NetCardBorder
            isLngValid -> NetOnlineGreen
            else -> Color(0xFFCF6679)
        },
        label = "lngBorder"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // SECTION 1 : LOCALISATION GPS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NetOnlineGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Icône localisation GPS",
                            tint = NetOnlineGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Position GPS du Modem",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NetTextPrimary
                        )
                        Text(
                            text = "Coordonnées exactes pour le dispatching d'équipe",
                            fontSize = 12.sp,
                            color = NetTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val buttonScale by animateFloatAsState(
                    targetValue = if (isGpsLoading) 0.98f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "buttonScale"
                )

                Button(
                    onClick = onRequestGpsLocation,
                    enabled = !isGpsLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetOnlineGreen,
                        contentColor = Color.White,
                        disabledContainerColor = NetOnlineGreen.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .scale(buttonScale)
                        .testTag("screen_fetch_gps_button")
                ) {
                    if (isGpsLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Acquisition du signal…",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Capturer la position automatiquement",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Capturer Position GPS Automatique",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = onLatitudeChange,
                        label = { Text("Latitude", color = NetTextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isLatValid) NetOnlineGreen else NetTextMuted
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isLatValid,
                                enter = scaleIn(),
                                exit = scaleOut()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Latitude valide",
                                    tint = NetOnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("screen_input_latitude"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = latBorderColor,
                            unfocusedBorderColor = latBorderColor,
                            focusedTextColor = NetTextPrimary,
                            unfocusedTextColor = NetTextPrimary,
                            cursorColor = NetOnlineGreen
                        ),
                        isError = latitudeText.isNotBlank() && !isLatValid
                    )

                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = onLongitudeChange,
                        label = { Text("Longitude", color = NetTextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isLngValid) NetOnlineGreen else NetTextMuted
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isLngValid,
                                enter = scaleIn(),
                                exit = scaleOut()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Longitude valide",
                                    tint = NetOnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("screen_input_longitude"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = lngBorderColor,
                            unfocusedBorderColor = lngBorderColor,
                            focusedTextColor = NetTextPrimary,
                            unfocusedTextColor = NetTextPrimary,
                            cursorColor = NetOnlineGreen
                        ),
                        isError = longitudeText.isNotBlank() && !isLngValid
                    )
                }

                AnimatedVisibility(
                    visible = (latitudeText.isNotBlank() && !isLatValid) || (longitudeText.isNotBlank() && !isLngValid),
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Erreur de validation",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Veuillez entrer des coordonnées GPS valides.",
                            color = Color(0xFFCF6679),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                AnimatedVisibility(
                    visible = locationSource.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // SECTION 2 : CODE CLIENT UNIQUE
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetSecondaryBlue.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
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
                                .background(NetSecondaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Code validé",
                                tint = NetSecondaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Code Client Unique",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NetTextPrimary
                            )
                            Text(
                                text = "Matricule NetGuard",
                                fontSize = 11.sp,
                                color = NetTextMuted,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NetSecondaryBlue.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetSecondaryBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NETGUARD-ID",
                                fontSize = 10.sp,
                                color = NetTextMuted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = clientCode,
                                color = NetSecondaryBlue,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                letterSpacing = 3.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(clientCode))
                                    isCodeCopied = true
                                    Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                                    scope.launch {
                                        delay(2000)
                                        isCodeCopied = false
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCodeCopied) NetOnlineGreen.copy(alpha = 0.2f)
                                        else NetDarkBackground
                                    )
                            ) {
                                AnimatedVisibility(
                                    visible = !isCodeCopied,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copier le code client dans le presse-papiers",
                                        tint = NetSecondaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isCodeCopied,
                                    enter = scaleIn(),
                                    exit = scaleOut()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Code copié avec succès",
                                        tint = NetOnlineGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            val rotateAngle by animateFloatAsState(
                                targetValue = if (isRegenerating) 360f else 0f,
                                animationSpec = tween(durationMillis = 600),
                                label = "rotate"
                            )

                            IconButton(
                                onClick = {
                                    isRegenerating = true
                                    onClientCodeChange(LocationUtils.generateUniqueClientCode())
                                    scope.launch {
                                        delay(600)
                                        isRegenerating = false
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NetDarkBackground)
                                    .testTag("screen_regenerate_code_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Générer un nouveau code client",
                                    tint = NetSecondaryBlue,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .rotate(rotateAngle)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NetTextMuted.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = NetTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ce matricule sera associé aux tickets de maintenance générés pour votre box. Conservez-le précieusement.",
                            fontSize = 12.sp,
                            color = NetTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = areCoordsValid && clientCode.isNotBlank(),
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NetOnlineGreen.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetOnlineGreen.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Toutes les informations sont complètes",
                        tint = NetOnlineGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Étape 2 complète — prêt pour la validation",
                        color = NetOnlineGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
