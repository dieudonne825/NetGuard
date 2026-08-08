package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.model.NetworkFaultType
import com.example.ui.components.IncidentReportDialog
import com.example.ui.components.OutageAlertPopupDialog
import com.example.ui.components.OverlayPermissionPromptDialog
import com.example.ui.components.PermissionsOnboardingDialog
import com.example.ui.components.hasAllRequiredPermissions
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DjangoRegistrationScreen
import com.example.ui.screens.Modem3DVisualScreen
import com.example.ui.screens.SpeedTestScreen
import com.example.ui.screens.TicketsAndLogsScreen
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
import com.example.util.NetGuardOverlayManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: NetworkMonitorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val outages by viewModel.allOutages.collectAsState()
    val speedTests by viewModel.allSpeedTests.collectAsState()
    val tickets by viewModel.allTickets.collectAsState()

    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) }
    var showSimMenu by remember { mutableStateOf(false) }
    var showOverlayPermissionPrompt by remember { mutableStateOf(!NetGuardOverlayManager.hasOverlayPermission(context)) }
    var showPermissionsOnboarding by remember { mutableStateOf(!hasAllRequiredPermissions(context)) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userNotificationMessage) {
        uiState.userNotificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotificationMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = NetPrimaryCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NetGuard Pro",
                            color = NetTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // Active Badge
                        val badgeColor = when (uiState.faultType) {
                            NetworkFaultType.NONE_ONLINE -> NetOnlineGreen
                            NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> NetAlertRed
                            else -> NetWarningAmber
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = uiState.faultType.badgeLabel,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    // Menu to simulate network fault test
                    IconButton(
                        onClick = { showSimMenu = true },
                        modifier = Modifier.testTag("simulation_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Simulations Pannes",
                            tint = NetTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showSimMenu,
                        onDismissRequest = { showSimMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("🛡️ Autorisations & Permissions App", color = NetPrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            onClick = {
                                showSimMenu = false
                                showPermissionsOnboarding = true
                            },
                            modifier = Modifier.testTag("menu_permissions_item")
                        )

                        DropdownMenuItem(
                            text = { Text("📋 Enregistrement Client & Django Backend", color = NetPrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                            onClick = {
                                showSimMenu = false
                                viewModel.showRegistrationDialog()
                            },
                            modifier = Modifier.testTag("menu_django_registration_item")
                        )

                        DropdownMenuItem(
                            text = { Text("🔔 Configurer Alertes Hors-App", color = NetTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            onClick = {
                                showSimMenu = false
                                if (NetGuardOverlayManager.hasOverlayPermission(context)) {
                                    NetGuardOverlayManager.requestOverlayPermission(context)
                                } else {
                                    showOverlayPermissionPrompt = true
                                }
                            },
                            modifier = Modifier.testTag("menu_overlay_perm_item")
                        )

                        NetworkFaultType.values().forEach { fault ->
                            val textColor = when (fault) {
                                NetworkFaultType.NONE_ONLINE -> NetOnlineGreen
                                NetworkFaultType.ISP_OUTAGE, NetworkFaultType.POWER_OUTAGE, NetworkFaultType.MODEM_OVERHEAT -> NetAlertRed
                                else -> NetWarningAmber
                            }
                            val prefix = when (fault) {
                                NetworkFaultType.NONE_ONLINE -> "✅ "
                                NetworkFaultType.POWER_OUTAGE -> "⚡ "
                                NetworkFaultType.MODEM_OVERHEAT -> "🔥 "
                                else -> "⚠️ "
                            }

                            DropdownMenuItem(
                                text = { Text("$prefix${fault.title}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                onClick = {
                                    showSimMenu = false
                                    viewModel.triggerSimulatedFault(fault)
                                },
                                modifier = Modifier.testTag("sim_${fault.name.lowercase()}")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NetDarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NetCardSurface,
                contentColor = NetSecondaryBlue
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Wifi, contentDescription = "Tableau de bord") },
                    label = { Text("Bord", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetSecondaryBlue,
                        selectedTextColor = NetSecondaryBlue,
                        indicatorColor = NetSecondaryBlue.copy(alpha = 0.12f),
                        unselectedIconColor = NetTextMuted,
                        unselectedTextColor = NetTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Router, contentDescription = "Modem 3D") },
                    label = { Text("Modem 3D", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetSecondaryBlue,
                        selectedTextColor = NetSecondaryBlue,
                        indicatorColor = NetSecondaryBlue.copy(alpha = 0.12f),
                        unselectedIconColor = NetTextMuted,
                        unselectedTextColor = NetTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_modem_3d")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Test Débit") },
                    label = { Text("Débit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetSecondaryBlue,
                        selectedTextColor = NetSecondaryBlue,
                        indicatorColor = NetSecondaryBlue.copy(alpha = 0.12f),
                        unselectedIconColor = NetTextMuted,
                        unselectedTextColor = NetTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_speed")
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Suivi & Tickets") },
                    label = { Text("Tickets", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NetSecondaryBlue,
                        selectedTextColor = NetSecondaryBlue,
                        indicatorColor = NetSecondaryBlue.copy(alpha = 0.12f),
                        unselectedIconColor = NetTextMuted,
                        unselectedTextColor = NetTextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_tickets")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    uiState = uiState,
                    onNavigateToSpeedTest = { currentTab = 2 },
                    onNavigateToModem3D = { currentTab = 1 },
                    onReportTechnician = { viewModel.reportTicketToTechnicians() },
                    onSimulateFault = { fault -> viewModel.triggerSimulatedFault(fault) },
                    onOpenIncidentReport = { viewModel.openIncidentReport() },
                    onToggleLiveMode = { enableLive -> viewModel.toggleLiveMode(enableLive) },
                    onOpenRegistration = { viewModel.showRegistrationDialog() }
                )
                1 -> Modem3DVisualScreen(
                    uiState = uiState,
                    onSelectModem = { id -> viewModel.selectModem(id) }
                )
                2 -> SpeedTestScreen(
                    uiState = uiState,
                    history = speedTests,
                    onStartSpeedTest = { viewModel.startSpeedTest() },
                    onSelectServer = { serverName -> viewModel.selectSpeedTestServer(serverName) }
                )
                3 -> TicketsAndLogsScreen(
                    outages = outages,
                    tickets = tickets,
                    onOpenIncidentReport = { ticket -> viewModel.openIncidentReport(ticket) },
                    onUpdateTicketStatus = { id, status -> viewModel.updateTicketStatus(id, status) }
                )
            }

            // Real-Time High Priority Outage Alert Popup Modal
            if (uiState.showOutagePopup && uiState.faultType != NetworkFaultType.NONE_ONLINE) {
                OutageAlertPopupDialog(
                    faultType = uiState.faultType,
                    message = uiState.outagePopupMessage,
                    timestampFormatted = uiState.outageTimestampFormatted,
                    onDismiss = { viewModel.dismissPopup() },
                    onReportTechnician = { viewModel.reportTicketToTechnicians() },
                    onInspectModem = {
                        viewModel.dismissPopup()
                        currentTab = 1 // Navigate to 3D modem view
                    },
                    onExportIncidentReport = { viewModel.openIncidentReport() }
                )
            }

            // Export Incident Report Dialog
            if (uiState.showIncidentReportDialog) {
                IncidentReportDialog(
                    faultType = uiState.faultType,
                    activeModem = uiState.activeModemModel,
                    latencyMs = uiState.latencyMs,
                    packetLossPct = uiState.packetLossPct,
                    outagesHistory = outages,
                    ticket = uiState.selectedReportTicket,
                    onDismiss = { viewModel.closeIncidentReport() }
                )
            }

            // Permissions Onboarding Dialog on Launch
            if (showPermissionsOnboarding && !uiState.showDjangoRegistrationDialog) {
                PermissionsOnboardingDialog(
                    onDismiss = { showPermissionsOnboarding = false },
                    onAllPermissionsGranted = {
                        showPermissionsOnboarding = false
                        Toast.makeText(context, "Toutes les autorisations sont actives !", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Overlay Permission Prompt Dialog on Launch
            if (showOverlayPermissionPrompt && !NetGuardOverlayManager.hasOverlayPermission(context) && !uiState.showDjangoRegistrationDialog && !showPermissionsOnboarding) {
                OverlayPermissionPromptDialog(
                    onDismiss = { showOverlayPermissionPrompt = false }
                )
            }

            // Django Registration Dedicated Full-Screen Onboarding Modal
            if (uiState.showDjangoRegistrationDialog) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { viewModel.dismissRegistrationDialog() },
                    properties = androidx.compose.ui.window.DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    DjangoRegistrationScreen(
                        initialProfile = uiState.clientProfile,
                        isAlreadyRegistered = uiState.isRegisteredWithDjango,
                        onCompleteRegistration = { profile ->
                            viewModel.saveClientRegistration(profile)
                        },
                        onDismissRequest = { viewModel.dismissRegistrationDialog() }
                    )
                }
            }
        }
    }
}
