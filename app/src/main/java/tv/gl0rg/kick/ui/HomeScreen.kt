package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
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
    onBrowseCategory: (String, String) -> Unit,
    favorites: List<KickChannel>,
    liveStreams: List<KickStream>,
    selectedCategory: String,
    categoryStreams: List<KickStream>,
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
            TvNavAction("Categories") { scrollToSection(650) },
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
            ChannelRow(
                title = "Followed Channels (${favorites.size})",
                channels = favorites,
                emptyText = "Favorite channels from a channel page.",
                onOpenChannel = onOpenChannel
            )
            StreamRow(
                title = "Top Streamers",
                streams = liveStreams,
                emptyText = "Live streamers unavailable right now.",
                onOpenChannel = onOpenChannel
            )
            Column {
                ScreenTitle("Categories")
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val categories = listOf(
                        "Just Chatting" to "just-chatting",
                        "IRL" to "irl",
                        "Minecraft" to "minecraft",
                        "Grand Theft Auto V" to "grand-theft-auto-v",
                        "Sports" to "sports",
                        "Slots" to "slots"
                    )
                    categories.forEach { (name, slug) ->
                        TvButton(
                            label = name,
                            onClick = { onBrowseCategory(name, slug) },
                            selected = name == selectedCategory,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
            StreamRow(
                title = "$selectedCategory Live",
                streams = categoryStreams,
                emptyText = "No live streamers found in this category.",
                onOpenChannel = onOpenChannel
            )
            Column {
                ScreenTitle("Tools")
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    TvButton("Find Channel", onClick = onSearch, modifier = Modifier.width(180.dp))
                    TvButton("Settings", onClick = onSettings, modifier = Modifier.width(180.dp))
                }
            }
            StatusText(statusMessage)
        }
    }
}
