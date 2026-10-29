package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SunGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

/**
 * State holder for displaying custom aesthetic cyber toasts in Jetpack Compose.
 */
class CyberToastState {
    var isVisible by mutableStateOf(false)
        private set
    var message by mutableStateOf("CesarG")
        private set
    var subtitle by mutableStateOf<String?>("Desarrollador Oficial • 🇦🇷")
        private set

    fun show(
        msg: String = "CesarG",
        sub: String? = "Desarrollador Oficial • 🇦🇷",
        context: Context? = null,
        durationMs: Long = 2500
    ) {
        message = msg
        subtitle = sub
        isVisible = true
        // Also show native Android Toast for complete system coverage
        context?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun dismiss() {
        isVisible = false
    }
}

@Composable
fun rememberCyberToastState(): CyberToastState {
    return remember { CyberToastState() }
}

/**
 * Custom cyber-styled Toast notification pill matching the app's aesthetic.
 */
@Composable
fun CyberToastHost(
    toastState: CyberToastState,
    modifier: Modifier = Modifier,
    durationMs: Long = 2500
) {
    LaunchedEffect(toastState.isVisible, toastState.message) {
        if (toastState.isVisible) {
            delay(durationMs)
            toastState.dismiss()
        }
    }

    AnimatedVisibility(
        visible = toastState.isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it - 50 },
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { -it - 50 },
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = CyberCyan.copy(alpha = 0.5f),
                    ambientColor = ArgentineBlue.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(CyberCyan, ArgentineBlue, SunGold, CyberCyan)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { toastState.dismiss() }
                .testTag("cyber_toast_notification"),
            color = Color(0xEB07121F)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xCC050E18),
                                Color(0xE60A1A2E),
                                Color(0xCC050E18)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Crown / Star Icon badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(SunGold.copy(alpha = 0.35f), Color(0x10000000))
                            )
                        )
                        .border(1.dp, SunGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Creador",
                        tint = SunGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = toastState.message,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "👑",
                            fontSize = 12.sp
                        )
                    }

                    toastState.subtitle?.let { sub ->
                        Text(
                            text = sub,
                            color = ArgentineBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
