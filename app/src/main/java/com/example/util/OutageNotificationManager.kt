package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.model.NetworkFaultType
import com.example.ui.components.errorCode

object OutageNotificationManager {

    private const val CHANNEL_ID = "netguard_outage_alerts_channel"
    private const val CHANNEL_NAME = "Alertes de Panne Réseau NetGuard"
    private const val NOTIFICATION_ID = 99881

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification prioritaire affichée au-dessus des autres applications lors d'une panne réseau."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun showOutageSystemNotification(context: Context, faultType: NetworkFaultType, modemName: String) {
        createNotificationChannel(context)

        // Intent to open NetGuard App
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to send SMS to Telecom Integrator (688137007)
        val smsText = "[NetGuard] Diagnostic: ${faultType.title} (Code: ${faultType.errorCode}) - Box: $modemName"
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:688137007")
            putExtra("sms_body", smsText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val smsPendingIntent = PendingIntent.getActivity(
            context,
            1,
            smsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isMinorOrTransient = faultType == NetworkFaultType.WIFI_INTERFERENCE || faultType == NetworkFaultType.FIBER_SIGNAL_LOW
        val notifTitle = if (isMinorOrTransient) "NetGuard • Signal Atténué" else "NetGuard • Alerte Réseau"
        val notifSummary = if (isMinorOrTransient) {
            "Perturbation ou baisse de signal Wi-Fi détectée sur $modemName."
        } else {
            "Incident détecté : ${faultType.title}."
        }

        val bigTextContent = buildString {
            append("Équipement : $modemName\n")
            append("Diagnostic : ${faultType.title}\n\n")
            append("${faultType.description}\n\n")
            append("💡 Conseil : ${faultType.recommendedFix}")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(notifTitle)
            .setContentText(notifSummary)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextContent))
            .setPriority(if (isMinorOrTransient) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setColor(0xFF00B4D8.toInt()) // NetGuard Accent Cyan
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_send,
                "Signaler par SMS",
                smsPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "Consulter NetGuard",
                appPendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            notificationManager?.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
    }

    fun showSmsTicketUpdateNotification(
        context: Context,
        sender: String,
        message: String,
        ticketRef: String,
        readableStatus: String
    ) {
        createNotificationChannel(context)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            2,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("NetGuard • Suivi Ticket $ticketRef")
            .setContentText("Statut mis à jour : $readableStatus")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Ticket : $ticketRef\nStatut : $readableStatus\nSupport : $sender\nMessage : $message")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(0xFF0284C7.toInt())
            .setAutoCancel(true)
            .setContentIntent(appPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        try {
            notificationManager?.notify((NOTIFICATION_ID + 1..NOTIFICATION_ID + 1000).random(), builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
