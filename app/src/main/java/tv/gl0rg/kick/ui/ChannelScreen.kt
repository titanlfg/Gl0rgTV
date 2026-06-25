package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickVideo

@Composable
fun ChannelScreen(
    channel: KickChannel,
    videos: List<KickVideo>,
    onBack: () -> Unit,
    onWatch: () -> Unit,
    onWatchVideo: (KickVideo) -> Unit,
    onFavorite: () -> Unit,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Back", onClick = onBack),
            TvNavAction("Channel", selected = true) {}
        )
    ) {
        S0undLikeCanvas {
            ScreenTitle(
                title = channel.safeDisplayName,
                subtitle = channel.stream?.category ?: "Kick channel"
            )
            Spacer(Modifier.height(26.dp))
            InfoTile(
                title = channel.stream?.title ?: "Offline",
                body = if (channel.stream == null) {
                    "No live stream detected for this channel."
                } else {
                    "${channel.stream.viewerCount ?: 0} viewers"
                },
                modifier = Modifier.fillMaxWidth(0.74f)
            )
            Spacer(Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton("Watch", onClick = onWatch, enabled = channel.stream != null)
                TvButton("Favorite", onClick = onFavorite)
            }
            StatusText(statusMessage)
            VideoRow(
                title = "Recent Videos (${videos.size})",
                videos = videos,
                emptyText = "No public videos found for this channel.",
                onOpenVideo = onWatchVideo
            )
            Text(
                text = "Slug: ${channel.slug}",
                color = Gl0rgMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun VideoRow(
    title: String,
    videos: List<KickVideo>,
    emptyText: String,
    onOpenVideo: (KickVideo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = Gl0rgText,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(12.dp))
        if (videos.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted, fontSize = 14.sp)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                videos.take(5).forEach { video ->
                    PreviewCard(
                        title = video.title,
                        subtitle = "${video.views ?: 0} views",
                        imageUrl = video.thumbnailUrl,
                        onClick = { onOpenVideo(video) }
                    )
                }
            }
        }
    }
}
