package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.ConnectivityState
import com.example.ui.theme.NetOnlineGreen

val StatusGreen = NetOnlineGreen

@Composable
fun HeartbeatIndicator(
  state: ConnectivityState,
  modifier: Modifier = Modifier,
  sizeDp: Dp = 56.dp
) {
  // Couleur et rythme réglés selon l'état du réseau (1400ms pour HEALTHY)
  val (color, bg, pulseDurationMs) = when (state) {
    ConnectivityState.HEALTHY -> Triple(StatusGreen, Color(0xFFDCFCE7), 1400)
    ConnectivityState.DEGRADED -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), 800)
    ConnectivityState.WIFI_LOSS -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), 800)
    ConnectivityState.TOTAL_LOSS -> Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), 400)
  }

  val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")

  // Animation de contraction/dilatation du cœur central
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = if (state == ConnectivityState.TOTAL_LOSS) 1.25f else 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(pulseDurationMs / 2, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  // Animation d'expansion de l'onde de choc extérieure
  val ringScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 2.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(pulseDurationMs, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ringScale"
  )

  val ringAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(pulseDurationMs, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ringAlpha"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.size(sizeDp * 2.2f)
    ) {
      // 1. Anneau d'onde pulsée extérieure
      Box(
        modifier = Modifier
          .size(sizeDp)
          .scale(ringScale)
          .background(color.copy(alpha = ringAlpha * 0.3f), CircleShape)
      )

      // 2. Cœur central avec icône Favorite
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(sizeDp)
          .scale(pulseScale)
          .background(bg, CircleShape)
          .border(2.dp, color.copy(alpha = 0.3f), CircleShape)
      ) {
        Icon(
          imageVector = Icons.Filled.Favorite,
          contentDescription = "Heartbeat",
          tint = color,
          modifier = Modifier.size(sizeDp * 0.45f)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // 3. Spectre vertical de barres de signal animées
    Row(
      horizontalArrangement = Arrangement.spacedBy(3.dp),
      verticalAlignment = Alignment.Bottom,
      modifier = Modifier.height(20.dp)
    ) {
      val heights = listOf(0.4f, 0.7f, 1.0f, 0.7f, 0.4f, 0.9f, 0.5f)
      heights.forEachIndexed { index, targetHeight ->
        val barScale by infiniteTransition.animateFloat(
          initialValue = 0.3f,
          targetValue = targetHeight,
          animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMs, delayMillis = index * 120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
          ),
          label = "bar_$index"
        )
        Box(
          modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(barScale)
            .background(color, RoundedCornerShape(2.dp))
        )
      }
    }
  }
}
