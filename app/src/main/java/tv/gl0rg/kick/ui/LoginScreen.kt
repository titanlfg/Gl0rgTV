package tv.gl0rg.kick.ui

import android.net.Uri
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import tv.gl0rg.kick.kick.KickSessionProvider
import tv.gl0rg.kick.kick.WebViewKickSessionProvider
import kotlin.math.max
import kotlin.math.min

@Composable
fun LoginScreen(
    onLoginObserved: () -> Unit,
    onBack: () -> Unit,
    sessionProvider: KickSessionProvider = WebViewKickSessionProvider(),
    modifier: Modifier = Modifier
) {
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Back", onClick = onBack),
            TvNavAction("Login", selected = true) {}
        )
    ) {
        TvWebLogin(
            onLoginObserved = onLoginObserved,
            sessionProvider = sessionProvider,
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .border(2.dp, KickGreen, RoundedCornerShape(8.dp))
        )
    }
}

private fun Uri.isKickHost(): Boolean {
    val host = host?.lowercase() ?: return false
    return scheme == "https" && (host == "kick.com" || host.endsWith(".kick.com"))
}

@Composable
private fun TvWebLogin(
    onLoginObserved: () -> Unit,
    sessionProvider: KickSessionProvider,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val cursor = remember { mutableStateOf(Offset(320f, 240f)) }
    val size = remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun handleKey(keyCode: Int): Boolean {
        val webView = webViewRef.value ?: return false
        return moveOrClickWebCursor(
            webView = webView,
            keyCode = keyCode,
            cursor = cursor.value,
            width = size.value.width,
            height = size.value.height,
            onCursorChanged = { cursor.value = it }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> handleKey(KeyEvent.KEYCODE_DPAD_LEFT)
                    Key.DirectionRight -> handleKey(KeyEvent.KEYCODE_DPAD_RIGHT)
                    Key.DirectionUp -> handleKey(KeyEvent.KEYCODE_DPAD_UP)
                    Key.DirectionDown -> handleKey(KeyEvent.KEYCODE_DPAD_DOWN)
                    Key.Enter, Key.DirectionCenter -> handleKey(KeyEvent.KEYCODE_DPAD_CENTER)
                    else -> false
                }
            }
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { newSize ->
                    size.value = newSize
                    if (cursor.value == Offset(320f, 240f) && newSize.width > 0 && newSize.height > 0) {
                        cursor.value = Offset(newSize.width / 2f, newSize.height / 2f)
                    }
                },
            factory = { context ->
                WebView(context).apply {
                    isFocusable = true
                    isFocusableInTouchMode = true
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                            return !request.url.isKickHost()
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            if (Uri.parse(url).isKickHost() && sessionProvider.hasSession()) {
                                onLoginObserved()
                            }
                        }
                    }
                    setOnKeyListener { _, keyCode, event ->
                        event.action == KeyEvent.ACTION_DOWN && handleKey(keyCode)
                    }
                    requestFocus()
                    webViewRef.value = this
                    loadUrl("https://kick.com")
                }
            },
            onRelease = { webView ->
                if (webViewRef.value == webView) {
                    webViewRef.value = null
                }
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.destroy()
            }
        )
        Canvas(Modifier.fillMaxSize()) {
            val point = cursor.value
            drawCircle(KickGreen, radius = 12f, center = point)
            drawCircle(Gl0rgBackground, radius = 5f, center = point)
            drawLine(
                color = KickGreen,
                start = Offset(point.x - 24f, point.y),
                end = Offset(point.x + 24f, point.y),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = KickGreen,
                start = Offset(point.x, point.y - 24f),
                end = Offset(point.x, point.y + 24f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun moveOrClickWebCursor(
    webView: WebView,
    keyCode: Int,
    cursor: Offset,
    width: Int,
    height: Int,
    onCursorChanged: (Offset) -> Unit
): Boolean {
    if (width <= 0 || height <= 0) return false
    val move = 42f
    return when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> {
            onCursorChanged(cursor.copy(x = max(16f, cursor.x - move)))
            true
        }
        KeyEvent.KEYCODE_DPAD_RIGHT -> {
            onCursorChanged(cursor.copy(x = min(width - 16f, cursor.x + move)))
            true
        }
        KeyEvent.KEYCODE_DPAD_UP -> {
            val newY = cursor.y - move
            if (newY < 20f) webView.scrollBy(0, -move.toInt())
            onCursorChanged(cursor.copy(y = max(16f, newY)))
            true
        }
        KeyEvent.KEYCODE_DPAD_DOWN -> {
            val newY = cursor.y + move
            if (newY > height - 20f) webView.scrollBy(0, move.toInt())
            onCursorChanged(cursor.copy(y = min(height - 16f, newY)))
            true
        }
        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
            webView.dispatchCursorClick(cursor.x, cursor.y)
            true
        }
        else -> false
    }
}

private fun WebView.dispatchCursorClick(x: Float, y: Float) {
    val downTime = SystemClock.uptimeMillis()
    dispatchTouchEvent(MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0))
    dispatchTouchEvent(MotionEvent.obtain(downTime, downTime + 60L, MotionEvent.ACTION_UP, x, y, 0))
}
