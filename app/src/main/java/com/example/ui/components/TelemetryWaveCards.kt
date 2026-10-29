package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkTelemetry
import com.example.model.VpnState
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/**
 * Modern telemetry indicators matching the user screenshot:
 * - Left card: Download wave (Cyan #00F5D4), download cloud icon, current speed, total downloaded.
 * - Right card: Upload wave (Pink #FF2A85), upload cloud icon, current speed, total uploaded.
 * - Supports animated focus border for the interactive step-by-step tour.
 */
@Composable
fun TelemetryWaveCards(
    telemetry: NetworkTelemetry,
    vpnState: VpnState,
    isTourHighlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isConnected = vpnState == VpnState.CONNECTED

    val cardBorder = if (isTourHighlighted) {
        BorderStroke(2.dp, Color(0xFF00F5D4))
    } else {
        BorderStroke(1.dp, Color(0xFF142436))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main container around both cards
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.Transparent,
            border = cardBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isTourHighlighted) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tarjeta de Descarga (Cyan)
                TelemetrySingleCard(
                    title = "Descarga",
                    speed = telemetry.downloadSpeed,
                    totalBytes = telemetry.totalDownloadBytes,
                    isDownload = true,
                    isActive = isConnected,
                    themeColor = Color(0xFF00F5D4),
                    modifier = Modifier.weight(1f)
                )

                // Tarjeta de Subida (Rosa Neón)
                TelemetrySingleCard(
                    title = "Subida",
                    speed = telemetry.uploadSpeed,
                    totalBytes = telemetry.totalUploadBytes,
                    isDownload = false,
                    isActive = isConnected,
                    themeColor = Color(0xFFFF2A85),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Flecha indicadora hacia arriba si el tour está apuntando a este componente
        if (isTourHighlighted) {
            Spacer(modifier = Modifier.height(4.dp))
            Canvas(modifier = Modifier.size(width = 18.dp, height = 10.dp)) {
                val triangle = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(path = triangle, color = Color(0xFF00F5D4))
            }
        }
    }
}

@Composable
private fun TelemetrySingleCard(
    title: String,
    speed: String,
    totalBytes: Long,
    isDownload: Boolean,
    isActive: Boolean,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "waveTransition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 2200 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Surface(
        modifier = modifier
            .height(115.dp)
            .clip(RoundedCornerShape(14.dp)),
        color = Color(0xFF0C1927),
        border = BorderStroke(1.dp, Color(0xFF162A40))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Onda animada en la parte superior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val path = Path()
                    val points = 32
                    val step = size.width / points
                    val centerY = size.height * 0.5f
                    val amplitude = if (isActive) size.height * 0.32f else size.height * 0.12f

                    for (i in 0..points) {
                        val x = i * step
                        val normalizedX = (x / size.width) * (2.8f * PI.toFloat())
                        val y = centerY + sin(normalizedX + phase) * amplitude
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Resplandor exterior (Glow)
                    drawPath(
                        path = path,
                        color = themeColor.copy(alpha = 0.28f),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Trazo nítido principal
                    drawPath(
                        path = path,
                        color = themeColor,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Fila con Icono de Nube + Título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isDownload) Icons.Default.CloudDownload else Icons.Default.CloudUpload,
                    contentDescription = title,
                    tint = themeColor,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = title,
                    color = Color(0xFFBAC7D5),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Velocidad actual en texto grande y en negrita
            Text(
                text = speed,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            // Consumo total acumulado
            Text(
                text = "Total: ${formatTotalBytes(totalBytes)}",
                color = themeColor.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatTotalBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.US, "%.2f GB", gb)
}
