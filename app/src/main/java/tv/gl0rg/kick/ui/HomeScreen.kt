package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    val activeSection by remember {
        derivedStateOf {
            when (scrollState.value) {
                in 0 until 360 -> 0
                in 360 until 720 -> 1
                in 720 until 1040 -> 2
                else -> 3
            }
        }
    }

    val liveFollowed = HomeFeed.liveFollowed(favorites)

    TvShell(
        modifier = modifier,
        onSearch = onSearch,
        navActions = listOf(
            TvNavAction("Home", selected = activeSection == 0) { scrollToSection(0) },
            TvNavAction("Followed", selected = activeSection == 1) { scrollToSection(560) },
            TvNavAction("Categories", selected = activeSection == 2) { scrollToSection(900) },
            TvNavAction("Top Streamers", selected = activeSection == 3) { scrollToSection(1200) },
            TvNavAction("Settings", onClick = onSettings)
        )
    ) {
        S0undLikeCanvas(scrollState = scrollState) {
            // Live Now — large S0undTV-style preview tiles.
            Column {
                SectionHeader("Followed — Live")
                Spacer(Modifier.height(14.dp))
                if (liveFollowed.isEmpty()) {
                    Text(
                        text = if (favorites.isEmpty()) {
                            "Favorite channels to see them here when they go live."
                        } else {
                            "None of your followed channels are live right now."
                        },
                        color = Gl0rgMuted
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        liveFollowed.take(8).forEach { channel ->
                            val stream = channel.stream ?: return@forEach
                            BigPreviewCard(
                                name = channel.safeDisplayName,
                                subtitle = stream.title.ifBlank { stream.category ?: "Live" },
                                viewers = stream.viewerCount?.let { formatViewers(it) },
                                imageUrl = stream.thumbnailUrl ?: channel.avatarUrl,
                                avatarUrl = channel.avatarUrl,
                                previewHlsUrl = stream.hlsUrl,
                                onClick = { onOpenStream(stream) }
                            )
                        }
                    }
                }
            }
            // Followed channels — circular avatars.
            Column {
                SectionHeader("Followed Channels (${favorites.size})")
                Spacer(Modifier.height(14.dp))
                if (favorites.isEmpty()) {
                    Text("Favorite channels from a channel page to see them here.", color = Gl0rgMuted)
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        favorites.forEach { channel ->
                            AvatarCard(
                                name = channel.safeDisplayName,
                                avatarUrl = channel.avatarUrl,
                                live = channel.stream != null,
                                onClick = {
                                    val stream = channel.stream
                                    if (stream != null) onOpenStream(stream) else onOpenChannel(channel.slug)
                                }
                            )
                        }
                    }
                }
            }
            // Categories.
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
            StreamRow(
                title = "Top Streamers",
                streams = liveStreams,
                emptyText = "Live streamers unavailable right now.",
                onOpenChannel = onOpenChannel,
                onOpenStream = onOpenStream
            )
            StatusText(statusMessage)
        }
    }
}
