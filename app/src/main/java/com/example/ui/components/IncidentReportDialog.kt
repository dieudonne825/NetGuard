package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OutageLog
import com.example.data.TechnicianTicket
import com.example.model.NetworkFaultType
import com.example.model.RouterHardwareModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Diagnostic codes and optical attenuation for technical details
val NetworkFaultType.errorCode: String
    get() = when (this) {
        NetworkFaultType.NONE_ONLINE -> "SYS_NOMINAL_10G"
        NetworkFaultType.ISP_OUTAGE -> "ERR_OPTICAL_LOSS_NRO_DISCONNECT"
        NetworkFaultType.POWER_OUTAGE -> "ERR_POWER_SUPPLY_12V_OFF"
        NetworkFaultType.FIBER_SIGNAL_LOW -> "ERR_OPTICAL_ATTENUATION_HIGH_28DBM"
        NetworkFaultType.DNS_SERVER_DOWN -> "ERR_DNS_RESOLVER_TIMEOUT"
        NetworkFaultType.WIFI_INTERFERENCE -> "ERR_WIFI_RF_JAMMING_CHANNEL"
        NetworkFaultType.DHCP_POOL_EXHAUSTED -> "ERR_DHCP_SUBNET_FULL_EXHAUSTED"
        NetworkFaultType.MODEM_OVERHEAT -> "ERR_CPU_THERMAL_SHUTDOWN_88C"
        NetworkFaultType.ETHERNET_CABLE_FAULT -> "ERR_ETH_PHY_LINK_BRIDE_10M"
    }

val NetworkFaultType.opticalAttenuation: String
    get() = when (this) {
        NetworkFaultType.NONE_ONLINE -> "-18.2 dBm (Signal Optique Normal)"
        NetworkFaultType.FIBER_SIGNAL_LOW -> "-28.5 dBm (Signal Optique Très Faible)"
        NetworkFaultType.ISP_OUTAGE -> "-INF dBm (Signal Optique Interrompu)"
        NetworkFaultType.POWER_OUTAGE -> "-INF dBm (Modem Non Alimenté)"
        else -> "-19.1 dBm (Ligne Physique OK)"
    }

