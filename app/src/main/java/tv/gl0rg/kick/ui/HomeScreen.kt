package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    onOpenChannel: (String) -> Unit,
    onBrowseCategory: (String) -> Unit,
    favorites: List<KickChannel>,
    liveStreams: List<KickStream>,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Home", selected = true) {},
            TvNavAction("Search", onClick = onSearch),
            TvNavAction("Login", onClick = onLogin),
            TvNavAction("Settings", onClick = onSettings)
        )
    ) {
        S0undLikeCanvas {
            ScreenTitle(
                title = "Followed (${favorites.size})",
                subtitle = "Browse rows with D-pad. Focused tiles expand."
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
                TvButton("Sign In", onClick = onLogin)
            }
            StatusText(statusMessage)
        }
    }
}
