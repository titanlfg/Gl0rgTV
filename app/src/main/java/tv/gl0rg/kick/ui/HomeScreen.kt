package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.launch
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onOpenChannel: (String) -> Unit,
    onBrowseCategory: (String) -> Unit,
    favorites: List<KickChannel>,
    liveStreams: List<KickStream>,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    fun scrollToSection(position: Int) {
        scope.launch { scrollState.animateScrollTo(position) }
    }

    TvShell(
        modifier = modifier,
        onSearch = onSearch,
        navActions = listOf(
            TvNavAction("Followed", selected = true) { scrollToSection(0) },
            TvNavAction("Followed Channels") { scrollToSection(420) },
            TvNavAction("Top Games") { scrollToSection(650) },
            TvNavAction("Top Streamers") { scrollToSection(850) },
            TvNavAction("Settings", onClick = onSettings)
        )
    ) {
        S0undLikeCanvas(scrollState = scrollState) {
            ScreenTitle(
                title = "Followed (${favorites.size})",
                subtitle = "Search is the top-left icon. Settings contains login."
            )
            val featured = liveStreams.firstOrNull()
            if (featured != null) {
                FeaturedStreamCard(
                    stream = featured,
                    onClick = { onOpenChannel(featured.slug) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                InfoTile(
                    title = "No live preview",
                    body = "Public Kick channels still browse without login. Search from the top-left icon."
                )
            }
            StreamRow(
                title = "Followed",
                streams = liveStreams.take(1),
                emptyText = "Login from Settings to refresh your Kick session later.",
                onOpenChannel = onOpenChannel
            )
            ChannelRow(
                title = "Followed Channels (${favorites.size})",
                channels = favorites,
                emptyText = "Favorite channels from a channel page.",
                onOpenChannel = onOpenChannel
            )
            StreamRow(
                title = "Top Streamers",
                streams = liveStreams,
                emptyText = "Public live channels load without login.",
                onOpenChannel = onOpenChannel
            )
            Column {
                ScreenTitle("Top Games")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    listOf("Just Chatting", "IRL", "Minecraft", "Grand Theft Auto", "Sports").forEach { category ->
                        TvButton(
                            label = category,
                            onClick = { onBrowseCategory(category) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton("Find Channel", onClick = onSearch)
                TvButton("Settings", onClick = onSettings)
            }
            StatusText(statusMessage)
        }
    }
}
