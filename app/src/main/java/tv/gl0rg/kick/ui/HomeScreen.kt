package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
    onOpenStream: (KickStream) -> Unit,
    onBrowseCategory: (String, String) -> Unit,
    heroStream: KickStream?,
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
            TvNavAction("Home", selected = true) { scrollToSection(0) },
            TvNavAction("Followed") { scrollToSection(520) },
            TvNavAction("Top Streamers") { scrollToSection(820) },
            TvNavAction("Categories") { scrollToSection(1080) },
            TvNavAction("Settings", onClick = onSettings)
        )
    ) {
        S0undLikeCanvas(scrollState = scrollState) {
            if (heroStream != null) {
                HeroFeature(
                    stream = heroStream,
                    onWatch = { onOpenStream(heroStream) }
                )
            }
            ChannelRow(
                title = "Followed Channels (${favorites.size})",
                channels = favorites,
                emptyText = "Favorite channels from a channel page to see them here.",
                onOpenChannel = onOpenChannel,
                onOpenStream = onOpenStream
            )
            StreamRow(
                title = "Top Streamers",
                streams = liveStreams,
                emptyText = "Live streamers unavailable right now.",
                onOpenChannel = onOpenChannel,
                onOpenStream = onOpenStream
            )
            Column {
                SectionHeader("Categories")
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        CategoryChip(
                            label = name,
                            onClick = { onBrowseCategory(name, slug) },
                            selected = name == selectedCategory
                        )
                    }
                }
            }
            StreamRow(
                title = "$selectedCategory Live",
                streams = categoryStreams,
                emptyText = "No live streamers found in this category.",
                onOpenChannel = onOpenChannel,
                onOpenStream = onOpenStream
            )
            StatusText(statusMessage)
        }
    }
}
