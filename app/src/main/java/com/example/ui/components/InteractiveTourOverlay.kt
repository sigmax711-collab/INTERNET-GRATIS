package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TourStepData(
    val stepIndex: Int,
    val category: String,
    val title: String,
    val description: String,
    val tip: String,
    val voiceText: String
)

val TOUR_STEPS = listOf(
    TourStepData(
        stepIndex = 1,
        category = "SERVIDOR",
        title = "Selecciona tu Servidor",
        description = "Elige uno de los 2 servidores disponibles optimizados para conexión segura y de alta velocidad sin saldo.",
        tip = "Tip: Si no tienes saldo en tu línea móvil, estos servidores están optimizados para conectar gratis sin consumir tu crédito.",
        voiceText = "Paso uno. Selecciona tu servidor. Elige entre los dos servidores disponibles según tu preferencia."
    ),
    TourStepData(
        stepIndex = 2,
        category = "CREDENCIALES DE ACCESO",
        title = "Usuario y Contraseña SSH",
        description = "Ingresa tu nombre de usuario. Si utilizas el servidor oficial de Danael, el prefijo danaelssh se agrega de forma automática. También puedes crear tu cuenta gratis en el menú de Configuración con 5 días de duración.",
        tip = "Tip: Las cuentas creadas tienen 5 días de duración y se renuevan gratis.",
        voiceText = "Paso dos. Ingresa tu usuario y contraseña. Si no tienes cuenta, puedes crear una gratis desde el menú de Configuración, tiene cinco días de duración."
    ),
    TourStepData(
        stepIndex = 3,
        category = "MONITOR EN VIVO",
        title = "Velocidad y Latencia (Ping)",
        description = "Monitorea en tiempo real tu velocidad de subida y bajada. El ping cambia según la calidad: Verde (-150ms), Naranja (150-400ms) y Rojo (+400ms).",
        tip = "Tip: Puedes tocar directamente el ping del servidor para probar la latencia al instante.",
        voiceText = "Paso tres. Monitor en vivo. Monitorea en tiempo real tu velocidad de subida y bajada con ondas gráficas interactivas."
    ),
    TourStepData(
        stepIndex = 4,
        category = "CONEXIÓN Y ESTADO",
        title = "Botón Central de Conexión",
        description = "Toca el gran botón central para iniciar el túnel seguro. El anillo pulsante cambiará a verde neón cuando estés conectado y protegido.",
        tip = "Tip: La app te avisará por voz si la conexión fue exitosa o si ocurrió un error de servidor o de credenciales.",
        voiceText = "Paso cuatro. Botón central de conexión. Tócalo para activar la VPN. Te avisaré por voz el estado de tu conexión."
    ),
    TourStepData(
        stepIndex = 5,
        category = "AJUSTES Y HERRAMIENTAS",
        title = "Hotspot VPN y Configuración APN",
        description = "En el menú de configuración puedes compartir tu VPN por Zona Wi-Fi a tu PC o Smart TV, y acceder a los ajustes de APN móvil con un solo toque.",
        tip = "Tip: Toca el icono de engranaje arriba para abrir la configuración avanzada y el ahorro de batería.",
        voiceText = "Paso cinco. Ajustes avanzados. En configuración puedes compartir tu VPN por Hotspot y configurar el APN de tu red móvil."
    )
)

/**
 * Interactive Step-by-Step Guide matching the user screenshot:
 * Features speaking voice narration while guiding ("mientras va hablando cuando va guiando"),
 * step pills, tip boxes, progress dots, and back/next actions.
 */
@Composable
fun InteractiveTourCard(
    currentStep: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onDismiss: () -> Unit,
    onRepeatVoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = TOUR_STEPS.size
    val stepData = TOUR_STEPS.getOrElse(currentStep - 1) { TOUR_STEPS.first() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = Color(0xFF091422),
        border = BorderStroke(1.dp, Color(0xFF1B3654))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila Superior: Badge "PASO X DE 5" + Categoría + Botón Cerrar "X"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge de Paso
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF003844),
                        border = BorderStroke(1.dp, Color(0xFF00F5D4).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "PASO ${stepData.stepIndex} DE $totalSteps",
                            color = Color(0xFF00F5D4),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Categoría
                    Text(
                        text = stepData.category,
                        color = Color(0xFF64B5F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                // Botón Cerrar
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar guía",
                        tint = Color(0xFF8E9AAF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Título del paso
            Text(
                text = stepData.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.3.sp
            )

            // Descripción detallada
            Text(
                text = stepData.description,
                color = Color(0xFFBAC7D5),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontFamily = FontFamily.Default
            )

            // Caja de Tip / Consejo con icono de Rayo ⚡
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF132237),
                border = BorderStroke(1.dp, Color(0xFF1F385A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Tip",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stepData.tip,
                        color = Color(0xFFD0DBE5),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.Default
                    )
                }
            }

            // Indicador de Voz en Vivo (Permite repetir la explicación por voz con un toque)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRepeatVoice(stepData.voiceText) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Voz guiada",
                    tint = Color(0xFF00F5D4),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Voz guiada activa • Toca aquí para repetir explicación",
                    color = Color(0xFF00F5D4),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Fila Inferior: SALTAR + Indicadores de Puntos + Botón Atrás + Botón Siguiente
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botón SALTAR
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "SALTAR",
                        color = Color(0xFF8E9AAF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }

                // Puntos de paso (el actual es una píldora cian alargada)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    for (i in 1..totalSteps) {
                        val isCurrent = i == currentStep
                        Box(
                            modifier = Modifier
                                .height(5.dp)
                                .width(if (isCurrent) 18.dp else 5.dp)
                                .clip(if (isCurrent) RoundedCornerShape(3.dp) else CircleShape)
                                .background(if (isCurrent) Color(0xFF00F5D4) else Color(0xFF263A50))
                        )
                    }
                }

                // Botones Atrás y Siguiente
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Atrás (Flecha izquierda)
                    Surface(
                        modifier = Modifier
                            .size(width = 46.dp, height = 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = currentStep > 1) { onPrev() },
                        color = Color(0xFF15263B),
                        border = BorderStroke(1.dp, Color(0xFF223C5E))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Paso anterior",
                                tint = if (currentStep > 1) Color.White else Color(0xFF50647C),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Botón Siguiente o Finalizar
                    Button(
                        onClick = onNext,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4361EE),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (currentStep < totalSteps) "SIGUIENTE" else "FINALIZAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
