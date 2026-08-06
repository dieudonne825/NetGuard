package com.example.ui.registration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetPrimaryCyan
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary

@Composable
fun RegistrationStepIdentity(
    clientName: String,
    onClientNameChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    neighborhood: String,
    onNeighborhoodChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    popularCities: List<String>,
    modifier: Modifier = Modifier
) {
    // ═══════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════
    val isNameValid = clientName.trim().length >= 2
    val isPhoneValid = phone.trim().length >= 8 && phone.all { it.isDigit() || it in "+-() " }
    val isCityValid = city.trim().isNotBlank()
    val isNeighborhoodValid = neighborhood.trim().isNotBlank()
    val isStepComplete = isNameValid && isPhoneValid && isCityValid && isNeighborhoodValid

    // Couleurs de bordure animées
    val nameBorderColor by animateColorAsState(
        targetValue = when {
            clientName.isBlank() -> NetCardBorder
            isNameValid -> NetOnlineGreen
            else -> Color(0xFFCF6679)
        },
        label = "nameBorder"
    )
    val phoneBorderColor by animateColorAsState(
        targetValue = when {
            phone.isBlank() -> NetCardBorder
            isPhoneValid -> NetOnlineGreen
            else -> Color(0xFFCF6679)
        },
        label = "phoneBorder"
    )
    val cityBorderColor by animateColorAsState(
        targetValue = if (isCityValid) NetOnlineGreen else NetCardBorder,
        label = "cityBorder"
    )
    val neighborhoodBorderColor by animateColorAsState(
        targetValue = if (isNeighborhoodValid) NetOnlineGreen else NetCardBorder,
        label = "neighborhoodBorder"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ═══════════════════════════════════════════════════════
        // HERO BANNER
        // ═══════════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
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
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(NetSecondaryBlue, NetPrimaryCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Icône identité abonné",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Identité Abonné Client",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NetTextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Renseignez le titulaire et le lieu de résidence du modem.",
                            fontSize = 12.sp,
                            color = NetTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // CARTE FORMULAIRE
        // ═══════════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // ── Nom ──
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { if (it.length <= 60) onClientNameChange(it) },
                    label = { Text("Nom complet ou Entreprise *", color = NetTextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isNameValid) NetOnlineGreen else NetSecondaryBlue
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = isNameValid,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Nom valide",
                                tint = NetOnlineGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("screen_input_client_name"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = clientName.isNotBlank() && !isNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = nameBorderColor,
                        unfocusedBorderColor = nameBorderColor,
                        focusedTextColor = NetTextPrimary,
                        unfocusedTextColor = NetTextPrimary,
                        cursorColor = NetSecondaryBlue,
                        errorBorderColor = Color(0xFFCF6679)
                    )
                )

                // Message d'erreur nom
                AnimatedVisibility(
                    visible = clientName.isNotBlank() && !isNameValid,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Le nom doit contenir au moins 2 caractères.",
                            color = Color(0xFFCF6679),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Chips de ville rapide ──
                Text(
                    text = "Ville (sélection rapide)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NetTextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    popularCities.forEach { popCity ->
                        val isSelected = city.equals(popCity, ignoreCase = true)
                        val chipScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.05f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessHigh),
                            label = "chipScale"
                        )

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) onCityChange("") else onCityChange(popCity)
                            },
                            label = {
                                Text(
                                    popCity,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NetSecondaryBlue,
                                selectedLabelColor = Color.White,
                                containerColor = NetDarkBackground,
                                labelColor = NetTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.scale(chipScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Ville & Quartier ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = onCityChange,
                        label = { Text("Ville *", color = NetTextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = if (isCityValid) NetOnlineGreen else NetSecondaryBlue
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isCityValid,
                                enter = scaleIn(),
                                exit = scaleOut()
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Ville renseignée",
                                    tint = NetOnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("screen_input_city"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cityBorderColor,
                            unfocusedBorderColor = cityBorderColor,
                            focusedTextColor = NetTextPrimary,
                            unfocusedTextColor = NetTextPrimary,
                            cursorColor = NetSecondaryBlue
                        )
                    )

                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = onNeighborhoodChange,
                        label = { Text("Quartier *", color = NetTextMuted, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = null,
                                tint = if (isNeighborhoodValid) NetOnlineGreen else NetSecondaryBlue
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isNeighborhoodValid,
                                enter = scaleIn(),
                                exit = scaleOut()
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Quartier renseigné",
                                    tint = NetOnlineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("screen_input_neighborhood"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neighborhoodBorderColor,
                            unfocusedBorderColor = neighborhoodBorderColor,
                            focusedTextColor = NetTextPrimary,
                            unfocusedTextColor = NetTextPrimary,
                            cursorColor = NetSecondaryBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Téléphone ──
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        val filtered = it.filter { c -> c.isDigit() || c in "+-() " }
                        if (filtered.length <= 20) onPhoneChange(filtered)
                    },
                    label = { Text("Numéro WhatsApp / Support *", color = NetTextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = if (isPhoneValid) NetOnlineGreen else NetSecondaryBlue
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = isPhoneValid,
                            enter = scaleIn(),
                            exit = scaleOut()
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Numéro valide",
                                tint = NetOnlineGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("screen_input_phone"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = phone.isNotBlank() && !isPhoneValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = phoneBorderColor,
                        unfocusedBorderColor = phoneBorderColor,
                        focusedTextColor = NetTextPrimary,
                        unfocusedTextColor = NetTextPrimary,
                        cursorColor = NetSecondaryBlue,
                        errorBorderColor = Color(0xFFCF6679)
                    )
                )

                // Message d'erreur téléphone
                AnimatedVisibility(
                    visible = phone.isNotBlank() && !isPhoneValid,
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Numéro invalide (min. 8 chiffres).",
                            color = Color(0xFFCF6679),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // BADGE SÉCURITÉ
        // ═══════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NetCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NetOnlineGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Sécurité des données",
                        tint = NetOnlineGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Garantie NetGuard : vos coordonnées sont chiffrées et réservées exclusivement aux techniciens de maintenance.",
                    fontSize = 12.sp,
                    color = NetTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // INDICATEUR DE COMPLÉTION
        // ═══════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = isStepComplete,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
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
                        contentDescription = "Étape complète",
                        tint = NetOnlineGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Étape 1 complète — passez à la localisation GPS",
                        color = NetOnlineGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}