package tv.gl0rg.kick.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream

internal val KickGreen = Color(0xFF53FC18)
internal val Gl0rgBackground = Color(0xFF090B0A)
internal val Gl0rgPanel = Color(0xFF111713)
internal val Gl0rgPanelSoft = Color(0xFF1A221C)
internal val Gl0rgText = Color(0xFFF3F7F1)
internal val Gl0rgMuted = Color(0xFFAAB4A8)

data class TvNavAction(
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun TvShell(
    navActions: List<TvNavAction>,
    modifier: Modifier = Modifier,
    onSearch: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var navFocused by remember { mutableStateOf(false) }
    val navWidth by animateDpAsState(if (navFocused) 250.dp else 92.dp, label = "navWidth")
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Gl0rgBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .width(navWidth)
                .fillMaxHeight()
                .padding(end = if (navFocused) 24.dp else 10.dp)
                .onFocusChanged { navFocused = it.hasFocus },
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (onSearch != null) {
                    SearchIconButton(onClick = onSearch)
                    Spacer(Modifier.height(24.dp))
                }
                Gl0rgWordmark(compact = !navFocused)
                Spacer(Modifier.height(34.dp))
                navActions.forEach { action ->
                    SideNavItem(action, expanded = navFocused)
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (navFocused) {
                Text(
                    text = "Unofficial Kick viewer",
                    color = Gl0rgMuted,
                    fontSize = 13.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 18.dp)
        ) {
            content()
        }
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
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "searchIconScale")
    Box(
        modifier = modifier
            .width(72.dp)
            .height(72.dp)
            .scale(scale)
            .background(if (focused) KickGreen else Color(0xFFE90073), RoundedCornerShape(36.dp))
            .border(2.dp, if (focused) Color.White else Color.Transparent, RoundedCornerShape(36.dp))
            .onFocusChanged { focused = it.isFocused }
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
    val active = focused || selected
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, label = "tvButtonScale")
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
        border = if (focused) BorderStroke(3.dp, Color.White) else BorderStroke(1.dp, Color(0xFF303B32)),
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
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
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
            .border(1.dp, Color(0xFF2E392F), RoundedCornerShape(8.dp))
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
            maxLines = 3,
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        if (channels.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted, fontSize = 14.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                channels.take(5).forEach { channel ->
                    PreviewCard(
                        title = channel.safeDisplayName,
                        subtitle = channel.slug,
                        imageUrl = channel.avatarUrl,
                        onClick = { onOpenChannel(channel.slug) }
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        if (streams.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted, fontSize = 14.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                streams.take(5).forEach { stream ->
                    PreviewCard(
                        title = stream.slug,
                        subtitle = stream.category ?: "${stream.viewerCount ?: 0} viewers",
                        imageUrl = stream.thumbnailUrl,
                        onClick = { onOpenChannel(stream.slug) }
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
    val scale by animateFloatAsState(if (focused) 1.025f else 1f, label = "featuredCardScale")
    Card(
        modifier = modifier
            .height(300.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .border(
                width = if (focused) 4.dp else 1.dp,
                color = if (focused) KickGreen else Color(0xFF222A24),
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (focused) KickGreen else Gl0rgPanelSoft)
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
                    .background(if (focused) KickGreen else Color(0xDD0A0D0B))
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "previewCardScale")
    Column(
        modifier = modifier
            .width(220.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable(onClick = onClick)
            .border(
                width = if (focused) 4.dp else 1.dp,
                color = if (focused) KickGreen else Color(0xFF222A24),
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (focused) KickGreen else Gl0rgPanelSoft, RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF202820), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = title.take(1).uppercase(),
                    color = if (focused) Gl0rgBackground else KickGreen,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = if (focused) Gl0rgBackground else Gl0rgText,
                fontSize = if (focused) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = if (focused) Gl0rgBackground.copy(alpha = 0.8f) else Gl0rgMuted,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
