package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeOption: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit,
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
        S0undLikeCanvas {
            ScreenTitle(
                title = "Settings",
                subtitle = "Appearance, playback, and session controls."
            )
            Column {
                SectionHeader("Theme")
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ThemeOption.entries.forEach { option ->
                        CategoryChip(
                            label = option.displayName,
                            onClick = { onThemeChange(option) },
                            selected = option == themeOption
                        )
                    }
                }
            }
            InfoTile("Player mode", "Native HLS first, Kick WebView fallback.", Modifier.fillMaxWidth(0.86f))
            InfoTile("Quality", "Auto by default. Player overlay offers Auto, 1080p, 720p, and 480p.", Modifier.fillMaxWidth(0.86f))
            InfoTile("Kick session", if (isLoggedIn) "Logged in. Use Login / Refresh if Kick asks for a new session." else "Not logged in. Login / Refresh opens the QR login helper.", Modifier.fillMaxWidth(0.86f))
            InfoTile("Remote shortcuts", "OK opens controls. Back exits player. Live catches up the stream.", Modifier.fillMaxWidth(0.86f))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TvButton("Check Update", onClick = onCheckUpdate, modifier = Modifier.width(180.dp))
                TvButton("Install Update", onClick = onInstallUpdate, enabled = updateAvailable, modifier = Modifier.width(180.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TvButton("Login / Refresh", onClick = onRefreshLogin, modifier = Modifier.width(180.dp))
                TvButton("Sign Out", onClick = onSignOut, modifier = Modifier.width(180.dp))
            }
            StatusText(updateStatus)
        }
    }
}
