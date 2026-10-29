package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.VpnState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.WarningYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ConnectButton(
    vpnState: VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_transition")

    val rotateSlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_slow"
    )

    val rotateReverseSlow by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_reverse_slow"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val mainColor = when (vpnState) {
        VpnState.CONNECTED -> CyberCyan
        VpnState.CONNECTING, VpnState.RECONNECTING, VpnState.WAITING, VpnState.UPDATING -> WarningYellow
        VpnState.DISCONNECTED -> ArgentineBlue
    }

    val boltColor = when (vpnState) {
        VpnState.CONNECTED -> CyberCyan
        VpnState.CONNECTING, VpnState.RECONNECTING, VpnState.WAITING, VpnState.UPDATING -> WarningYellow
        VpnState.DISCONNECTED -> ArgentineBlue
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val outerRadius = size.toPx() / 2f - 4.dp.toPx()
            val innerRadius = outerRadius * 0.8f

            // Inner subtle background glow
            drawCircle(
                color = mainColor.copy(alpha = if (vpnState == VpnState.CONNECTED) 0.12f * pulseGlow else 0.06f),
                radius = outerRadius,
                center = center,
                style = Fill
            )

            // Outer rotating hexagon / polygon ring
            rotate(rotateSlow, pivot = center) {
                val hexPath = Path()
                val sides = 6
                for (i in 0 until sides) {
                    val angle = Math.toRadians((i * (360.0 / sides)).toDouble())
                    val x = (center.x + outerRadius * cos(angle)).toFloat()
                    val y = (center.y + outerRadius * sin(angle)).toFloat()
                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                }
                hexPath.close()

                drawPath(
                    path = hexPath,
                    color = mainColor.copy(alpha = 0.7f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Inner reverse-rotating dashed ring
            rotate(rotateReverseSlow, pivot = center) {
                drawCircle(
                    color = (if (vpnState == VpnState.CONNECTED) ArgentineBlue else CyberCyan).copy(alpha = 0.8f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )
                )
            }
        }

        // Center Bolt Icon
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Conectar / Desconectar",
            tint = boltColor,
            modifier = Modifier.size(size * 0.45f)
        )
    }
}
