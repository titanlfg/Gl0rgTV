package tv.gl0rg.kick.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.media3.common.PlaybackException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import tv.gl0rg.kick.ui.Gl0rgBackground
import tv.gl0rg.kick.ui.Gl0rgMuted
import tv.gl0rg.kick.ui.Gl0rgText
import tv.gl0rg.kick.ui.KickGreen
import tv.gl0rg.kick.ui.TvButton

@Composable
fun PlayerScreen(route: PlaybackRoute, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (route) {
            is PlaybackRoute.Native -> NativePlayer(route.hlsUrl, isLive = route.isLive)
            is PlaybackRoute.WebViewFallback -> WebViewPlayer(route.url)
        }
    }
}

@Composable
private fun NativePlayer(hlsUrl: String, isLive: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val wasPlaying = remember { mutableStateOf(false) }
    val playbackError = remember(hlsUrl) { mutableStateOf<String?>(null) }
    val quality = remember(hlsUrl) { mutableStateOf(PlayerQuality.Auto) }
    val controlsVisible = remember(hlsUrl) { mutableStateOf(false) }
    val trackSelector = remember(hlsUrl) { DefaultTrackSelector(context) }
    val playerResult = remember(hlsUrl) {
        runCatching {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Android TV; Gl0rgTV)")
                .setDefaultRequestProperties(
                    mapOf(
                        "Origin" to "https://kick.com",
                        "Referer" to "https://kick.com/"
                    )
                )
            val mediaSource = HlsMediaSource.Factory(httpDataSourceFactory)
                .createMediaSource(MediaItem.fromUri(hlsUrl))
            ExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build()
                .apply {
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        playbackError.value = error.message ?: error.errorCodeName
                    }
                })
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
            }
        }
    }
    val player = playerResult.getOrNull()
    if (player == null) {
        PlayerError(playerResult.exceptionOrNull()?.message ?: "Could not start player")
        return
    }

    playbackError.value?.let {
        PlayerError(it)
        return
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

    LaunchedEffect(controlsVisible.value, quality.value) {
        if (controlsVisible.value) {
            delay(7000)
            controlsVisible.value = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.DirectionCenter, Key.Enter, Key.Menu, Key.DirectionUp -> {
                        controlsVisible.value = true
                        true
                    }
                    Key.MediaPlayPause, Key.Spacebar -> {
                        if (player.isPlaying) player.pause() else player.play()
                        true
                    }
                    Key.MediaFastForward -> {
                        if (isLive) {
                            player.seekToDefaultPosition()
                            player.play()
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            }
    ) {
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
        if (controlsVisible.value) {
            PlayerControlOverlay(
                isLive = isLive,
                quality = quality.value,
                onQuality = {
                    quality.value = it
                    trackSelector.parameters = trackSelector.buildUponParameters()
                        .setMaxVideoSize(it.maxWidth, it.maxHeight)
                        .build()
                },
                onLive = {
                    player.seekToDefaultPosition()
                    player.play()
                },
                onHide = { controlsVisible.value = false },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
            )
        }
    }
}

@Composable
private fun PlayerControlOverlay(
    isLive: Boolean,
    quality: PlayerQuality,
    onQuality: (PlayerQuality) -> Unit,
    onLive: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Gl0rgBackground.copy(alpha = 0.78f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Playback", color = Gl0rgText, fontSize = 16.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (isLive) {
                TvButton("Live", onClick = onLive)
            }
            PlayerQuality.entries.forEach { item ->
                TvButton(
                    label = item.label,
                    selected = quality == item,
                    onClick = { onQuality(item) }
                )
            }
            TvButton("Hide", onClick = onHide)
        }
    }
}

private enum class PlayerQuality(val label: String, val maxWidth: Int, val maxHeight: Int) {
    Auto("Auto", Int.MAX_VALUE, Int.MAX_VALUE),
    Source("Source", Int.MAX_VALUE, Int.MAX_VALUE),
    High("1080p", 1920, 1080),
    Medium("720p", 1280, 720),
    Low("480p", 854, 480)
}

@Composable
private fun PlayerError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
    ) {
        Column {
            Text("Playback failed", color = KickGreen, fontSize = 28.sp)
            Text(message, color = Gl0rgText, fontSize = 16.sp, modifier = Modifier.padding(top = 14.dp))
            Text("Press Back and try another stream.", color = Gl0rgMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 16.dp))
        }
    }
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

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
