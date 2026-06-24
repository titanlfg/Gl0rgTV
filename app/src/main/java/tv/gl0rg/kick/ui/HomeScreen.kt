package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
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
        Column {
            ScreenTitle(
                title = "Live streams",
                subtitle = "Browse Kick from the couch."
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                InfoTile(
                    title = "Followed live",
                    body = "Sign in to load followed channels when Kick session parsing is available.",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    title = "Favorites",
                    body = "Saved channels stay on this device and survive app restarts.",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    title = "Trending",
                    body = "Search for a channel slug to open and play live streams.",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(30.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton("Find Channel", onClick = onSearch)
                TvButton("Sign In", onClick = onLogin)
            }
        }
    }
}
