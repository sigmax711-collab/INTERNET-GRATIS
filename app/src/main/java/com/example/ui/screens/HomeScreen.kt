package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.VpnState
import com.example.ui.components.AccountCreatorModal
import com.example.ui.components.ApnModal
import com.example.ui.components.AppLogo
import com.example.ui.components.AppUpdateModal
import com.example.ui.components.CloudUrlModal
import com.example.ui.components.CloudFrontModal
import com.example.ui.components.ConnectButton
import com.example.ui.components.ConnectionTerminalConsole
import com.example.ui.components.CreatorDialog
import com.example.ui.components.CyberToastHost
import com.example.ui.components.GuideModal
import com.example.ui.components.HotspotModal
import com.example.ui.components.InteractiveTourCard
import com.example.ui.components.LogsModal
import com.example.ui.components.MalvinasBackground
import com.example.ui.components.PayloadModal
import com.example.ui.components.ServerSelectorModal
import com.example.ui.components.SettingsModal
import com.example.ui.components.TelemetryWaveCards
import com.example.ui.components.rememberCyberToastState
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.ArgentineBlueLight
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SunGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningYellow
import com.example.viewmodel.VpnViewModel

@Composable
fun HomeScreen(viewModel: VpnViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val toastState = rememberCyberToastState()
    var isPasswordVisible by remember { mutableStateOf(true) }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onVpnPermissionGranted(context)
        } else {
            viewModel.onVpnPermissionDenied()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent != null) {
            vpnPermissionLauncher.launch(prepareIntent)
        } else {
            viewModel.onVpnPermissionGranted(context)
        }
    }

    LaunchedEffect(Unit) {
        toastState.show("CesarG", "Desarrollador Oficial • 🇦🇷", context)
        viewModel.vpnPermissionEvents.collect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasNotificationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasNotificationPermission) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    return@collect
                }
            }

            val prepareIntent = VpnService.prepare(context)
            if (prepareIntent != null) {
                vpnPermissionLauncher.launch(prepareIntent)
            } else {
                viewModel.onVpnPermissionGranted(context)
            }
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissSnackbar()
        }
    }

    val isConnected = uiState.vpnState == VpnState.CONNECTED
    val isBusy = uiState.vpnState == VpnState.CONNECTING || uiState.vpnState == VpnState.RECONNECTING || uiState.vpnState == VpnState.WAITING || uiState.vpnState == VpnState.UPDATING

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = CyberBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fondo Sutil Malvinas
            MalvinasBackground()

            // Contenedor Principal Minimalista
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Barra Superior Minimalista (Solo 2 botones espaciados, sin solapamientos)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + Nombre + Versión
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clickable { viewModel.setShowCreatorDialog(true) }
                    ) {
                        AppLogo(size = 28.dp)
                        Column {
                            Text(
                                text = "INTERNET GRATIS 🇦🇷",
                                color = TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "v1.0.0",
                                color = CyberCyan.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Botones de Acción Superiores (Guía, Ayuda, Sonido, Configuración)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Guía Interactiva
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, CyberCyan.copy(alpha = 0.6f), CircleShape)
                                .clickable { viewModel.startGuidedTour() }
                                .testTag("tour_guide_button"),
                            color = CyberCyan.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = "Guía Interactiva",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Botón Ayuda / Tutorial
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, ArgentineBlue.copy(alpha = 0.5f), CircleShape)
                                .clickable { viewModel.setShowGuideModal(true) }
                                .testTag("help_button"),
                            color = Color(0xFF091826)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Ayuda",
                                    tint = ArgentineBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Botón Sonido
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    if (uiState.soundEnabled) CyberCyan.copy(alpha = 0.7f) else TextGray.copy(alpha = 0.4f),
                                    CircleShape
                                )
                                .clickable { viewModel.toggleSound() }
                                .testTag("sound_toggle_button"),
                            color = if (uiState.soundEnabled) CyberCyan.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.4f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                    contentDescription = "Sonido",
                                    tint = if (uiState.soundEnabled) CyberCyan else TextGray,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }

                        // Botón Configuración
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, ArgentineBlue.copy(alpha = 0.6f), CircleShape)
                                .clickable { viewModel.setShowSettingsModal(true) }
                                .testTag("settings_button"),
                            color = Color(0xFF0A1926)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Configuración",
                                    tint = ArgentineBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Banner de Título
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "INTERNET",
                            color = TextWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "GRATIS",
                            color = ArgentineBlue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "🇦🇷 Las Malvinas Son Argentinas 🇦🇷",
                        color = CyberCyan.copy(alpha = 0.85f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // 3. Hero Central: Botón de Conexión + Estado + Duración
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConnectButton(
                        vpnState = uiState.vpnState,
                        onClick = {
                            toastState.show("CesarG", "Desarrollador Oficial • 🇦🇷", context)
                            viewModel.toggleVpn()
                        },
                        size = 120.dp,
                        modifier = Modifier.testTag("connect_central_button")
                    )

                    // Etiqueta de Estado de Conexión
                    Text(
                        text = when (uiState.vpnState) {
                            VpnState.CONNECTED -> "CONECTADO • SEGURO"
                            VpnState.CONNECTING, VpnState.WAITING -> "CONECTANDO..."
                            VpnState.RECONNECTING -> "RECONECTANDO AUTOMÁTICAMENTE..."
                            VpnState.UPDATING -> "ACTUALIZANDO..."
                            VpnState.DISCONNECTED -> "TOCA PARA CONECTAR"
                        },
                        color = when {
                            isConnected -> NeonGreen
                            isBusy -> WarningYellow
                            else -> ArgentineBlue
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    // Badge de 5 Días de Duración
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isConnected) NeonGreen.copy(alpha = 0.12f) else CyberCyan.copy(alpha = 0.1f),
                        border = BorderStroke(0.8.dp, if (isConnected) NeonGreen.copy(alpha = 0.5f) else CyberCyan.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(text = "⏱️", fontSize = 10.sp)
                            Text(
                                text = if (isConnected) "5 DÍAS DE DURACIÓN • ACTIVO" else "VÁLIDO POR 5 DÍAS",
                                color = if (isConnected) NeonGreen else CyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 4. Controles Principales Minimalistas
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarjeta 1: Sección de Servidores (Personal y Claro separados)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isClaroActive = uiState.selectedServer?.name?.contains("Claro", ignoreCase = true) == true ||
                                uiState.selectedServer?.host?.contains("claro", ignoreCase = true) == true

                        // Pestañas de Selección de Operadora: Personal y Claro
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pestaña Personal
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (!isClaroActive) 1.5.dp else 1.dp,
                                        color = if (!isClaroActive) CyberCyan else CyberCyan.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectCarrier(isClaro = false) }
                                    .testTag("carrier_tab_personal"),
                                color = if (!isClaroActive) CyberCyan.copy(alpha = 0.18f) else CyberPanel
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🩵 PERSONAL",
                                        color = if (!isClaroActive) CyberCyan else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (!isClaroActive) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Pestaña Claro
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isClaroActive) 1.5.dp else 1.dp,
                                        color = if (isClaroActive) AlertRed else AlertRed.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.selectCarrier(isClaro = true) }
                                    .testTag("carrier_tab_claro"),
                                color = if (isClaroActive) AlertRed.copy(alpha = 0.18f) else CyberPanel
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🔴 CLARO",
                                        color = if (isClaroActive) AlertRed else TextGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isClaroActive) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Tarjeta Principal del Servidor de la Operadora
                        val accentColor = if (isClaroActive) AlertRed else CyberCyan
                        val carrierTitle = if (isClaroActive) "SECCIÓN CLARO SELECCIONADO" else "SECCIÓN PERSONAL SELECCIONADO"
                        val defaultHost = if (isClaroActive) "simple.claro.com.ar" else "recargas.personal.com.ar"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.2.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.openServerModalForCarrier(isClaro = isClaroActive) }
                                .testTag("server_selector_card"),
                            color = CyberPanel
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(11.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = carrierTitle,
                                        color = accentColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (isClaroActive) "ELEGIR SERVIDOR CLARO ➔" else "ELEGIR SERVIDOR PERSONAL ➔",
                                        color = accentColor.copy(alpha = 0.85f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cloud,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = uiState.selectedServer?.name ?: (if (isClaroActive) "Claro Argentina 🔴" else "Personal 🩵"),
                                                color = TextWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${uiState.selectedServer?.host ?: defaultHost} ➔ ${uiState.selectedServer?.cf_host ?: "CloudFront"}",
                                                color = if (isClaroActive) AlertRed.copy(alpha = 0.9f) else ArgentineBlue,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Elegir Servidor",
                                        tint = accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tarjeta 2: Credenciales SSH (Usuario & Clave compactos)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Usuario
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            color = CyberPanel
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(CyberCyan.copy(alpha = 0.15f))
                                        .fillMaxHeight()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "danaelssh-",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                BasicTextField(
                                    value = uiState.rawUsername,
                                    onValueChange = { viewModel.setRawUsername(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp)
                                        .testTag("username_input"),
                                    textStyle = TextStyle(
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Ascii,
                                        imeAction = ImeAction.Next
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (uiState.rawUsername.isEmpty()) {
                                            Text(
                                                text = "nombre de usuario",
                                                color = TextGray.copy(alpha = 0.6f),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }

                        // Contraseña
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, ArgentineBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                            color = CyberPanel
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(CyberCyan.copy(alpha = 0.12f))
                                        .fillMaxHeight()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "CLAVE:",
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                BasicTextField(
                                    value = uiState.password,
                                    onValueChange = { viewModel.setPassword(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                        .testTag("password_input"),
                                    textStyle = TextStyle(
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    singleLine = true,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    decorationBox = { innerTextField ->
                                        if (uiState.password.isEmpty()) {
                                            Text(
                                                text = "Escribe tu clave...",
                                                color = TextGray.copy(alpha = 0.6f),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                if (uiState.password.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable { viewModel.setPassword("") }
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Borrar clave",
                                            tint = TextGray.copy(alpha = 0.8f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { isPasswordVisible = !isPasswordVisible }
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    color = Color.Transparent
                                ) {
                                    Text(text = if (isPasswordVisible) "👁" else "🔒", fontSize = 13.sp)
                                }

                                if (uiState.password.isEmpty()) {
                                    Surface(
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .border(0.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                            .clickable { viewModel.setPassword("123456") },
                                        color = CyberCyan.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "+ 123456",
                                            color = CyberCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tarjeta 3: Monitoreo en Segundo Plano (Reconexión Automática Activa)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (uiState.autoReconnectEnabled) NeonGreen.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.toggleAutoReconnect() }
                            .testTag("auto_reconnect_status_card"),
                        color = CyberPanel
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (uiState.autoReconnectEnabled) NeonGreen else TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "MONITOREO EN SEGUNDO PLANO",
                                        color = TextWhite,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (uiState.autoReconnectEnabled)
                                            "Reconexión automática activada ante caídas"
                                        else
                                            "Reconexión en segundo plano pausada",
                                        color = if (uiState.autoReconnectEnabled) NeonGreen.copy(alpha = 0.85f) else TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (uiState.autoReconnectEnabled) NeonGreen.copy(alpha = 0.15f) else TextGray.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (uiState.autoReconnectEnabled) NeonGreen.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (uiState.autoReconnectEnabled) "ACTIVO" else "PAUSADO",
                                    color = if (uiState.autoReconnectEnabled) NeonGreen else TextGray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Tarjetas Gráficas de Ondas y Telemetría en Vivo (Descarga, Subida y Ping abajo)
                TelemetryWaveCards(
                    telemetry = uiState.telemetry,
                    vpnState = uiState.vpnState,
                    isTourHighlighted = uiState.showGuidedTour && uiState.guidedTourStep == 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("telemetry_wave_cards")
                )

                // 6. Consola Terminal de Conexión en Tiempo Real
                ConnectionTerminalConsole(
                    logs = uiState.logs,
                    vpnState = uiState.vpnState,
                    isAutoScroll = uiState.isAutoScrollLogs,
                    onToggleAutoScroll = { viewModel.toggleAutoScrollLogs() },
                    onClearLogs = { viewModel.clearLogs() },
                    onOpenFullLogs = { viewModel.setShowLogsModal(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terminal_console_card")
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Diálogos & Modales
            if (uiState.showCreatorDialog) {
                CreatorDialog(
                    onDismiss = { viewModel.setShowCreatorDialog(false) },
                    onSpeak = { viewModel.speak(it) }
                )
            }

            if (uiState.showGuideModal) {
                GuideModal(
                    onDismiss = { viewModel.setShowGuideModal(false) },
                    onOpenCreator = {
                        viewModel.setShowGuideModal(false)
                        viewModel.setShowAccountCreatorModal(true)
                    }
                )
            }

            if (uiState.showAccountCreatorModal) {
                AccountCreatorModal(
                    onDismiss = { viewModel.setShowAccountCreatorModal(false) },
                    onAccountCreated = { u, p ->
                        viewModel.registerCloudFrontAccount(u, p)
                        viewModel.setShowAccountCreatorModal(false)
                    }
                )
            }

            if (uiState.showServerModal) {
                ServerSelectorModal(
                    categories = uiState.categories,
                    selectedCategory = uiState.selectedCategoryFilter,
                    searchQuery = uiState.serverSearchQuery,
                    onCategorySelected = { viewModel.setSelectedCategoryFilter(it) },
                    onSearchChanged = { viewModel.setServerSearchQuery(it) },
                    onServerSelected = { viewModel.setSelectedServer(it) },
                    onSyncCloud = { viewModel.syncWithCloud(silent = false) },
                    onDismiss = { viewModel.setShowServerModal(false) }
                )
            }

            if (uiState.showPayloadModal) {
                PayloadModal(
                    server = uiState.selectedServer,
                    onSavePayload = { viewModel.updateServerPayload(it) },
                    onDismiss = { viewModel.setShowPayloadModal(false) }
                )
            }

            if (uiState.showCloudFrontModal) {
                CloudFrontModal(
                    currentHost = uiState.selectedServer?.cf_host ?: "d3axktnpdrjmnq.cloudfront.net",
                    onSaveCloudFront = { viewModel.updateCloudFrontHost(it) },
                    onResetDefault = { viewModel.resetCloudFrontHost() },
                    onDismiss = { viewModel.setShowCloudFrontModal(false) }
                )
            }

            if (uiState.showLogsModal) {
                LogsModal(
                    logs = uiState.logs,
                    selectedServer = uiState.selectedServer,
                    rawUsername = uiState.rawUsername,
                    isAutoScroll = uiState.isAutoScrollLogs,
                    onToggleAutoScroll = { viewModel.toggleAutoScrollLogs() },
                    onClearLogs = { viewModel.clearLogs() },
                    onDismiss = { viewModel.setShowLogsModal(false) }
                )
            }

            if (uiState.showSettingsModal) {
                SettingsModal(
                    onDismiss = { viewModel.setShowSettingsModal(false) },
                    onOpenCreator = { viewModel.setShowCreatorDialog(true) },
                    onOpenGuide = { viewModel.setShowGuideModal(true) },
                    onSyncCloud = { viewModel.syncWithCloud(silent = false) },
                    onClearCache = { viewModel.clearAppCache() },
                    onOpenAccountCreator = { viewModel.setShowAccountCreatorModal(true) },
                    onOpenHotspot = { viewModel.setShowHotspotModal(true) },
                    onOpenApn = { viewModel.setShowApnModal(true) },
                    batterySaverEnabled = uiState.batterySaverEnabled,
                    onToggleBatterySaver = { viewModel.toggleBatterySaver() },
                    autoReconnectEnabled = uiState.autoReconnectEnabled,
                    onToggleAutoReconnect = { viewModel.toggleAutoReconnect() },
                    appVersion = "1.0.0 (Build 1)",
                    lastSyncTimestamp = uiState.lastSyncTimestamp,
                    onCheckAppUpdate = { viewModel.checkForAppUpdateManual() },
                    onOpenCloudUrl = { viewModel.setShowCloudUrlModal(true) }
                )
            }

            if (uiState.showCloudUrlModal) {
                CloudUrlModal(
                    currentUrl = uiState.customCloudUrl,
                    onSaveUrl = { url -> viewModel.updateCustomCloudUrl(url) },
                    onResetDefault = { viewModel.resetCustomCloudUrl() },
                    onDismiss = { viewModel.setShowCloudUrlModal(false) }
                )
            }

            val currentAppUpdate = uiState.appUpdateInfo
            if (uiState.showAppUpdateModal && currentAppUpdate != null) {
                AppUpdateModal(
                    updateInfo = currentAppUpdate,
                    onDismiss = { viewModel.dismissAppUpdateModal() }
                )
            }

            if (uiState.showHotspotModal) {
                HotspotModal(
                    isActive = uiState.isHotspotActive,
                    hotspotIp = uiState.hotspotIp,
                    hotspotPort = uiState.hotspotPort,
                    onToggleHotspot = { viewModel.toggleHotspot() },
                    onDismiss = { viewModel.setShowHotspotModal(false) }
                )
            }

            if (uiState.showApnModal) {
                ApnModal(
                    onDismiss = { viewModel.setShowApnModal(false) }
                )
            }

            // Custom Cyberpunk Toast Notification Pill
            CyberToastHost(
                toastState = toastState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
            )

            // Guía Interactiva Paso a Paso con Voz en Vivo ("mientras va hablando cuando va guiando")
            if (uiState.showGuidedTour) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    InteractiveTourCard(
                        currentStep = uiState.guidedTourStep,
                        onNext = { viewModel.nextTourStep() },
                        onPrev = { viewModel.prevTourStep() },
                        onDismiss = { viewModel.dismissGuidedTour() },
                        onRepeatVoice = { viewModel.speak(it) },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
