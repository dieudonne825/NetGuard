package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OutageLog
import com.example.data.TechnicianTicket
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

@Composable
fun TicketsAndLogsScreen(
    outages: List<OutageLog>,
    tickets: List<TechnicianTicket>,
    onOpenIncidentReport: (TechnicianTicket?) -> Unit = {},
    onUpdateTicketStatus: (Long, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Tickets Techniciens, 1 = Journal Pannes
    val context = LocalContext.current

    // SMS permission state
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Suivi automatique des SMS techniciens activé !", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission SMS refusée. Le suivi reste manuel.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetDarkBackground)
            .padding(16.dp)
            .testTag("tickets_logs_screen")
    ) {
        // Top Header with Export Report Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NetPrimaryCyan,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Suivi Technicien & Cycle Incident",
                        color = NetTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Suivez l'avancement des tickets et le journal d'incidents",
                        color = NetTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            OutlinedButton(
                onClick = { onOpenIncidentReport(null) },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NetPrimaryCyan),
                modifier = Modifier.testTag("global_export_report_button")
            ) {
                Text(text = "📄 Exporter Fiche", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // SMS Auto Sync Optional Option Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = NetCardSurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hasSmsPermission) NetOnlineGreen.copy(alpha = 0.5f) else NetWarningAmber.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = if (hasSmsPermission) NetOnlineGreen else NetWarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (hasSmsPermission) "Suivi SMS Technicien Actif" else "Optionnel : Suivi SMS Automatique",
                            color = NetTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasSmsPermission)
                                "L'application mettra à jour vos tickets dès réception d'un SMS d'assistance."
                            else
                                "Activez pour mettre à jour automatiquement le statut de vos pannes par SMS.",
                            color = NetTextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }
                }

                if (!hasSmsPermission) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS) },
                        colors = ButtonDefaults.buttonColors(containerColor = NetWarningAmber, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("enable_sms_sync_button")
                    ) {
                        Text(text = "Activer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Actif",
                        tint = NetOnlineGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NetCardSurface,
            contentColor = NetPrimaryCyan,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NetPrimaryCyan
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("tab_tickets")
            ) {
                Text(
                    text = "Tickets Techniciens (${tickets.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("tab_outages")
            ) {
                Text(
                    text = "Journal Pannes (${outages.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Tickets List
            if (tickets.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = NetTextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Aucun ticket d'incident en cours.",
                            color = NetTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "En cas d'anomalie réseau, générez un rapport directement via le bouton 'Exporter Fiche' ci-dessus.",
                            color = NetTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tickets) { ticket ->
                        TicketItemCard(
                            ticket = ticket,
                            onOpenIncidentReport = onOpenIncidentReport,
                            onUpdateStatus = { newStatus -> onUpdateTicketStatus(ticket.id, newStatus) }
                        )
                    }
                }
            }
        } else {
            // Outages History List
            if (outages.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NetCardSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune panne réseau enregistrée dans l'historique.",
                        color = NetTextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(outages) { log ->
                        OutageLogItemCard(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItemCard(
    ticket: TechnicianTicket,
    onOpenIncidentReport: (TechnicianTicket) -> Unit = {},
    onUpdateStatus: (String) -> Unit = {}
) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date(ticket.timestamp))
    var showMenu by remember { mutableStateOf(false) }

    val (statusLabel, statusColor) = when (ticket.status) {
        "EN_ATTENTE", "REPORTED" -> "Signalé 📥" to NetWarningAmber
        "TECHNICIEN_ASSIGNE", "ACKNOWLEDGED" -> "Pris en Charge 👨‍🔧" to NetSecondaryBlue
        "INTERVENTION_EN_COURS", "IN_PROGRESS" -> "En Intervention 🛠️" to NetPrimaryCyan
        "RESOLU", "RESOLVED" -> "Résolu ✅" to NetOnlineGreen
        "CLOTURE", "CLOSED" -> "Clôturé 🔒" to NetTextMuted
        else -> ticket.status to NetWarningAmber
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NetCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.ticketNumber,
                    color = NetPrimaryCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )

                // Status Badge with Dropdown Picker
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .clickable { showMenu = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(NetCardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Signalé 📥", color = NetWarningAmber, fontSize = 12.sp) },
                            onClick = { onUpdateStatus("EN_ATTENTE"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Pris en Charge 👨‍🔧", color = NetSecondaryBlue, fontSize = 12.sp) },
                            onClick = { onUpdateStatus("TECHNICIEN_ASSIGNE"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("En Intervention 🛠️", color = NetPrimaryCyan, fontSize = 12.sp) },
                            onClick = { onUpdateStatus("INTERVENTION_EN_COURS"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Résolu ✅", color = NetOnlineGreen, fontSize = 12.sp) },
                            onClick = { onUpdateStatus("RESOLU"); showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clôturé 🔒", color = NetTextMuted, fontSize = 12.sp) },
                            onClick = { onUpdateStatus("CLOTURE"); showMenu = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Motif : " + ticket.faultType, color = NetTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Équipement : " + ticket.modemModel + " ($dateStr)", color = NetTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Détails : " + ticket.description, color = NetTextMuted, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = NetSecondaryBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ticket.estimatedResolution,
                        color = NetSecondaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = { onOpenIncidentReport(ticket) },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetPrimaryCyan),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NetPrimaryCyan),
                    modifier = Modifier.testTag("ticket_export_report_button_${ticket.ticketNumber}")
                ) {
                    Text(text = "📄 Transmettre / Fiche", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OutageLogItemCard(log: OutageLog) {
    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH).format(Date(log.timestamp))
    val isIsp = log.type == "ISP_OUTAGE"
    val color = if (isIsp) NetAlertRed else NetWarningAmber

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NetCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.title, color = NetTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = dateStr, color = NetTextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Opérateur : " + log.ispName, color = NetTextSecondary, fontSize = 12.sp)
            }
        }
    }
}
