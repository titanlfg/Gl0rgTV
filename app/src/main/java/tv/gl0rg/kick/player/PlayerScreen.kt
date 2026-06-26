package tv.gl0rg.kick.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val qualityVisible = remember(hlsUrl) { mutableStateOf(false) }
    val isPlaying = remember(hlsUrl) { mutableStateOf(true) }
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

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying.value = playing
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

    LaunchedEffect(controlsVisible.value, qualityVisible.value, quality.value, isPlaying.value) {
        if (controlsVisible.value && !qualityVisible.value) {
            delay(7000)
            controlsVisible.value = false
        }
    }

    BackHandler(enabled = controlsVisible.value) {
        if (qualityVisible.value) qualityVisible.value = false else controlsVisible.value = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    // OK toggles play/pause and reveals the bar; when the bar is
                    // already up, let the focused control handle OK.
                    Key.DirectionCenter, Key.Enter -> {
                        if (controlsVisible.value) {
                            false
                        } else {
                            if (player.isPlaying) player.pause() else player.play()
                            controlsVisible.value = true
                            true
                        }
                    }
                    Key.MediaPlayPause, Key.Spacebar -> {
                        if (player.isPlaying) player.pause() else player.play()
                        true
                    }
                    Key.Menu, Key.DirectionUp, Key.DirectionDown -> {
                        controlsVisible.value = true
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
                    useController = false
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
            PlayerControls(
                isLive = isLive,
                isPlaying = isPlaying.value,
                quality = quality.value,
                qualityVisible = qualityVisible.value,
                onPlayPause = { if (player.isPlaying) player.pause() else player.play() },
                onToggleQuality = { qualityVisible.value = !qualityVisible.value },
                onQuality = {
                    quality.value = it
                    trackSelector.parameters = trackSelector.buildUponParameters()
                        .setMaxVideoSize(it.maxWidth, it.maxHeight)
                        .build()
                    qualityVisible.value = false
                },
                onLive = {
                    player.seekToDefaultPosition()
                    player.play()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 28.dp)
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isLive: Boolean,
    isPlaying: Boolean,
    quality: PlayerQuality,
    qualityVisible: Boolean,
    onPlayPause: () -> Unit,
    onToggleQuality: () -> Unit,
    onQuality: (PlayerQuality) -> Unit,
    onLive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { firstFocus.requestFocus() } }
    Column(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF2000000))))
            .padding(horizontal = 28.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvButton(
                label = if (isPlaying) "❚❚  Pause" else "▶  Play",
                onClick = onPlayPause,
                modifier = Modifier.focusRequester(firstFocus)
            )
            if (isLive) {
                TvButton("● Live", onClick = onLive)
            }
            TvButton(
                label = "⚙  ${quality.label}",
                onClick = onToggleQuality,
                selected = qualityVisible
            )
        }
        if (qualityVisible) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlayerQuality.entries.forEach { item ->
                    TvButton(
                        label = item.label,
                        selected = quality == item,
                        onClick = { onQuality(item) }
                    )
                }
            }
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