@Composable
fun IncidentReportDialog(
    faultType: NetworkFaultType,
    activeModem: RouterHardwareModel,
    latencyMs: Int,
    packetLossPct: Double,
    outagesHistory: List<OutageLog>,
    ticket: TechnicianTicket?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date())
    val ticketNumber = ticket?.ticketNumber ?: "RPT-FR-${(10000..99999).random()}"

    // Default recipient is the Telecom Integrator number specified by user
    var smsRecipientNumber by remember { mutableStateOf("688137007") }
    var showTechnicalDetails by remember { mutableStateOf(false) }

    // Short, clear SMS message for the technician
    val compactSmsText = "[NETGUARD] INCIDENT #$ticketNumber\nBox: ${activeModem.name}\nPanne: ${faultType.title}\nCode: ${faultType.errorCode}\nPTO: PTO-83920-FI-42\nAtténuation: ${faultType.opticalAttenuation}"

    // Full detailed text report (for copy / export)
    val fullReportText = buildString {
        appendLine("==================================================")
        appendLine("📄 FICHE D'INCIDENT TECHNIQUE - NETGUARD PRO")
        appendLine("==================================================")
        appendLine("RÉFÉRENCE TICKET : $ticketNumber")
        appendLine("HORODATAGE      : $currentDateStr")
        appendLine("STATUT DIAGNOSTIC : ${faultType.severity} (${faultType.title})")
        appendLine("--------------------------------------------------")
        appendLine("1. ÉQUIPEMENT & LIGNE :")
        appendLine(" - Equipement : ${activeModem.name} (${activeModem.provider})")
        appendLine(" - Connexion  : ${activeModem.type}")
        appendLine(" - Prise PTO  : PTO-83920-FI-42")
        appendLine(" - Passerelle : 192.168.1.1 (WAN: 82.65.14.92)")
        appendLine("--------------------------------------------------")
        appendLine("2. MESURES OPTIQUES & RÉSEAU :")
        appendLine(" - Code Erreur Réseau  : ${faultType.errorCode}")
        appendLine(" - Atténuation Optique : ${faultType.opticalAttenuation}")
        appendLine(" - Latence WAN         : ${if (latencyMs < 999) "$latencyMs ms" else "INJOIGNABLE"}")
        appendLine(" - Perte de Paquets    : $packetLossPct %")
        appendLine("--------------------------------------------------")
        appendLine("3. DESCRIPTION DE LA PANNE :")
        appendLine(" ${faultType.description}")
        appendLine("--------------------------------------------------")
        appendLine("4. CONSIGNES & ACTION RECOMMANDÉE :")
        appendLine(" ${faultType.recommendedFix}")
        appendLine("==================================================")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("incident_report_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NetDarkBackground),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NetPrimaryCyan)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NetPrimaryCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = NetPrimaryCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Signalement d'Incident",
                                color = NetTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transmettez l'alerte à votre intégrateur / FAI",
                                color = NetTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = NetTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User-Friendly Summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (faultType) {
                            NetworkFaultType.NONE_ONLINE -> NetOnlineGreen
                            NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> NetAlertRed
                            else -> NetWarningAmber
                        }
                    ),
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
                                    text = "RÉFÉRENCE DOSSIER",
                                    color = NetTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = ticketNumber,
                                    color = NetPrimaryCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            // Simple Status Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (faultType) {
                                            NetworkFaultType.NONE_ONLINE -> NetOnlineGreen.copy(alpha = 0.2f)
                                            NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE -> NetAlertRed.copy(alpha = 0.2f)
                                            else -> NetWarningAmber.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = faultType.severity,
                                    color = when (faultType) {
                                        NetworkFaultType.NONE_ONLINE -> NetOnlineGreen
                                        NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE -> NetAlertRed
                                        else -> NetWarningAmber
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clear Human Description
                        Text(
                            text = faultType.title,
                            color = NetTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = faultType.description,
                            color = NetTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Recommandation simple
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NetDarkBackground)
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NetPrimaryCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = faultType.recommendedFix,
                                    color = NetTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION SMS DIRECT POUR INTÉGRATEUR TÉLÉCOM
                Card(
                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NetWarningAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CellTower,
                                contentDescription = null,
                                tint = NetWarningAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Envoi SMS Secours GSM (Sans Internet)",
                                color = NetTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Si votre ligne Internet ou Wi-Fi est coupée, envoyez ce signalement au technicien via le réseau mobile GSM.",
                            color = NetTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Number Field (Pre-filled with Telecom Integrator 688137007)
                        OutlinedTextField(
                            value = smsRecipientNumber,
                            onValueChange = { smsRecipientNumber = it },
                            label = { Text("N° Technicien / Intégrateur Télécom", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = NetWarningAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NetWarningAmber,
                                unfocusedBorderColor = NetCardBorder,
                                focusedTextColor = NetTextPrimary,
                                unfocusedTextColor = NetTextPrimary,
                                focusedLabelColor = NetWarningAmber,
                                unfocusedLabelColor = NetTextMuted
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_recipient_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Main Send SMS Button
                        Button(
                            onClick = {
                                try {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:${smsRecipientNumber.trim()}")
                                        putExtra("sms_body", compactSmsText)
                                    }
                                    context.startActivity(smsIntent)
                                    Toast.makeText(
                                        context,
                                        "Ouverture SMS pour envoi à $smsRecipientNumber",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Erreur lors de l'ouverture SMS: ${e.localizedMessage}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NetWarningAmber,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("send_sms_gsm_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📲 Envoyer via SMS GSM ($smsRecipientNumber)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // WhatsApp & Email Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // WhatsApp Button
                            Button(
                                onClick = {
                                    try {
                                        val cleanNum = smsRecipientNumber.trim().replace(" ", "").replace("+", "")
                                        val waNumber = if (cleanNum.startsWith("237")) cleanNum else "237$cleanNum"
                                        val waUri = Uri.parse("https://api.whatsapp.com/send?phone=$waNumber&text=${Uri.encode(fullReportText)}")
                                        val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                                        context.startActivity(waIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Impossible d'ouvrir WhatsApp : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetOnlineGreen,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("send_whatsapp_button")
                            ) {
                                Text(text = "💬 WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Email Button
                            Button(
                                onClick = {
                                    try {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:support@netguard.cm")
                                            putExtra(Intent.EXTRA_SUBJECT, "Rapport Incident Réseau #$ticketNumber")
                                            putExtra(Intent.EXTRA_TEXT, fullReportText)
                                        }
                                        context.startActivity(emailIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erreur application E-mail : ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NetSecondaryBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("send_email_button")
                            ) {
                                Text(text = "✉️ E-Mail Support", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Action Buttons (Copy / Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullReportText))
                            Toast.makeText(context, "Fiche d'incident copiée !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NetPrimaryCyan, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("copy_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Copier Fiche", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Incident Réseau - Ticket $ticketNumber")
                                putExtra(Intent.EXTRA_TEXT, fullReportText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager le rapport"))
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NetPrimaryCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("share_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Partager", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TOGGLE OPTION FOR TECHNICAL DETAILS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showTechnicalDetails = !showTechnicalDetails }
                        .background(NetCardSurface)
                        .border(1.dp, NetCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = NetSecondaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Voir les détails techniques avancés",
                            color = NetTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (showTechnicalDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = NetTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // EXPANDABLE TECHNICAL DETAILS SECTION
                AnimatedVisibility(
                    visible = showTechnicalDetails,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "1. Télémétrie Ligne Optique PTO & Box",
                                    color = NetPrimaryCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TechnicalDataRow(label = "Équipement", value = "${activeModem.name} (${activeModem.provider})")
                                TechnicalDataRow(label = "Technologie", value = activeModem.type)
                                TechnicalDataRow(label = "Prise PTO", value = "PTO-83920-FI-42")
                                TechnicalDataRow(label = "Passerelle WAN", value = "192.168.1.1 (IP WAN: 82.65.14.92)")

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "2. Mesures d'Atténuation & Diagnostic",
                                    color = NetPrimaryCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                TechnicalDataRow(label = "Code Erreur", value = faultType.errorCode)
                                TechnicalDataRow(label = "Atténuation Optique", value = faultType.opticalAttenuation)
                                TechnicalDataRow(label = "Latence WAN", value = if (latencyMs < 999) "$latencyMs ms" else "INJOIGNABLE")
                                TechnicalDataRow(label = "Perte Paquets", value = "$packetLossPct %")
                                TechnicalDataRow(
                                    label = "État Voyants",
                                    value = "PWR: ${activeModem.powerLed.state.name} | WAN: ${activeModem.wanLed.state.name} | INET: ${activeModem.internetLed.state.name}"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Raw Text Code Preview Block
                        Text(
                            text = "Rapport Brut (Format Texte) :",
                            color = NetTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF020617))
                                .border(1.dp, NetCardBorder, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = fullReportText,
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TechnicalDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = NetTextSecondary, fontSize = 11.sp)
        Text(text = value, color = NetTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
