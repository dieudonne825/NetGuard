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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun RegistrationHeaderStepper(
    currentStep: Int,
    totalSteps: Int = 3,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NetCardSurface,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Column {
            // Header Row: Logo, Title, and Close Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NetSecondaryBlue, NetPrimaryCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Fiche Abonné NetGuard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NetTextPrimary
                        )
                        Text(
                            text = "Espace d'Enregistrement & Suivi Box",
                            fontSize = 12.sp,
                            color = NetSecondaryBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NetDarkBackground)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = NetTextPrimary
                    )
                }
            }

            // Stepper Indicator Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val stepTitles = listOf("Identité", "GPS & Pass", "Validation")
                stepTitles.forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isCompleted = stepNum < currentStep
                    val isCurrent = stepNum == currentStep

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
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
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
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

                        if (index < totalSteps - 1) {
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

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = NetPrimaryCyan,
                trackColor = NetCardBorder
            )
        }
    }
}
