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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Shield
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetPrimaryCyan
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary
import com.example.util.LocationUtils

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
    val context = LocalContext.current
    var isGeolocating by remember { mutableStateOf(false) }

    // ═══════════════════════════════════════════════════════
    // VALIDATION (inchangée)
    // ═══════════════════════════════════════════════════════
    val isNameValid = clientName.trim().length >= 2
    val isPhoneValid = phone.trim().length >= 8 && phone.all { it.isDigit() || it in "+-() " }
    val isCityValid = city.trim().isNotBlank()
    val isNeighborhoodValid = neighborhood.trim().isNotBlank()
    val isStepComplete = isNameValid && isPhoneValid && isCityValid && isNeighborhoodValid

    val completionProgress = listOf(isNameValid, isPhoneValid, isCityValid, isNeighborhoodValid)
        .count { it } / 4f

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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ═══════════════════════════════════════════════════════
        // HEADER ÉPURÉ : Indicateur d'étape + progression
        // ═══════════════════════════════════════════════════════
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge numéro d'étape
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(NetSecondaryBlue, NetPrimaryCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        text = "Identité Abonné",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NetTextPrimary
                    )
                    Text(
                        text = "Titulaire du modem et lieu d'installation",
                        fontSize = 13.sp,
                        color = NetTextSecondary
                    )
                }
            }

            // Barre de progression visuelle
            LinearProgressIndicator(
                progress = { completionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NetPrimaryCyan,
                trackColor = NetCardBorder,
            )
        }

        // ═══════════════════════════════════════════════════════
        // CARTE 1 : COORDONNÉES (Nom + Téléphone)
        // ═══════════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Titre de section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = NetSecondaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Coordonnées du titulaire",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetTextPrimary,
                        letterSpacing = 0.3.sp
                    )
                }

                // ── Nom complet (pleine largeur) ──
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
                    shape = RoundedCornerShape(14.dp),
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
                    Text(
                        text = "Le nom doit contenir au moins 2 caractères.",
                        color = Color(0xFFCF6679),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // ── Téléphone (pleine largeur, sous le nom) ──
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
                    shape = RoundedCornerShape(14.dp),
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
                    Text(
                        text = "Numéro invalide (min. 8 chiffres).",
                        color = Color(0xFFCF6679),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════
        // CARTE 2 : LOCALISATION (Chips Ville + Quartier)
        // ═══════════════════════════════════════════════════════
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Titre de section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationCity,
                        contentDescription = null,
                        tint = NetSecondaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Adresse d'installation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetTextPrimary,
                        letterSpacing = 0.3.sp
                    )
                }

                // ── BOUTON DÉTECTION GPS AUTOMATIQUE ──
                Button(
                    onClick = {
                        isGeolocating = true
                        try {
                            val coords = LocationUtils.getCurrentLocation(context)
                            val addressInfo = LocationUtils.getAddressFromLocation(
                                context,
                                coords.latitude,
                                coords.longitude
                            )
                            onCityChange(addressInfo.city)
                            onNeighborhoodChange(addressInfo.neighborhood)
                            Toast.makeText(
                                context,
                                "📍 Géolocalisé : ${addressInfo.neighborhood}, ${addressInfo.city}",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur de géolocalisation", Toast.LENGTH_SHORT).show()
                        } finally {
                            isGeolocating = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_auto_geolocate_identity"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetSecondaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "GPS Auto",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isGeolocating) "Détection GPS..." else "📍 Détecter Ville & Quartier par GPS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }

                // ── Chips de ville (défilables horizontalement) ──
                Text(
                    text = "Sélection rapide",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NetTextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
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
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.scale(chipScale)
                        )
                    }
                }

                // ── Ville (pleine largeur) ──
                OutlinedTextField(
                    value = city,
                    onValueChange = onCityChange,
                    label = { Text("Ville * (ex: Yaoundé, Douala)", color = NetTextMuted, fontSize = 13.sp) },
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
                        .fillMaxWidth()
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

                // ── Suggestions de Quartiers populaires ──
                val popularNeighborhoodsMap = remember {
                    mapOf(
                        "Yaoundé" to listOf("Bastos", "Mendong", "Omnisports", "Odza", "Biyem-Assi", "Etoudi", "Elig-Essono", "Mvan"),
                        "Douala" to listOf("Akwa", "Bonanjo", "Bonapriso", "Makèpè", "Deido", "Logbessou", "Ndogpassi", "Bonamoussadi"),
                        "Bafoussam" to listOf("Tamdja", "Djeleng", "Kouogouo", "Bamendzi", "Kamcop"),
                        "Garoua" to listOf("Roumde Adjia", "Poumpoumré", "Yelwa", "Laindé"),
                        "Kribi" to listOf("Ngoye", "Mboa Manga", "Talla", "Dombe"),
                        "Bamenda" to listOf("Commercial Avenue", "Up Station", "Nkwen", "Mankon"),
                        "Buea" to listOf("Molyko", "Clerks Quarters", "Great Soppo")
                    )
                }

                val matchedCityEntry = popularNeighborhoodsMap.entries.firstOrNull {
                    it.key.equals(city.trim(), ignoreCase = true)
                }
                val suggestions = matchedCityEntry?.value ?: listOf("Bastos", "Akwa", "Mendong", "Bonanjo", "Biyem-Assi", "Makèpè")

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (matchedCityEntry != null) "Quartiers à ${matchedCityEntry.key} (1 clic) :" else "Quartiers rapides (clic direct) :",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = NetTextMuted
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { popNeigh ->
                            val isSelected = neighborhood.equals(popNeigh, ignoreCase = true)
                            val chipScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.05f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessHigh),
                                label = "neighChipScale"
                            )

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        onNeighborhoodChange("")
                                    } else {
                                        onNeighborhoodChange(popNeigh)
                                        // Si la ville était vide, déduire automatiquement la ville liée au quartier !
                                        if (city.isBlank()) {
                                            val inferredCity = popularNeighborhoodsMap.entries.firstOrNull { entry ->
                                                entry.value.contains(popNeigh)
                                            }?.key
                                            if (inferredCity != null) {
                                                onCityChange(inferredCity)
                                            }
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        popNeigh,
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.scale(chipScale)
                            )
                        }
                    }
                }

                // ── Quartier (pleine largeur, sous les suggestions) ──
                OutlinedTextField(
                    value = neighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("Quartier / Secteur *", color = NetTextMuted, fontSize = 13.sp) },
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
                        .fillMaxWidth()
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
        }

        // ═══════════════════════════════════════════════════════
        // BADGE SÉCURITÉ (plus compact, en footer)
        // ═══════════════════════════════════════════════════════
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NetCardSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NetOnlineGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Sécurité des données",
                        tint = NetOnlineGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Garantie NetGuard : vos coordonnées sont chiffrées et réservées aux techniciens de maintenance.",
                    fontSize = 12.sp,
                    color = NetTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // ═══════════════════════════════════════════════════════
        // INDICATEUR DE COMPLÉTION (apparaît uniquement si complet)
        // ═══════════════════════════════════════════════════════
        AnimatedVisibility(
            visible = isStepComplete,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NetOnlineGreen.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetOnlineGreen.copy(alpha = 0.25f)),
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
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Étape 1 complète — passez à la localisation GPS",
                        color = NetOnlineGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Espace de sécurité en bas pour le scroll
        Spacer(modifier = Modifier.height(16.dp))
    }
}