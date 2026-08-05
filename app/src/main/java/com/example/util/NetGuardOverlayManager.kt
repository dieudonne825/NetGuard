package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.model.NetworkFaultType
import com.example.ui.components.errorCode

object NetGuardOverlayManager {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                Toast.makeText(context, "Activez 'Autoriser la superposition' pour NetGuard", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showSystemOverlay(context: Context, faultType: NetworkFaultType, modemName: String) {
        if (faultType == NetworkFaultType.NONE_ONLINE) {
            hideSystemOverlay(context)
            return
        }

        // Always show the Heads-Up notification as well
        OutageNotificationManager.showOutageSystemNotification(context, faultType, modemName)

        if (!hasOverlayPermission(context)) {
            // Cannot show WindowManager overlay without permission
            return
        }

        Handler(Looper.getMainLooper()).post {
            try {
                if (overlayView != null) {
                    hideSystemOverlay(context)
                }

                windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    ?: return@post

                val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    y = 60
                }

                // Construct programmatic overlay layout if XML layout inflater is missing
                val container = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(36, 32, 36, 32)
                    background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(0xFF0F172A.toInt()) // NetDarkBackground
                        setStroke(4, 0xFFEF4444.toInt()) // Red border
                        cornerRadius = 40f
                    }
                }

                // Header Row
                val headerRow = android.widget.LinearLayout(context).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }

                val titleText = TextView(context).apply {
                    text = "🚨 ALERTE HORS-APP: PANNE RÉSEAU"
                    setTextColor(0xFFEF4444.toInt())
                    textSize = 15f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val closeBtn = Button(context).apply {
                    text = "✕"
                    setTextColor(0xFF94A3B8.toInt())
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnClickListener {
                        hideSystemOverlay(context)
                    }
                }

                headerRow.addView(titleText)
                headerRow.addView(closeBtn)
                container.addView(headerRow)

                // Message Text
                val descText = TextView(context).apply {
                    text = "Panne : ${faultType.title}\nBox : $modemName\nDetail : ${faultType.description}"
                    setTextColor(0xFFF8FAFC.toInt())
                    textSize = 13f
                    setPadding(0, 12, 0, 16)
                }
                container.addView(descText)

                // Action SMS Button
                val smsBtn = Button(context).apply {
                    text = "📲 ENVOYER SMS SIGNALEMENT (688137007)"
                    setTextColor(0xFF000000.toInt())
                    setBackgroundColor(0xFFF59E0B.toInt()) // Amber
                    setOnClickListener {
                        try {
                            val smsText = "[NETGUARD] INCIDENT: ${faultType.title} (Code: ${faultType.errorCode}) Box: $modemName"
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:688137007")
                                putExtra("sms_body", smsText)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(smsIntent)
                            hideSystemOverlay(context)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur SMS: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                container.addView(smsBtn)

                overlayView = container
                windowManager?.addView(overlayView, params)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun hideSystemOverlay(context: Context) {
        Handler(Looper.getMainLooper()).post {
            try {
                if (overlayView != null && windowManager != null) {
                    windowManager?.removeView(overlayView)
                    overlayView = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
