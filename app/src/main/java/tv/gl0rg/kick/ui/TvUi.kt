package tv.gl0rg.kick.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream

// Theme-backed color tokens. These resolve from the active [Gl0rgColors] supplied
// by [Gl0rgTheme], so every reference re-skins automatically when the theme changes.
internal val KickGreen: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.accent
internal val Gl0rgBackground: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.background
internal val Gl0rgPanel: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.surface
internal val Gl0rgPanelSoft: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.surfaceAlt
internal val Gl0rgText: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.text
internal val Gl0rgMuted: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.muted
// Extra tokens used by redesigned components.
internal val Gl0rgSurfaceFocus: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.surfaceFocus
internal val Gl0rgAccentText: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.accentText
internal val Gl0rgSecondary: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.secondary
internal val Gl0rgLive: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.live
internal val Gl0rgOutline: Color @Composable @ReadOnlyComposable get() = LocalGl0rgColors.current.outline

/** Compact viewer count, e.g. 950, 12.3K, 1.2M. */
fun formatViewers(count: Int): String = when {
    count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
    count >= 1_000 -> "%.1fK".format(count / 1_000.0)
    else -> count.toString()
}

data class TvNavAction(
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

private val LocalOpenTvMenu = staticCompositionLocalOf<(() -> Unit)?> { null }

private fun Modifier.openTvMenuOnKey(openMenu: (() -> Unit)?): Modifier =
    if (openMenu == null) {
        this
    } else {
        onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown && (event.key == Key.DirectionLeft || event.key == Key.Menu)) {
                openMenu()
                true
            } else {
                false
            }
        }
    }

@Composable
fun TvShell(
    navActions: List<TvNavAction>,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var navOpen by remember { mutableStateOf(false) }
    val navFocusRequester = remember { FocusRequester() }
    LaunchedEffect(navOpen) {
        if (navOpen) {
            runCatching { navFocusRequester.requestFocus() }
        }
    }

    CompositionLocalProvider(LocalOpenTvMenu provides { navOpen = true }) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Gl0rgBackground)
                .padding(24.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.DirectionLeft, Key.Menu -> {
                            if (!navOpen) {
                                navOpen = true
                                true
                            } else {
                                false
                            }
                        }
                        Key.DirectionRight -> {
                            if (navOpen) {
                                navOpen = false
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                }
        ) {
        if (navOpen) {
            Column(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .alpha(1f)
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (onSearch != null) {
                        SearchIconButton(
                            onClick = onSearch,
                            modifier = Modifier.focusRequester(navFocusRequester)
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    Gl0rgWordmark(compact = false)
                    Spacer(Modifier.height(34.dp))
                    navActions.forEachIndexed { index, action ->
                        val itemModifier = if (onSearch == null && index == 0) {
                            Modifier.focusRequester(navFocusRequester)
                        } else {
                            Modifier
                        }
                        SideNavItem(action, expanded = true, modifier = itemModifier)
                        Spacer(Modifier.height(12.dp))
                    }
                }
                Text(
                    text = "Unofficial Kick viewer",
                    color = Gl0rgMuted,
                    fontSize = 13.sp
                )
            }
        } else {
            MenuEdgeHandle(
                onOpen = { navOpen = true },
                modifier = Modifier.fillMaxHeight()
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = if (navOpen) 18.dp else 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = if (!navOpen && onSearch != null) 96.dp else 0.dp)
            ) {
                content()
            }
            if (!navOpen && onSearch != null) {
                SearchIconButton(
                    onClick = onSearch,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 4.dp)
                )
            }
            if (!navOpen) {
                Text(
                    text = "Press Left for menu",
                    color = Gl0rgMuted.copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 4.dp)
                )
            }
        }
        }
    }
}

