package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.NetCardBorder
import com.example.ui.theme.NetCardSurface
import com.example.ui.theme.NetDarkBackground
import com.example.ui.theme.NetOnlineGreen
import com.example.ui.theme.NetPrimaryCyan
import com.example.ui.theme.NetSecondaryBlue
import com.example.ui.theme.NetTextMuted
import com.example.ui.theme.NetTextPrimary
import com.example.ui.theme.NetTextSecondary
import com.example.util.NetGuardOverlayManager

/**
 * Utility function to check if all required application permissions are granted.
 */
fun hasAllRequiredPermissions(context: Context): Boolean {
    val hasGps = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true

    return hasGps && hasSms && hasNotif
}

@Composable
fun PermissionsOnboardingDialog(
    onDismiss: () -> Unit,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    var hasGps by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    var hasSms by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotif by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var hasOverlay by remember {
        mutableStateOf(NetGuardOverlayManager.hasOverlayPermission(context))
    }

    // Permission launcher for system runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasGps = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotif = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }

        if (hasGps && hasSms && hasNotif) {
            onAllPermissionsGranted()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.5.dp, NetPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .testTag("permissions_onboarding_dialog"),
            color = NetCardSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(NetPrimaryCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Autorisations requises",
                        tint = NetPrimaryCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Autorisations Requises 🛡️",
                    color = NetTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pour assurer la surveillance continue de votre modem NetGuard et vous alerter en cas de panne, l'application a besoin des autorisations suivantes :",
                    color = NetTextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Permission Item 1: GPS & Wi-Fi
                PermissionCardItem(
                    icon = Icons.Default.LocationOn,
                    iconColor = NetOnlineGreen,
                    title = "Localisation GPS & Wi-Fi",
                    description = "Détection du signal Wi-Fi du modem et coordonnées précises pour le dépannage.",
                    isGranted = hasGps
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permission Item 2: Notifications
                PermissionCardItem(
                    icon = Icons.Default.Notifications,
                    iconColor = NetSecondaryBlue,
                    title = "Notifications Système",
                    description = "Alertes instantanées en cas d'interruption du service fibre ou de surchauffe modem.",
                    isGranted = hasNotif
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permission Item 3: SMS Diagnostics
                PermissionCardItem(
                    icon = Icons.Default.Sms,
                    iconColor = NetPrimaryCyan,
                    title = "Réception SMS de Secours",
                    description = "Réception automatique des codes de diagnostic hors-ligne si la fibre tombe.",
                    isGranted = hasSms
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permission Item 4: Overlay Display
                PermissionCardItem(
                    icon = Icons.Default.Window,
                    iconColor = Color(0xFFFFB74D),
                    title = "Affichage en Superposition",
                    description = "Affichage des fenêtres d'urgence au-dessus des autres applications lors des pannes.",
                    isGranted = hasOverlay,
                    onGrantClick = {
                        NetGuardOverlayManager.requestOverlayPermission(context)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Request Button
                val allRuntimeGranted = hasGps && hasSms && hasNotif

                Button(
                    onClick = {
                        if (!allRuntimeGranted) {
                            val perms = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.RECEIVE_SMS
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                perms.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(perms.toTypedArray())
                        } else {
                            onAllPermissionsGranted()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("grant_all_permissions_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allRuntimeGranted) NetOnlineGreen else NetPrimaryCyan,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (allRuntimeGranted) "Toutes les autorisations accordées ✅" else "Accorder les autorisations 🔐",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("skip_permissions_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NetCardBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = NetCardSurface,
                        contentColor = NetTextPrimary
                    )
                ) {
                    Text(
                        text = "Continuer vers l'application",
                        color = NetTextMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionCardItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NetDarkBackground),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) NetOnlineGreen.copy(alpha = 0.4f) else NetCardBorder
        )
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
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = NetTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Accordé",
                            tint = NetOnlineGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = NetTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (!isGranted && onGrantClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "Activer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
