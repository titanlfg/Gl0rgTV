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
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Back", onClick = onBack),
            TvNavAction("Settings", selected = true) {}
        )
    ) {
        Column {
            ScreenTitle(
                title = "Settings",
                subtitle = "Playback and session controls."
            )
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                InfoTile("Player mode", "Native HLS first, Kick WebView fallback.", Modifier.weight(1f))
                InfoTile("Quality", "Auto.", Modifier.weight(1f))
                InfoTile("Mature gate", "On.", Modifier.weight(1f))
            }
            Spacer(Modifier.height(28.dp))
            TvButton("Sign Out", onClick = onSignOut)
        }
    }
}
