package com.example.ui.registration

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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetPrimaryCyan
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary

@Composable
fun RegistrationStepValidation(
    clientName: String,
    city: String,
    neighborhood: String,
    phone: String,
    latitudeText: String,
    longitudeText: String,
    clientCode: String,
    djangoBackendUrl: String,
    onDjangoBackendUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
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
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(NetPrimaryCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column {
                        Text(
                            text = "Validation & Transmission 🛡️",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NetTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vérifiez vos informations avant de confirmer la fiche abonné.",
                            fontSize = 13.sp,
                            color = NetTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = djangoBackendUrl,
                    onValueChange = onDjangoBackendUrlChange,
                    label = { Text("URL Serveur Django Admin", color = NetTextMuted, fontSize = 13.sp) },
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

                Spacer(modifier = Modifier.height(20.dp))

                // Profile Recap Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Récapitulatif Fiche Abonné",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NetSecondaryBlue
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        RecapRow(icon = Icons.Default.Person, label = "Nom Client", value = clientName.ifBlank { "Non renseigné" })
                        RecapRow(icon = Icons.Default.Home, label = "Ville & Quartier", value = "$city, $neighborhood")
                        RecapRow(icon = Icons.Default.Phone, label = "Téléphone Support", value = phone.ifBlank { "Non renseigné" })
                        RecapRow(icon = Icons.Default.LocationOn, label = "GPS", value = "$latitudeText, $longitudeText")
                        RecapRow(icon = Icons.Default.Dns, label = "Code Unique Pass", value = clientCode)
                    }
                }
            }
        }

        // Confirmation Disclaimer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NetCardSurface)
                .border(1.dp, NetCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NetOnlineGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "En enregistrant cette fiche, vous permettez à l'application NetGuard de soumettre automatiquement les diagnostics de panne en cas de dysfonctionnement.",
                    fontSize = 12.sp,
                    color = NetTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun RecapRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = NetTextMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 13.sp, color = NetTextMuted)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NetTextPrimary)
    }
}
