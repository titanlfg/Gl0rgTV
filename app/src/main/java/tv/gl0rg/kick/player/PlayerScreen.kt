package tv.gl0rg.kick.player

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun PlayerScreen(route: PlaybackRoute, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        when (route) {
            is PlaybackRoute.Native -> NativePlayer(route.hlsUrl)
            is PlaybackRoute.WebViewFallback -> WebViewPlayer(route.url)
        }
    }
}

@Composable
private fun NativePlayer(hlsUrl: String) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val wasPlaying = remember { mutableStateOf(false) }
    val player = remember(hlsUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(hlsUrl))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    wasPlaying.value = player.playWhenReady
                    player.pause()
                }
                Lifecycle.Event.ON_START -> {
                    if (wasPlaying.value) {
                        player.play()
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PlayerView(it).apply {
                useController = true
            }
        },
        update = { playerView ->
            playerView.player = player
        },
        onRelease = { playerView ->
            playerView.player = null
        }
    )
}

@Composable
private fun WebViewPlayer(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: android.webkit.WebResourceRequest): Boolean {
                        return !request.url.isKickHttpsHost()
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) {
                webView.loadUrl(url)
            }
        },
        onRelease = { webView ->
            webView.stopLoading()
            webView.onPause()
            webView.pauseTimers()
            webView.webViewClient = WebViewClient()
            webView.destroy()
        }
    )
}

private fun Uri.isKickHttpsHost(): Boolean {
    val host = host?.lowercase() ?: return false
    return scheme == "https" && (host == "kick.com" || host.endsWith(".kick.com"))
}
