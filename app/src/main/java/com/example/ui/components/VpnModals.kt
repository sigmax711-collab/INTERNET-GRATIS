package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.AppUpdateInfo
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.ServerCategory
import com.example.model.ServerItem
import com.example.ui.theme.AlertRed
import com.example.ui.theme.ArgentineBlue
import com.example.ui.theme.ArgentineBlueLight
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.CyberPanelBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SunGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningYellow

// 1. MODAL CREADOR: CESARG
@Composable
fun CreatorDialog(
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        Toast.makeText(context, "CesarG", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = {
            onSpeak("Creador CesarG")
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                color = CyberSurface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyberPanel)
                            .border(2.dp, ArgentineBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLogo(size = 44.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "CREADOR: CESARG",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "DESARROLLADOR OFICIAL",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, ArgentineBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🇦🇷 Las Malvinas Son Argentinas",
                                color = ArgentineBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Personal & Claro CloudFront VPN",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Sincronizado vía Nube con Panel Admin",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onSpeak("Creador CesarG")
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArgentineBlue,
                            contentColor = CyberBackground
                        )
                    ) {
                        Text(
                            text = "CERRAR DIÁLOGO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// 2. MODAL INDICACIONES
@Composable
fun GuideModal(
    onDismiss: () -> Unit,
    onOpenCreator: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, WarningYellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF09141F))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = WarningYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "INDICACIONES DE USO",
                                color = WarningYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = AlertRed
                            )
                        }
                    }

                    HorizontalDivider(color = WarningYellow.copy(alpha = 0.2f))

                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            GuideStepCard(
                                stepNumber = "1",
                                stepColor = CyberCyan,
                                title = "Abrir la página generadora",
                                description = "Toca en CREAR CUENTA para ingresar a la plataforma de danael.fun/create/cloudfront/"
                            )
                        }

                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, ArgentineBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                color = Color.Black.copy(alpha = 0.55f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ArgentineBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("2", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = "Llenar los datos en la web",
                                            color = ArgentineBlueLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = "• Usuario: Escribe tu nombre (Ej: Cesar). La web generará automáticamente tu usuario como danaelssh-Cesar",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                        Text(
                                            text = "• Contraseña: Debe ser exactamente: 123456",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        )
                                        Text(
                                            text = "• Toca el botón Create / Crear",
                                            color = CyberCyan,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            GuideStepCard(
                                stepNumber = "3",
                                stepColor = Color(0xFFA855F7),
                                title = "Conectar en la App",
                                description = "Regresa a la app y escribe únicamente tu nombre (Cesar). La app ya tiene el prefijo danaelssh- y la clave 123456 colocada. ¡Solo presiona CONECTAR!"
                            )
                        }

                        item {
                            Button(
                                onClick = onOpenCreator,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(top = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ArgentineBlue,
                                    contentColor = CyberBackground
                                )
                            ) {
                                Text(
                                    text = "🚀 IR A CREAR MI CUENTA AHORA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    stepColor: Color,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, stepColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        color = Color.Black.copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(stepColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stepNumber, color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = title,
                    color = stepColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// 3. MODAL CREAR USUARIO CLOUDFRONT (WEBVIEW)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AccountCreatorModal(
    onDismiss: () -> Unit,
    onAccountCreated: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val targetUrl = "https://danael.fun/create/cloudfront/"
    var webViewCrashed by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF071420))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DANAEL CLOUDFRONT WEB",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Navegador", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }

                            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = AlertRed
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f))

                    // WebView content o Fallback si el proceso de renderizado falla
                    if (!webViewCrashed) {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    // Renderizado por software para máxima compatibilidad y evitar crash de GPU en el emulador
                                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.cacheMode = WebSettings.LOAD_DEFAULT

                                    class WebAppInterface {
                                        @JavascriptInterface
                                        fun saveCredentials(user: String, pass: String) {
                                            onAccountCreated?.invoke(user, pass)
                                        }
                                    }
                                    addJavascriptInterface(WebAppInterface(), "AndroidApp")

                                    webViewClient = object : WebViewClient() {
                                        // Evitar que la aplicación se cierre si el proceso de renderizado de Chromium muere
                                        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                            try {
                                                (view?.parent as? ViewGroup)?.removeView(view)
                                                view?.destroy()
                                            } catch (_: Throwable) {}
                                            webViewCrashed = true
                                            return true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            try {
                                                view?.evaluateJavascript(
                                                    """
                                                    (function() {
                                                        var forms = document.getElementsByTagName('form');
                                                        for(var i=0; i<forms.length; i++) {
                                                            forms[i].addEventListener('submit', function() {
                                                                var u = document.querySelector('input[name*="user"], input[type="text"]')?.value;
                                                                var p = document.querySelector('input[name*="pass"], input[type="password"]')?.value;
                                                                if(u && p && window.AndroidApp) {
                                                                    window.AndroidApp.saveCredentials(u, p);
                                                                }
                                                            });
                                                        }
                                                    })();
                                                    """.trimIndent(), null
                                                )
                                            } catch (_: Throwable) {}
                                        }
                                    }
                                    webChromeClient = WebChromeClient()
                                    loadUrl(targetUrl)
                                }
                            },
                            onRelease = { webView ->
                                try {
                                    webView.stopLoading()
                                    webView.loadUrl("about:blank")
                                    webView.clearHistory()
                                    webView.removeAllViews()
                                    webView.destroy()
                                } catch (_: Throwable) {}
                            }
                        )
                    } else {
                        // Vista alternativa si WebView Chromium falla
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(52.dp)
                                )
                                Text(
                                    text = "Plataforma Danael CloudFront",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "Genera tu cuenta gratis abriendo el enlace en tu navegador web:",
                                    color = TextGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ABRIR EN NAVEGADOR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Barra inferior de confirmación directa
                    var manualInput by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF071420)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "💡 ¿Ya creaste tu usuario en la web? Escríbelo aquí para autorizarlo:",
                                color = TextGray,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                    color = CyberPanel
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "danaelssh-",
                                            color = CyberCyan,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        BasicTextField(
                                            value = manualInput,
                                            onValueChange = { manualInput = it },
                                            singleLine = true,
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 6.dp),
                                            decorationBox = { innerTextField: @Composable () -> Unit ->
                                                if (manualInput.isEmpty()) {
                                                    Text(
                                                        text = "Cesar",
                                                        color = TextGray.copy(alpha = 0.5f),
                                                        fontSize = 12.sp,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (manualInput.isNotBlank()) {
                                            onAccountCreated?.invoke(manualInput.trim(), "123456")
                                        }
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ArgentineBlue),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text("ACTIVAR", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. MODAL LISTA DE SERVIDORES
@Composable
fun ServerSelectorModal(
    categories: List<ServerCategory>,
    selectedCategory: ServerCategory?,
    searchQuery: String,
    onCategorySelected: (ServerCategory?) -> Unit,
    onSearchChanged: (String) -> Unit,
    onServerSelected: (ServerItem) -> Unit,
    onSyncCloud: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF061117))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = selectedCategory?.name ?: "CATEGORÍAS",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (onSyncCloud != null) {
                                    Surface(
                                        modifier = Modifier
                                            .height(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(1.dp, NeonGreen.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                            .clickable { onSyncCloud() }
                                            .testTag("modal_sync_cloud_button"),
                                        color = NeonGreen.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Actualizar servidores desde la nube",
                                                tint = NeonGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "ACTUALIZAR",
                                                color = NeonGreen,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = AlertRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ArgentineBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchChanged,
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Buscar servidor...",
                                                color = TextGray.copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchChanged("") },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Borrar",
                                            tint = TextGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f))

                    // Content List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (selectedCategory != null) {
                            // Category details / server items
                            item {
                                OutlinedButton(
                                    onClick = { onCategorySelected(null) },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = AlertRed.copy(alpha = 0.15f),
                                        contentColor = AlertRed
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertRed.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            "VOLVER A CATEGORÍAS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            val filteredServers = selectedCategory.items.filter { s ->
                                searchQuery.isBlank() ||
                                        s.name.contains(searchQuery, ignoreCase = true) ||
                                        s.cf_host.contains(searchQuery, ignoreCase = true)
                            }

                            items(filteredServers) { srv ->
                                val isClaroSrv = srv.name.contains("Claro", ignoreCase = true) || srv.host.contains("claro", ignoreCase = true)
                                val srvAccent = if (isClaroSrv) AlertRed else CyberCyan

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, srvAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onServerSelected(srv) },
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cloud,
                                            contentDescription = null,
                                            tint = srvAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = srv.name,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${srv.host} ➔ ${srv.cf_host}",
                                                color = srvAccent,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "MODO: ${srv.mode} | PUERTO: ${srv.port} | UDP: ${srv.udpPort} ${if (srv.description.isNotBlank()) "• ${srv.description}" else ""}",
                                                color = TextGray,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Categories list (Personal & Claro separated)
                            val filteredCategories = categories.filter { cat ->
                                searchQuery.isBlank() ||
                                        cat.name.contains(searchQuery, ignoreCase = true) ||
                                        cat.items.any { it.name.contains(searchQuery, ignoreCase = true) }
                            }

                            items(filteredCategories) { cat ->
                                val isClaroCat = cat.name.contains("Claro", ignoreCase = true)
                                val catColor = if (isClaroCat) AlertRed else ArgentineBlue

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.2.dp, catColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .clickable { onCategorySelected(cat) },
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cat.name,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "${cat.items.size} Servidores Disponibles • ${if (isClaroCat) "simple.claro.com.ar" else "recargas.personal.com.ar"}",
                                                color = if (isClaroCat) AlertRed.copy(alpha = 0.85f) else ArgentineBlueLight,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. MODAL LOGS (PANTALLA DE REGISTRO EXACTA)
@Composable
fun LogsModal(
    logs: List<LogEntry>,
    selectedServer: ServerItem?,
    rawUsername: String,
    isAutoScroll: Boolean,
    onToggleAutoScroll: () -> Unit,
    onClearLogs: () -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size, isAutoScroll) {
        if (isAutoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                color = Color(0xFF030712)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Bar: Trash icon on the left, "REGISTRO" centered, Close icon on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0F1D))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClearLogs,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Limpiar Registro",
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "REGISTRO",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Logs List: Clean terminal style identical to original app
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF030712))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(logs) { log ->
                            val textColor = when (log.type) {
                                LogType.SUCCESS -> Color(0xFF38BDF8) // Azul claro como 101 https://t.me/danaelssh
                                LogType.ERROR -> Color(0xFFFF5252)   // Rojo para errores
                                LogType.HIGHLIGHT -> Color(0xFFF1F5F9) // Blanco / Resaltado
                                LogType.NORMAL -> Color(0xFFCBD5E1)  // Blanco grisáceo claro
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${log.timestamp} -",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = when (log.type) {
                                        LogType.SUCCESS, LogType.HIGHLIGHT -> FontWeight.Bold
                                        else -> FontWeight.Normal
                                    },
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Bottom Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0F1D))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CLOUDFRONT SSH • ARGENTINA 🇦🇷",
                            color = ArgentineBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onToggleAutoScroll() },
                            color = if (isAutoScroll) CyberCyan.copy(alpha = 0.2f) else Color(0xFF1E293B)
                        ) {
                            Text(
                                text = "Auto-Scroll: ${if (isAutoScroll) "ON" else "OFF"}",
                                color = if (isAutoScroll) CyberCyan else TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 6. MODAL CONFIGURACIÓN
@Composable
fun SettingsModal(
    onDismiss: () -> Unit,
    onOpenCreator: () -> Unit,
    onOpenGuide: () -> Unit,
    onSyncCloud: () -> Unit,
    onClearCache: () -> Unit,
    onOpenAccountCreator: () -> Unit = {},
    onOpenHotspot: () -> Unit = {},
    onOpenApn: () -> Unit = {},
    batterySaverEnabled: Boolean = true,
    onToggleBatterySaver: () -> Unit = {},
    autoReconnectEnabled: Boolean = true,
    onToggleAutoReconnect: () -> Unit = {},
    appVersion: String = "1.0.0 (Build 1)",
    lastSyncTimestamp: String = "No registrada",
    onCheckAppUpdate: () -> Unit = {},
    onOpenCloudUrl: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B131A))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "CONFIGURACIÓN",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = AlertRed
                            )
                        }
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Creator button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable {
                                    Toast.makeText(context, "CesarG", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                    onOpenCreator()
                                },
                            color = CyberCyan.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = SunGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Creador: CesarG",
                                        color = CyberCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Créditos del desarrollador",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Crear Cuenta button (Solicitado explícitamente en Configuración)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenAccountCreator()
                                },
                            color = NeonGreen.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Crear Cuenta SSH (Gratis)",
                                        color = NeonGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Generar usuario y clave en danael.fun o Telegram",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Guide button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, WarningYellow.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenGuide()
                                },
                            color = WarningYellow.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = WarningYellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Indicaciones y Guía",
                                        color = WarningYellow,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Cómo crear usuario y contraseña",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Hotspot VPN button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenHotspot()
                                },
                            color = NeonGreen.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Compartir VPN (Hotspot)",
                                        color = NeonGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Proxy local para PC, Smart TV u otros celulares",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // APN Settings button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, SunGold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onOpenApn()
                                },
                            color = SunGold.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = SunGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Configuración APN Móvil",
                                        color = SunGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Ajustes de red óptimos (Personal, Claro, Movistar)",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Battery Saver Toggle
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            color = CyberCyan.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Optimización de Batería",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (batterySaverEnabled) "Sondeo reducido en segundo plano (Activo)" else "Sondeo frecuente de telemetría (1 seg)",
                                            color = if (batterySaverEnabled) NeonGreen else TextGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Switch(
                                    checked = batterySaverEnabled,
                                    onCheckedChange = { onToggleBatterySaver() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CyberCyan,
                                        checkedTrackColor = CyberCyan.copy(alpha = 0.4f),
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = Color(0xFF142436)
                                    )
                                )
                            }
                        }

                        // Monitoreo en segundo plano y Reconexión Automática
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            color = NeonGreen.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = NeonGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Reconexión Automática",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (autoReconnectEnabled) "Monitoreo en segundo plano activo: reconecta ante caídas sin tocar nada" else "Desactivado (requiere reconectar manualmente)",
                                            color = if (autoReconnectEnabled) NeonGreen else TextGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Switch(
                                    checked = autoReconnectEnabled,
                                    onCheckedChange = { onToggleAutoReconnect() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonGreen,
                                        checkedTrackColor = NeonGreen.copy(alpha = 0.4f),
                                        uncheckedThumbColor = TextGray,
                                        uncheckedTrackColor = Color(0xFF142436)
                                    )
                                )
                            }
                        }


                        // Panel Avanzado de Nube y Actualización de Servidores/APK
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            color = NeonGreen.copy(alpha = 0.06f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(NeonGreen)
                                        )
                                        Text(
                                            text = "Nube CesarG Activa",
                                            color = NeonGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Text(
                                        text = "v$appVersion",
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = "Última sincronización: $lastSyncTimestamp",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onCheckAppUpdate()
                                        },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(36.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "ACTUALIZAR NUBE/APK",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onDismiss()
                                            onOpenCloudUrl()
                                        },
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .height(36.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                                    ) {
                                        Text(
                                            text = "URL NUBE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = ArgentineBlue.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Clear cache button
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AlertRed.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onDismiss()
                                    onClearCache()
                                },
                            color = AlertRed.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Limpiar Caché de App",
                                        color = AlertRed,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Restaura configuraciones de fábrica",
                                        color = AlertRed.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. MODAL EDITOR DE PAYLOAD HTTP / WEBSOCKET (DTUNNEL STYLE)
@Composable
fun PayloadModal(
    server: ServerItem?,
    onSavePayload: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val personalPayloadPreset = "PUT / HTTP/1.1[crlf]Host: [host][crlf][crlf][lf][lf][instant_split][lf][lf]X / HTTP/1.2[crlf]Host: [host][crlf][lf][crlf]GET /.rar HTTP/1.1[crlf]Host: [cf_host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]"
    val claroPayloadPreset = "GET / HTTP/1.1[crlf]Host: [cf_host][crlf]X-Online-Host: [host][crlf]X-Forward-Host: [host][crlf]Upgrade: websocket[crlf]Connection: Upgrade[crlf][crlf]"
    val fallbackWorkingPayload = personalPayloadPreset
    val defaultPayload = server?.payload?.ifBlank { fallbackWorkingPayload } ?: fallbackWorkingPayload
    var payloadText by remember(server?.id) { mutableStateOf(defaultPayload) }
    var showExplanation by remember { mutableStateOf(true) }
    var showPreview by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val effectiveHost = server?.host?.ifBlank { "recargas.personal.com.ar" } ?: "recargas.personal.com.ar"
    val effectiveCf = server?.cf_host?.ifBlank { "d3axktnpdrjmnq.cloudfront.net" } ?: "d3axktnpdrjmnq.cloudfront.net"

    // Vista previa formateada reemplazando las etiquetas dinámicas
    val formattedPreview = remember(payloadText, effectiveHost, effectiveCf) {
        payloadText
            .replace("[host]", effectiveHost)
            .replace("[cf_host]", effectiveCf)
            .replace("[port]", (server?.port ?: 80).toString())
            .replace("[crlf]", "\r\n")
            .replace("[lf]", "\n")
            .replace("[instant_split]", "\n--[CORTE INSTANTÁNEO SPLIT]--\n")
            .replace("[split]", "\n--[CORTE SPLIT CON RETARDO]--\n")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.98f)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "MODIFICAR PAYLOAD HTTP",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${server?.name ?: "Servidor"} • Host: $effectiveHost",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Tarjeta Explicativa destacada de por qué se pone 2 veces recargas.personal.com.ar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, SunGold.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                        color = Color(0xFF161508)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showExplanation = !showExplanation },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "💡", fontSize = 14.sp)
                                    Text(
                                        text = "¿Por qué se pone 2 veces 'recargas.personal.com.ar'?",
                                        color = SunGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = if (showExplanation) "▲" else "▼",
                                    color = SunGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (showExplanation) {
                                Text(
                                    text = "1. Personal Argentina bloquea conexiones directas sin saldo si no van a su portal gratuito (saldo cero).\n" +
                                            "2. La técnica Split Payload divide la conexión en dos paquetes HTTP:\n" +
                                            "   • Petición 1: 'Host: [host]' (valida el host sin saldo para que la operadora abra la puerta).\n" +
                                            "   • Corte: '[instant_split]' (corta el paquete en la red de la operadora).\n" +
                                            "   • Petición 2: 'Host: [host]' (repite el host para mantener la autorización) + 'Upgrade: websocket' hacia CloudFront.\n" +
                                            "3. Como la plantilla tiene [host] en ambos lugares, al inyectar se reemplaza dos veces por 'recargas.personal.com.ar'. ¡Es 100% necesario para que conecte!",
                                    color = Color(0xFFFDE68A),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // Presets rápidos (Personal y Claro)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable {
                                    payloadText = personalPayloadPreset
                                    Toast.makeText(context, "Payload Personal Argentina cargado", Toast.LENGTH_SHORT).show()
                                },
                            color = CyberPanel
                        ) {
                            Text(
                                text = "🩵 PERSONAL ARGENTINA",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AlertRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable {
                                    payloadText = claroPayloadPreset
                                    Toast.makeText(context, "Payload Claro Argentina cargado", Toast.LENGTH_SHORT).show()
                                },
                            color = CyberPanel
                        ) {
                            Text(
                                text = "🔴 CLARO ARGENTINA",
                                color = AlertRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Etiquetas rápidas de inserción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("[crlf]", "[host]", "[instant_split]", "[lf]", "[cf_host]").forEach { tag ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, ArgentineBlue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .clickable { payloadText += tag },
                                color = CyberPanel
                            ) {
                                Text(
                                    text = tag,
                                    color = ArgentineBlueLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Selector de Modo: Editar Código vs Vista Previa Inyectada
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (!showPreview) CyberCyan else TextGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { showPreview = false },
                            color = if (!showPreview) CyberCyan.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = "EDITAR CÓDIGO",
                                color = if (!showPreview) CyberCyan else TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, if (showPreview) NeonGreen else TextGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { showPreview = true },
                            color = if (showPreview) NeonGreen.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = "VISTA PREVIA INYECTADA",
                                color = if (showPreview) NeonGreen else TextGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }

                    // Cuadro de Entrada o Vista Previa
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (showPreview) NeonGreen.copy(alpha = 0.4f) else CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                        color = Color(0xFF040A12)
                    ) {
                        if (!showPreview) {
                            BasicTextField(
                                value = payloadText,
                                onValueChange = { payloadText = it },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                ),
                                decorationBox = { innerTextField ->
                                    if (payloadText.isEmpty()) {
                                        Text(
                                            text = "Escribe o pega aquí tu payload HTTP...",
                                            color = TextGray.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        } else {
                            Text(
                                text = formattedPreview,
                                color = NeonGreen.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }

                    // Botones de Acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val def = server?.payload?.ifBlank { fallbackWorkingPayload } ?: fallbackWorkingPayload
                                payloadText = def
                                Toast.makeText(context, "Payload predeterminado restablecido", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                        ) {
                            Text(
                                text = "RESTAURAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                onSavePayload(payloadText)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen.copy(alpha = 0.25f),
                                contentColor = NeonGreen
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "GUARDAR Y APLICAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 8. MODAL CONFIGURACIÓN CLOUDFRONT CDN
// ==========================================
@Composable
fun CloudFrontModal(
    currentHost: String,
    onSaveCloudFront: (String) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var hostInput by remember { mutableStateOf(currentHost) }

    fun isDanael(host: String): Boolean {
        return host.contains("d2htdw8fj0jdwe", ignoreCase = true) ||
                host.contains("d3cdy6viklzpq1", ignoreCase = true) ||
                host.contains("danael", ignoreCase = true)
    }

    val isCurrentDanael = isDanael(hostInput)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                color = CyberSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "SERVIDOR CLOUDFRONT CDN",
                                color = CyberCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Cambia el dominio de CloudFront si la página actualizó el servidor. Se aplicará a la conexión SSH sobre WebSocket.",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Detection Indicator Banner
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isCurrentDanael) CyberCyan.copy(alpha = 0.5f) else WarningYellow.copy(alpha = 0.7f),
                                RoundedCornerShape(8.dp)
                            ),
                        color = if (isCurrentDanael) CyberCyan.copy(alpha = 0.08f) else WarningYellow.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "🛡️ Servidor CloudFront CDN • Red DanaelSSH",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "✔ Tu usuario mantendrá el prefijo 'danaelssh-' automáticamente (clave: 123456) para conectar sin problemas.",
                                color = TextWhite,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Field with Paste & Clear action
                    Text(
                        text = "HOST / DOMINIO CLOUDFRONT:",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                        color = CyberPanel
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = hostInput,
                                onValueChange = { hostInput = it.trim() },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                singleLine = true,
                                decorationBox = { inner ->
                                    if (hostInput.isEmpty()) {
                                        Text(
                                            text = "ej. d2htdw8fj0jdwe.cloudfront.net",
                                            color = TextGray.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    inner()
                                }
                            )

                            if (hostInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { hostInput = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar",
                                        tint = TextGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Paste button
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        clipboardManager.getText()?.text?.let { clipText ->
                                            val cleaned = clipText.trim().removePrefix("https://").removePrefix("http://").trimEnd('/')
                                            if (cleaned.isNotBlank()) {
                                                hostInput = cleaned
                                                Toast.makeText(context, "Pegado del portapapeles", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                color = CyberCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "PEGAR",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Presets rápidos (Servidores Oficiales)
                    Text(
                        text = "PREAJUSTES OFICIALES RÁPIDOS:",
                        color = TextGray,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable {
                                    hostInput = "d2htdw8fj0jdwe.cloudfront.net"
                                    Toast.makeText(context, "Seleccionado Servidor 1", Toast.LENGTH_SHORT).show()
                                },
                            color = CyberPanel
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Servidor 1 (Oficial)",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "d2htdw8fj0jdwe...",
                                    color = TextGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable {
                                    hostInput = "d3cdy6viklzpq1.cloudfront.net"
                                    Toast.makeText(context, "Seleccionado Servidor 2", Toast.LENGTH_SHORT).show()
                                },
                            color = CyberPanel
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Servidor 2 (Alterno)",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "d3cdy6viklzpq1...",
                                    color = TextGray,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onResetDefault()
                                Toast.makeText(context, "Restablecido a predeterminado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                        ) {
                            Text(
                                text = "RESTAURAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                if (hostInput.isNotBlank()) {
                                    onSaveCloudFront(hostInput)
                                } else {
                                    Toast.makeText(context, "Ingresa un host válido", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen.copy(alpha = 0.2f),
                                contentColor = NeonGreen
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
                        ) {
                            Text(
                                text = "GUARDAR Y APLICAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// 12. MODAL DE ACTUALIZACIÓN DE LA APLICACIÓN (OTA / NUBE)
@Composable
fun AppUpdateModal(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            if (!updateInfo.forceUpdate) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.forceUpdate,
            dismissOnClickOutside = !updateInfo.forceUpdate,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, NeonGreen, RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Badge
                    Surface(
                        shape = CircleShape,
                        color = NeonGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonGreen),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Text(
                        text = "🚀 NUEVA ACTUALIZACIÓN",
                        color = NeonGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Version comparison row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF071118))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ACTUAL: v1.0.0",
                            color = TextGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "NUEVA: ${updateInfo.latestVersion}",
                            color = SunGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Cloud Auto-Sync Notice Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CyberCyan.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "✔ SERVIDORES EN LA NUBE AL DÍA",
                                color = CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tus servidores y CloudFront de Claro y Personal ya se sincronizaron con éxito desde la nube en este teléfono.",
                                color = TextWhite.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Changelog Section
                    if (updateInfo.changelog.isNotBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF09141E))
                                .border(1.dp, Color(0xFF193246), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Novedades de la actualización:",
                                color = SunGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = updateInfo.changelog,
                                color = TextWhite,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Action: Download APK Button
                    Button(
                        onClick = {
                            if (updateInfo.downloadUrl.isNotBlank()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error abriendo enlace: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Enlace de descarga no configurado", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "DESCARGAR E INSTALAR APK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Copy Link Button
                    OutlinedButton(
                        onClick = {
                            if (updateInfo.downloadUrl.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Link Actualización VPN", updateInfo.downloadUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Enlace copiado al portapapeles", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                    ) {
                        Text(
                            text = "COPIAR ENLACE DE DESCARGA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Dismiss Button (if not forced)
                    if (!updateInfo.forceUpdate) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                        ) {
                            Text(
                                text = "CONTINUAR CON LA VERSIÓN ACTUAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// 13. MODAL CONFIGURADOR DE URL DE NUBE (JSONBIN / RAW GITHUB)
@Composable
fun CloudUrlModal(
    currentUrl: String,
    onSaveUrl: (String) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    var urlInput by remember(currentUrl) { mutableStateOf(currentUrl) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CyberCyan, RoundedCornerShape(14.dp)),
                color = CyberSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "URL DE LA NUBE",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = AlertRed)
                        }
                    }

                    Text(
                        text = "Enlace JSON centralizado desde donde todos los usuarios reciben actualizaciones automáticas de servidores y versiones:",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    BasicTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A1520))
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onResetDefault,
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WarningYellow.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningYellow)
                        ) {
                            Text(
                                text = "RESTAURAR",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = {
                                if (urlInput.isNotBlank()) {
                                    onSaveUrl(urlInput.trim())
                                } else {
                                    Toast.makeText(context, "Ingresa una URL válida", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(40.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black)
                        ) {
                            Text(
                                text = "GUARDAR Y SINCRONIZAR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// 10. MODAL COMPARTIR VPN (HOTSPOT / ZONA WI-FI)
@Composable
fun HotspotModal(
    isActive: Boolean,
    hotspotIp: String,
    hotspotPort: Int,
    onToggleHotspot: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, if (isActive) NeonGreen.copy(alpha = 0.6f) else CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B141C))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = if (isActive) NeonGreen else CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "COMPARTIR VPN • HOTSPOT",
                                color = if (isActive) NeonGreen else CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = AlertRed)
                        }
                    }

                    HorizontalDivider(color = CyberCyan.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Status Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (isActive) NeonGreen.copy(alpha = 0.5f) else TextGray.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            color = if (isActive) NeonGreen.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isActive) "🟢 PROXY ACTIVO" else "⚪ PROXY INACTIVO",
                                        color = if (isActive) NeonGreen else TextGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = if (isActive) "Enrutando tráfico vía túnel SSH" else "Inicia el proxy para compartir",
                                        color = TextGray,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Button(
                                    onClick = onToggleHotspot,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) AlertRed else NeonGreen,
                                        contentColor = if (isActive) TextWhite else Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isActive) "DETENER" else "INICIAR",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Proxy IP & Port Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            color = CyberPanel
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "DATOS DE CONFIGURACIÓN PROXY:",
                                    color = CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "IP Servidor Proxy:", color = TextGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        Text(text = hotspotIp, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Puerto Proxy:", color = TextGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        Text(text = "$hotspotPort", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = ClipData.newPlainText("Proxy", "$hotspotIp:$hotspotPort")
                                        clipboard?.setPrimaryClip(clip)
                                        copied = true
                                        Toast.makeText(context, "Copiado: $hotspotIp:$hotspotPort", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (copied) NeonGreen else CyberCyan.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (copied) NeonGreen else CyberCyan)
                                ) {
                                    Icon(imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (copied) "¡COPIADO!" else "COPIAR DATOS ($hotspotIp:$hotspotPort)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Open Android Tethering Settings Button
                        OutlinedButton(
                            onClick = { openHotspotSettings(context) },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SunGold.copy(alpha = 0.8f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SunGold)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ABRIR ZONA WI-FI / HOTSPOT EN ANDROID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Quick Guide Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "📌 Pasos para conectar tus dispositivos:",
                                    color = SunGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "1. Activa Zona Wi-Fi / Hotspot en este teléfono.\n2. Conecta tu PC, TV u otro móvil a la red Wi-Fi de este teléfono.\n3. En los ajustes Wi-Fi del otro dispositivo -> Opciones avanzadas -> Proxy Manual.\n4. Introduce Host: $hotspotIp y Puerto: $hotspotPort.\n5. ¡Listo! Ya navega usando la VPN sin consumir datos.",
                                    color = TextGray,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 11. MODAL CONFIGURACIÓN APN RED MÓVIL
@Composable
fun ApnModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCarrier by remember { mutableStateOf(0) } // 0: Personal, 1: Claro, 2: Movistar

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SunGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                color = CyberSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141208))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = SunGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "CONFIGURACIÓN APN MÓVIL",
                                color = SunGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = AlertRed)
                        }
                    }

                    HorizontalDivider(color = SunGold.copy(alpha = 0.2f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Direct Button to Android APN Settings (Compatible with all devices)
                        Button(
                            onClick = { openApnSettings(context) },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SunGold, contentColor = Color.Black)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ABRIR AJUSTES DE APN EN ANDROID",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Carrier Tabs: Personal, Claro, Movistar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("🩵 Personal", "🔴 Claro", "🔵 Movistar").forEachIndexed { idx, title ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(
                                            1.dp,
                                            if (selectedCarrier == idx) CyberCyan else TextGray.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedCarrier = idx },
                                    color = if (selectedCarrier == idx) CyberCyan.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.4f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = title,
                                            color = if (selectedCarrier == idx) TextWhite else TextGray,
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedCarrier == idx) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        // Carrier APN Details
                        val apnData = when (selectedCarrier) {
                            0 -> ApnInfo("Personal Datos", "datos.personal.com", "datos", "datos", "722", "34", "default,supl", "IPv4")
                            1 -> ApnInfo("Claro AR", "igprs.claro.com.ar", "ctigprs", "ctigprs999", "722", "310", "default,supl", "IPv4")
                            else -> ApnInfo("Movistar Datos", "wap.gprs.unifon.com.ar", "wap", "wap", "722", "07", "default,supl", "IPv4")
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            color = CyberPanel
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ApnDetailRow(context, label = "Nombre:", value = apnData.name)
                                ApnDetailRow(context, label = "APN:", value = apnData.apn, isHighlighted = true)
                                ApnDetailRow(context, label = "Usuario:", value = apnData.user)
                                ApnDetailRow(context, label = "Contraseña:", value = apnData.pass)
                                ApnDetailRow(context, label = "MCC / MNC:", value = "${apnData.mcc} / ${apnData.mnc}")
                                ApnDetailRow(context, label = "Tipo de APN:", value = apnData.type)
                                ApnDetailRow(context, label = "Protocolo APN:", value = apnData.protocol, isHighlighted = true)
                            }
                        }

                        // Tip Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningYellow.copy(alpha = 0.10f))
                                .border(1.dp, WarningYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = WarningYellow, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "⭐ Tip Clave para Saldo Cero: Asegúrate de que el 'Protocolo APN' y 'Protocolo en Roaming' estén configurados en IPv4 (no IPv6 ni IPv4/IPv6). Esto previene bloqueos de la operadora y acelera la conexión por el puerto 80.",
                                    color = TextWhite,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ApnInfo(
    val name: String,
    val apn: String,
    val user: String,
    val pass: String,
    val mcc: String,
    val mnc: String,
    val type: String,
    val protocol: String
)

@Composable
private fun ApnDetailRow(
    context: Context,
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                color = if (isHighlighted) NeonGreen else TextWhite,
                fontSize = 11.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copiar $value",
                tint = CyberCyan.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText(label, value)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Copiado: $value", Toast.LENGTH_SHORT).show()
                    }
            )
        }
    }
}

/**
 * Abre los ajustes de APN / Redes Móviles de forma universal y robusta,
 * garantizando compatibilidad con Samsung, Xiaomi/MIUI/HyperOS, Motorola,
 * Huawei, Oppo, Vivo, Transsion, Google Pixel y todas las versiones de Android (8 a 15).
 */
fun openApnSettings(context: Context) {
    val candidateIntents = listOf(
        // 1. Intent estándar del sistema para APN
        Intent(android.provider.Settings.ACTION_APN_SETTINGS),
        Intent("android.settings.APN_SETTINGS"),

        // 2. Samsung Galaxy (One UI en Android 8 a 15)
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$ApnSettingsActivity")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.network.telephony.ApnSettings")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$ApnEditorActivity")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$NetworkOperatorSettingsActivity")),

        // 3. Xiaomi / Redmi / POCO (MIUI / HyperOS)
        Intent().setComponent(ComponentName("com.android.phone", "com.android.phone.ApnSettings")),
        Intent("android.intent.action.MAIN").setClassName("com.android.phone", "com.android.phone.ApnSettings"),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$ApnSettingsActivity")),

        // 4. Motorola / Lenovo (MyUX / Hello UI)
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$ApnSettingsActivity")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.ApnSettings")),

        // 5. Huawei / Honor (EMUI / MagicOS)
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.ApnSettings")),

        // 6. Configuración de Redes Móviles / SIM (En Android 10, 11, 12, 13, 14, 15 donde la opción APN está dentro de la SIM)
        Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS),
        Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
        Intent("android.settings.NETWORK_OPERATOR_SETTINGS"),
        Intent("android.settings.DATA_ROAMING_SETTINGS"),
        Intent("android.settings.MMS_MESSAGE_SETTING"),

        // 7. Conexiones inalámbricas / Redes e Internet
        Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
        Intent("android.settings.WIRELESS_SETTINGS"),

        // 8. Ajustes generales del dispositivo
        Intent(android.provider.Settings.ACTION_SETTINGS)
    )

    for (intent in candidateIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "Entra en tu SIM y busca 'Nombres de punto de acceso (APN)'", Toast.LENGTH_LONG).show()
            return
        } catch (_: Throwable) {
            // Continúa buscando la actividad correcta para este fabricante y versión de Android
        }
    }

    Toast.makeText(context, "Abre Ajustes -> Conexiones / Red Móvil -> APN", Toast.LENGTH_LONG).show()
}

/**
 * Abre los ajustes de Zona Wi-Fi / Hotspot en cualquier fabricante Android.
 */
fun openHotspotSettings(context: Context) {
    val candidateIntents = listOf(
        Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS),
        Intent("android.settings.TETHER_SETTINGS"),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.TetherSettings")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.wifi.tether.WifiTetherSettings")),
        Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$WifiApSettingsActivity")),
        Intent(android.provider.Settings.ACTION_SETTINGS)
    )

    for (intent in candidateIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Throwable) {
        }
    }
    Toast.makeText(context, "Abre Ajustes -> Conexiones -> Zona Wi-Fi / Hotspot", Toast.LENGTH_LONG).show()
}


