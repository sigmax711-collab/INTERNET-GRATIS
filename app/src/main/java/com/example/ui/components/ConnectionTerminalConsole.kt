package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.VpnState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningYellow

/**
 * Componente visual de Consola/Terminal en tiempo real para la pantalla principal.
 * Muestra el flujo de conexión del túnel, estado en vivo, y resalta errores de respuesta
 * de servidor (HTTP 502, 500, 403, 307, Connection Reset, Timeouts) para facilitar el debug.
 */
@Composable
fun ConnectionTerminalConsole(
    logs: List<LogEntry>,
    vpnState: VpnState,
    isAutoScroll: Boolean,
    onToggleAutoScroll: () -> Unit,
    onClearLogs: () -> Unit,
    onOpenFullLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto scroll al recibir nuevas líneas
    LaunchedEffect(logs.size, isAutoScroll) {
        if (isAutoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    // Identificar último error crítico o de respuesta de servidor para el banner de depuración
    val lastErrorLog = remember(logs) {
        logs.findLast { entry ->
            entry.type == LogType.ERROR ||
                    entry.message.contains("Error", ignoreCase = true) ||
                    entry.message.contains("502", ignoreCase = true) ||
                    entry.message.contains("500", ignoreCase = true) ||
                    entry.message.contains("403", ignoreCase = true) ||
                    entry.message.contains("307", ignoreCase = true) ||
                    entry.message.contains("Connection reset", ignoreCase = true) ||
                    entry.message.contains("Sin respuesta", ignoreCase = true)
        }
    }

    // Color del estado del túnel
    val stateDotColor = when (vpnState) {
        VpnState.CONNECTED -> NeonGreen
        VpnState.CONNECTING, VpnState.RECONNECTING, VpnState.WAITING, VpnState.UPDATING -> WarningYellow
        VpnState.DISCONNECTED -> if (lastErrorLog != null && logs.isNotEmpty()) AlertRed else ArgentineBlue
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .animateContentSize()
            .testTag("connection_terminal_console"),
        color = Color(0xFF030712)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // === BARRA SUPERIOR DE LA TERMINAL ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1D))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Título e indicador de estado en vivo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(stateDotColor)
                    )
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "TERMINAL DEBUG",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "[${vpnState.name}]",
                        color = stateDotColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Botones de acción rápida
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Copiar últimos logs
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val text = logs.takeLast(30).joinToString("\n") { "${it.timestamp} - ${it.message}" }
                            clipboard.setPrimaryClip(ClipData.newPlainText("Logs VPN", text))
                            Toast.makeText(context, "Registro copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar logs",
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Limpiar
                    IconButton(
                        onClick = onClearLogs,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar terminal",
                            tint = TextGray,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Expandir / Reducir altura del componente
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Reducir" else "Expandir",
                            tint = ArgentineBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // === BANNER DE ERROR DESTACADO (SI HUBO RESPUESTA ANORMAL DEL SERVIDOR) ===
            if (lastErrorLog != null && vpnState != VpnState.CONNECTED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AlertRed.copy(alpha = 0.12f))
                        .border(width = 0.5.dp, color = AlertRed.copy(alpha = 0.35f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = AlertRed,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "ÚLTIMA RESPUESTA: ${lastErrorLog.message}",
                        color = AlertRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // === CUERPO DE LA CONSOLA (STREAM DE LOGS EN TIEMPO REAL) ===
            val terminalHeight = if (isExpanded) 240.dp else 135.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(terminalHeight)
                    .background(Color(0xFF030712))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                if (logs.isEmpty()) {
                    // Estado vacío con cursor parpadeante
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "> Terminal lista para captura de túnel _",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Presione [CONECTAR] para ver el handshake en vivo",
                            color = Color(0xFF475569),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(logs) { index, log ->
                            val isError = log.type == LogType.ERROR ||
                                    log.message.contains("Error", ignoreCase = true) ||
                                    log.message.contains("502", ignoreCase = true) ||
                                    log.message.contains("500", ignoreCase = true) ||
                                    log.message.contains("403", ignoreCase = true) ||
                                    log.message.contains("307", ignoreCase = true) ||
                                    log.message.contains("Connection reset", ignoreCase = true)

                            val isSuccess = log.type == LogType.SUCCESS ||
                                    log.message.contains("101") ||
                                    log.message.contains("establecido con éxito") ||
                                    log.message.contains("SSH-2.0")

                            val textColor = when {
                                isError -> AlertRed
                                isSuccess -> NeonGreen
                                log.type == LogType.HIGHLIGHT -> CyberCyan
                                else -> Color(0xFFCBD5E1)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isError) AlertRed.copy(alpha = 0.08f) else Color.Transparent,
                                        RoundedCornerShape(2.dp)
                                    )
                                    .padding(vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = log.timestamp,
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isError || isSuccess) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // === BARRA INFERIOR DE ESTADO Y SCROLL ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0F1D))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Líneas: ${logs.size}",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isAutoScroll) "AUTO-SCROLL: ON" else "AUTO-SCROLL: OFF",
                        color = if (isAutoScroll) NeonGreen else Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleAutoScroll() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
