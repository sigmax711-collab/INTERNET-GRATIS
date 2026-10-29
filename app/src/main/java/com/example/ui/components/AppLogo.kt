package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.SunGold

@Composable
fun AppLogo(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scale = w / 512f

        // Outer circular border with Argentine glow
        drawCircle(
            color = ArgentineBlue.copy(alpha = 0.4f),
            radius = w * 0.46f,
            center = Offset(w / 2f, h / 2f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Shield
        val shieldPath = Path().apply {
            moveTo(256f * scale, 70f * scale)
            lineTo(390f * scale, 140f * scale)
            cubicTo(
                390f * scale, 280f * scale,
                256f * scale, 395f * scale,
                256f * scale, 395f * scale
            )
            cubicTo(
                256f * scale, 395f * scale,
                122f * scale, 280f * scale,
                122f * scale, 140f * scale
            )
            close()
        }

        drawPath(
            path = shieldPath,
            color = CyberSurface,
            style = Fill
        )
        drawPath(
            path = shieldPath,
            color = ArgentineBlue,
            style = Stroke(width = 2.dp.toPx())
        )

        // Sun of May
        drawCircle(
            color = SunGold,
            radius = 32f * scale,
            center = Offset(256f * scale, 220f * scale)
        )

        // Lightning Bolt
        val boltPath = Path().apply {
            moveTo(275f * scale, 125f * scale)
            lineTo(185f * scale, 260f * scale)
            lineTo(245f * scale, 260f * scale)
            lineTo(225f * scale, 385f * scale)
            lineTo(335f * scale, 235f * scale)
            lineTo(270f * scale, 235f * scale)
            close()
        }

        drawPath(
            path = boltPath,
            color = CyberCyan,
            style = Fill
        )
        drawPath(
            path = boltPath,
            color = Color.White,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