@Composable
private fun MenuEdgeHandle(
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .width(18.dp)
            .padding(end = 8.dp)
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onOpen()
            }
            .focusable()
            .clickable(onClick = onOpen)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(if (focused) 8.dp else 5.dp)
                .height(96.dp)
                .background(KickGreen.copy(alpha = if (focused) 1f else 0.7f), RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun SideNavItem(action: TvNavAction, expanded: Boolean, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val active = focused || action.selected
    val color by animateColorAsState(
        if (active) Gl0rgText else Gl0rgMuted,
        label = "sideNavColor"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = action.onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(5.dp)
                .height(if (active) 34.dp else 0.dp)
                .background(KickGreen, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(14.dp))
        if (expanded) {
            Text(
                text = action.label,
                color = color,
                fontSize = if (active) 24.sp else 21.sp,
                fontWeight = if (active) FontWeight.Black else FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = action.label.take(1),
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun SearchIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val openMenu = LocalOpenTvMenu.current
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "searchIconScale")
    Box(
        modifier = modifier
            .width(72.dp)
            .height(72.dp)
            .scale(scale)
            .background(if (focused) KickGreen else Color(0xFFE90073), RoundedCornerShape(36.dp))
            .border(2.dp, if (focused) Color.White else Color.Transparent, RoundedCornerShape(36.dp))
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .openTvMenuOnKey(openMenu)
            .clickable(onClick = onClick)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 6f, cap = StrokeCap.Round)
            drawCircle(
                color = Color.White,
                radius = size.minDimension * 0.19f,
                center = Offset(size.width * 0.45f, size.height * 0.42f),
                style = stroke
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.58f, size.height * 0.58f),
                end = Offset(size.width * 0.72f, size.height * 0.72f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun Gl0rgWordmark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(modifier = modifier) {
        Text(
            text = if (compact) "G0" else "Gl0rgTV",
            color = KickGreen,
            fontSize = if (compact) 26.sp else 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp
        )
        Box(
            Modifier
                .padding(top = 6.dp)
                .width(if (compact) 44.dp else 120.dp)
                .height(4.dp)
                .background(KickGreen)
        )
    }
}

@Composable
fun TvButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    var focused by remember { mutableStateOf(false) }
    val openMenu = LocalOpenTvMenu.current
    val active = focused || selected
    val scale by animateFloatAsState(if (focused) 1.025f else 1f, label = "tvButtonScale")
    val background by animateColorAsState(
        when {
            !enabled -> Color(0xFF202620)
            active -> KickGreen
            else -> Gl0rgPanelSoft
        },
        label = "tvButtonBackground"
    )
    val content by animateColorAsState(
        if (active && enabled) Gl0rgBackground else Gl0rgText,
        label = "tvButtonContent"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        border = if (focused) BorderStroke(3.dp, Color.White) else BorderStroke(1.dp, Color(0xFF263127)),
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = content,
            disabledContainerColor = background,
            disabledContentColor = Gl0rgMuted
        ),
        modifier = modifier
            .height(54.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .openTvMenuOnKey(openMenu)
    ) {
        Text(
            text = label,
            fontSize = 17.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ScreenTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = Gl0rgMuted,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
fun InfoTile(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Gl0rgPanelSoft, RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF263127), RoundedCornerShape(8.dp))
            .padding(18.dp)
    ) {
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = body,
            color = Gl0rgMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusText(text: String?, modifier: Modifier = Modifier) {
    if (text != null) {
        Text(
            text = text,
            color = KickGreen,
            fontSize = 15.sp,
            modifier = modifier
        )
    }
}

@Composable
fun ChannelRow(
    title: String,
    channels: List<KickChannel>,
    emptyText: String,
    onOpenChannel: (String) -> Unit,
    onOpenStream: (KickStream) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = title)
        Spacer(Modifier.height(14.dp))
        if (channels.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted, fontSize = 14.sp)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                channels.take(12).forEach { channel ->
                    PreviewCard(
                        title = channel.safeDisplayName,
                        subtitle = channel.stream?.category ?: channel.slug,
                        imageUrl = channel.stream?.thumbnailUrl ?: channel.avatarUrl,
                        previewHlsUrl = channel.stream?.hlsUrl,
                        live = channel.stream != null,
                        viewers = channel.stream?.viewerCount?.let { formatViewers(it) },
                        onClick = {
                            val stream = channel.stream
                            if (stream != null) onOpenStream(stream) else onOpenChannel(channel.slug)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StreamRow(
    title: String,
    streams: List<KickStream>,
    emptyText: String,
    onOpenChannel: (String) -> Unit,
    onOpenStream: (KickStream) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(title = title)
        Spacer(Modifier.height(14.dp))
        if (streams.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted, fontSize = 14.sp)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                streams.take(12).forEach { stream ->
                    PreviewCard(
                        title = stream.slug,
                        subtitle = stream.category ?: "Live now",
                        imageUrl = stream.thumbnailUrl,
                        previewHlsUrl = stream.hlsUrl,
                        live = true,
                        viewers = stream.viewerCount?.let { formatViewers(it) },
                        onClick = {
                            if (!stream.hlsUrl.isNullOrBlank()) onOpenStream(stream) else onOpenChannel(stream.slug)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedStreamCard(
    stream: KickStream,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.015f else 1f, label = "featuredCardScale")
    Card(
        modifier = modifier
            .height(300.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .border(
                width = if (focused) 4.dp else 1.dp,
                color = if (focused) Color.White else Color(0xFF202820),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Gl0rgPanelSoft)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (!stream.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = stream.thumbnailUrl,
                    contentDescription = stream.slug,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(Color(0xFFE1003C), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("LIVE", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(if (focused) KickGreen else Color(0xE6060807))
                    .padding(18.dp)
            ) {
                Text(
                    text = stream.slug,
                    color = if (focused) Gl0rgBackground else Gl0rgText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOfNotNull(stream.category, stream.viewerCount?.let { "$it viewers" }).joinToString(" | ")
                        .ifBlank { "Kick live stream" },
                    color = if (focused) Gl0rgBackground.copy(alpha = 0.82f) else Gl0rgMuted,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun S0undLikeCanvas(
    scrollState: androidx.compose.foundation.ScrollState = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(end = 28.dp, bottom = 42.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
        content = content
    )
}

@Composable
fun PreviewCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    previewHlsUrl: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    live: Boolean = false,
    viewers: String? = null
) {
    var focused by remember { mutableStateOf(false) }
    val openMenu = LocalOpenTvMenu.current
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "previewCardScale")
    val accent = KickGreen
    val cardShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .width(232.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .openTvMenuOnKey(openMenu)
            .clickable(onClick = onClick)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) accent else Gl0rgOutline,
                shape = cardShape
            )
            .background(if (focused) Gl0rgSurfaceFocus else Gl0rgPanelSoft, cardShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Gl0rgPanel, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        ) {
            if (focused && !previewHlsUrl.isNullOrBlank()) {
                FocusedLivePreview(
                    hlsUrl = previewHlsUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = title.take(1).uppercase(),
                    color = accent,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // Bottom scrim for legibility over thumbnails.
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))
                    )
            )
            if (live) {
                LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding(10.dp))
            }
            if (!viewers.isNullOrBlank()) {
                CountPill(text = viewers, modifier = Modifier.align(Alignment.TopEnd).padding(10.dp))
            }
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                color = Gl0rgText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = if (focused) accent else Gl0rgMuted,
                fontSize = 13.sp,
                fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LiveBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Gl0rgLive, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(7.dp)
                .height(7.dp)
                .background(Color.White, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text("LIVE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun CountPill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(Color(0xB3000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun CategoryChip(
    label: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val openMenu = LocalOpenTvMenu.current
    val active = focused || selected
    val scale by animateFloatAsState(if (focused) 1.05f else 1f, label = "chipScale")
    val background by animateColorAsState(
        when {
            focused -> KickGreen
            selected -> Gl0rgSurfaceFocus
            else -> Gl0rgPanelSoft
        },
        label = "chipBackground"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .height(48.dp)
            .background(background, RoundedCornerShape(24.dp))
            .border(
                width = if (active) 2.dp else 1.dp,
                color = if (focused) Color.White else if (selected) KickGreen else Gl0rgOutline,
                shape = RoundedCornerShape(24.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .openTvMenuOnKey(openMenu)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (focused) Gl0rgAccentText else Gl0rgText,
            fontSize = 16.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .width(5.dp)
                .height(22.dp)
                .background(KickGreen, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun HeroFeature(
    stream: KickStream,
    onWatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val openMenu = LocalOpenTvMenu.current
    val heroShape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .openTvMenuOnKey(openMenu)
            .clickable(onClick = onWatch)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) KickGreen else Gl0rgOutline,
                shape = heroShape
            )
            .background(Gl0rgPanel, heroShape)
    ) {
        if (focused && !stream.hlsUrl.isNullOrBlank()) {
            FocusedLivePreview(hlsUrl = stream.hlsUrl, modifier = Modifier.fillMaxSize())
        } else if (!stream.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = stream.thumbnailUrl,
                contentDescription = stream.slug,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color(0xF2000000)))
                )
        )
        LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Text(
                text = "FEATURED",
                color = KickGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stream.slug,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = listOfNotNull(
                    stream.category,
                    stream.viewerCount?.let { "$it watching" }
                ).joinToString("  •  ").ifBlank { "Kick live stream" },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .background(if (focused) KickGreen else Color.White, RoundedCornerShape(6.dp))
                    .padding(horizontal = 26.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "▶  Watch",
                    color = if (focused) Gl0rgAccentText else Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun FocusedLivePreview(
    hlsUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val player = remember(hlsUrl) {
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
        ExoPlayer.Builder(context).build().apply {
            volume = 1f
            repeatMode = Player.REPEAT_MODE_OFF
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                useController = false
                this.player = player
            }
        },
        update = { it.player = player },
        onRelease = { it.player = null }
    )
}
