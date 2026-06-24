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

@Composable
fun ChannelScreen(
    channel: KickChannel,
    onBack: () -> Unit,
    onWatch: () -> Unit,
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
        Column {
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
            Spacer(Modifier.height(18.dp))
            StatusText(statusMessage)
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Slug: ${channel.slug}",
                color = Gl0rgMuted,
                fontSize = 14.sp
            )
        }
    }
}
