package tv.gl0rg.kick.ui

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import tv.gl0rg.kick.kick.KickSessionProvider
import tv.gl0rg.kick.kick.WebViewKickSessionProvider

@Composable
fun LoginScreen(
    onLoginObserved: () -> Unit,
    sessionProvider: KickSessionProvider = WebViewKickSessionProvider(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Text("Login uses Kick web session. This unofficial mode can stop working when Kick changes its site.")
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                WebView(context).apply {
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
                    loadUrl("https://kick.com")
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.destroy()
            }
        )
    }
}

private fun Uri.isKickHost(): Boolean {
    val host = host?.lowercase() ?: return false
    return scheme == "https" && (host == "kick.com" || host.endsWith(".kick.com"))
}
