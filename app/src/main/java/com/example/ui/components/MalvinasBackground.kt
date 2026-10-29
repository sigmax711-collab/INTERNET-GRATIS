package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBackgroundEnd

@Composable
fun MalvinasBackground(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Fondo visual cyberpunk de alta definición con circuitos y neón
        Image(
            painter = painterResource(id = R.drawable.img_cyber_vpn_bg),
            contentDescription = "Cyber VPN Futuristic Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.55f
        )

        // Degradado cibernético para garantizar legibilidad perfecta de todos los paneles y textos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CyberBackground.copy(alpha = 0.82f),
                            Color(0xFF040E1B).copy(alpha = 0.60f),
                            CyberBackgroundEnd.copy(alpha = 0.88f)
                        )
                    )
                )
        )
    }
}
