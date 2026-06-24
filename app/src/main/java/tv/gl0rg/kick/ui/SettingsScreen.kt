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
    isLoggedIn: Boolean,
    onRefreshLogin: () -> Unit,
    updateStatus: String?,
    updateAvailable: Boolean,
    onCheckUpdate: () -> Unit,
    onInstallUpdate: () -> Unit,
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
                InfoTile("Kick session", if (isLoggedIn) "Logged in. Use Login / Refresh if streams need a new session." else "Not logged in. Login / Refresh opens the QR helper.", Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                InfoTile("Remote shortcuts", "OK: controls. Back: exit player. Live: catch up stream.", Modifier.weight(1f))
                InfoTile("Quality selector", "Use player overlay to switch Auto, 1080p, 720p, or 480p.", Modifier.weight(1f))
                InfoTile("Custom binds", "Shortcut remapping screen planned after device testing.", Modifier.weight(1f))
            }
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TvButton("Check Update", onClick = onCheckUpdate)
                TvButton("Install Update", onClick = onInstallUpdate, enabled = updateAvailable)
                TvButton("Login / Refresh", onClick = onRefreshLogin)
                TvButton("Sign Out", onClick = onSignOut)
            }
            Spacer(Modifier.height(18.dp))
            StatusText(updateStatus)
        }
    }
}
