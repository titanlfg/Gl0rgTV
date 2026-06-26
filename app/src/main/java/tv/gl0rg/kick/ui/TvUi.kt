package tv.gl0rg.kick.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
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
    val contentFocus = remember { FocusRequester() }
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Gl0rgBackground)
    ) {
        NavRail(navActions = navActions, onSearch = onSearch)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 40.dp, top = 36.dp, end = 40.dp)
                .focusRequester(contentFocus)
                .focusGroup()
        ) {
            content()
        }
    }
    // Send initial focus into the content, not the rail, so returning from a
    // stream or first launch lands on the page instead of the side menu.
    LaunchedEffect(Unit) {
        runCatching { contentFocus.requestFocus() }
    }
}

@Composable
private fun NavRail(
    navActions: List<TvNavAction>,
    onSearch: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val width by animateDpAsState(if (expanded) 252.dp else 98.dp, label = "railWidth")
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(Gl0rgPanel)
            .onFocusChanged { expanded = it.hasFocus }
            .focusGroup()
            .padding(horizontal = 18.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Gl0rgWordmark(compact = !expanded)
        Spacer(Modifier.height(28.dp))
        if (onSearch != null) {
            RailItem(
                label = "Search",
                icon = Icons.Filled.Search,
                selected = false,
                expanded = expanded,
                onClick = onSearch
            )
        }
        navActions.forEach { action ->
            RailItem(
                label = action.label,
                icon = iconForLabel(action.label),
                selected = action.selected,
                expanded = expanded,
                onClick = action.onClick
            )
        }
        Spacer(Modifier.weight(1f))
        if (expanded) {
            Text(
                text = "Unofficial Kick viewer",
                color = Gl0rgMuted,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}

private fun iconForLabel(label: String): androidx.compose.ui.graphics.vector.ImageVector = when {
    label.equals("Home", ignoreCase = true) -> Icons.Filled.Home
    label.startsWith("Followed", ignoreCase = true) ||
        label.startsWith("Favorite", ignoreCase = true) -> Icons.Filled.Favorite
    label.startsWith("Top", ignoreCase = true) -> Icons.Filled.Star
    label.startsWith("Categ", ignoreCase = true) -> Icons.Filled.List
    label.equals("Settings", ignoreCase = true) -> Icons.Filled.Settings
    label.equals("Back", ignoreCase = true) -> Icons.Filled.ArrowBack
    label.equals("Search", ignoreCase = true) -> Icons.Filled.Search
    label.equals("Login", ignoreCase = true) -> Icons.Filled.AccountCircle
    label.equals("Channel", ignoreCase = true) -> Icons.Filled.Person
    else -> Icons.Filled.PlayArrow
}

@Composable
private fun RailItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val active = focused || selected
    val background by animateColorAsState(
        if (focused) KickGreen else Color.Transparent,
        label = "railItemBackground"
    )
    val foreground by animateColorAsState(
        when {
            focused -> Gl0rgAccentText
            selected -> Gl0rgText
            else -> Gl0rgMuted
        },
        label = "railItemForeground"
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(background, RoundedCornerShape(14.dp))
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected && !focused) {
            Box(
                Modifier
                    .width(4.dp)
                    .height(26.dp)
                    .background(KickGreen, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(9.dp))
        }
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = foreground,
            modifier = Modifier.size(28.dp)
        )
        if (expanded) {
            Spacer(Modifier.width(16.dp))
            Text(
                text = label,
                color = foreground,
                fontSize = 18.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (compact) 56.dp else 46.dp)
                .background(KickGreen, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "G0",
                color = Gl0rgAccentText,
                fontSize = if (compact) 22.sp else 19.sp,
                fontWeight = FontWeight.Black
            )
        }
        if (!compact) {
            Spacer(Modifier.width(12.dp))
            Row {
                Text("Gl0rg", color = Gl0rgText, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Text("TV", color = KickGreen, fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
        }
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

/**
 * Large S0undTV-style live tile: 16:9 art (thumbnail, live video on focus) with
 * a LIVE badge and viewer count, plus a colored info bar (avatar + name + title)
 * that turns accent when focused.
 */
@Composable
fun BigPreviewCard(
    name: String,
    subtitle: String,
    viewers: String?,
    imageUrl: String?,
    avatarUrl: String?,
    previewHlsUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.03f else 1f, label = "bigCardScale")
    val topShape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
    Column(
        modifier = modifier
            .width(440.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(topShape)
                .background(Gl0rgPanel)
                .border(
                    width = if (focused) 3.dp else 1.dp,
                    color = if (focused) KickGreen else Gl0rgOutline,
                    shape = topShape
                )
        ) {
            PreviewArt(imageUrl = imageUrl, hls = previewHlsUrl, focused = focused, name = name)
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6000000))))
            )
            LiveBadge(modifier = Modifier.align(Alignment.TopStart).padding(12.dp))
            if (!viewers.isNullOrBlank()) {
                Text(
                    text = "$viewers viewers",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .background(if (focused) KickGreen else Gl0rgPanelSoft)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarCircle(url = avatarUrl, name = name, size = 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = if (focused) Gl0rgAccentText else Gl0rgText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = if (focused) Gl0rgAccentText.copy(alpha = 0.85f) else Gl0rgMuted,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PreviewArt(imageUrl: String?, hls: String?, focused: Boolean, name: String) {
    if (focused && !hls.isNullOrBlank()) {
        FocusedLivePreview(hlsUrl = hls, modifier = Modifier.fillMaxSize())
    } else if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        PlaceholderArt(name = name, modifier = Modifier.fillMaxSize())
    }
}

/** Never-black fallback art: a branded gradient with the channel initial. */
@Composable
private fun PlaceholderArt(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(Gl0rgPanel, Gl0rgSurfaceFocus))
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = KickGreen,
            fontSize = 64.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun AvatarCircle(url: String?, name: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Gl0rgSurfaceFocus),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.take(1).uppercase(),
                color = KickGreen,
                fontSize = (size.value / 2.4f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/** Circular channel tile used for the Followed Channels row. */
@Composable
fun AvatarCard(
    name: String,
    avatarUrl: String?,
    live: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.08f else 1f, label = "avatarCardScale")
    Column(
        modifier = modifier
            .width(140.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .border(
                        width = if (focused) 3.dp else 2.dp,
                        color = if (focused) KickGreen else Gl0rgOutline,
                        shape = CircleShape
                    )
            ) {
                AvatarCircle(url = avatarUrl, name = name, size = 112.dp)
            }
            if (live) {
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Gl0rgBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Gl0rgLive)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = name,
            color = if (focused) KickGreen else Gl0rgText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    // Debounce: only spin up an ExoPlayer once focus settles, so scrolling past
    // many cards doesn't create/destroy players in a burst (which exhausts video
    // decoders and leaves later previews black).
    var ready by remember(hlsUrl) { mutableStateOf(false) }
    LaunchedEffect(hlsUrl) {
        kotlinx.coroutines.delay(320)
        ready = true
    }
    Box(modifier.background(Gl0rgPanel)) {
        if (ready) {
            LivePreviewSurface(hlsUrl = hlsUrl, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun LivePreviewSurface(
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
