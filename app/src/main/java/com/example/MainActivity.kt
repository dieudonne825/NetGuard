package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.NetworkMonitorViewModel
import com.example.ui.theme.NetGuardTheme
import com.example.util.OutageNotificationManager

class MainActivity : ComponentActivity() {
  private val viewModel: NetworkMonitorViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Create Notification Channel for System Alerts
    OutageNotificationManager.createNotificationChannel(this)

    // Request Notification Permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }
    }

    setContent {
      NetGuardTheme {
        MainScreen(viewModel = viewModel)
      }
    }
  }
}

