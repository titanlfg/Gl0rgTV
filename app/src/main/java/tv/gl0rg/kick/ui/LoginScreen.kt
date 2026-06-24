package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    localLoginUrl: String?,
    statusMessage: String?,
    onBack: () -> Unit,
    onRestartServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Back", onClick = onBack),
            TvNavAction("Login", selected = true) {}
        )
    ) {
        Column {
            ScreenTitle(
                title = "Login",
                subtitle = "Use a phone or computer on the same network."
            )
            Spacer(Modifier.height(28.dp))
            InfoTile(
                title = "Open this address",
                body = localLoginUrl ?: "Starting local login server..."
            )
            Spacer(Modifier.height(20.dp))
            InfoTile(
                title = "Paste Kick session",
                body = "Sign in to Kick on your phone or computer, copy the Kick Cookie header, paste it into the Gl0rgTV page, then submit."
            )
            Spacer(Modifier.height(24.dp))
            TvButton("Restart Login Server", onClick = onRestartServer)
            Spacer(Modifier.height(18.dp))
            StatusText(statusMessage)
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Kick does not provide a stable TV device login flow like Twitch. This keeps login out of the TV WebView.",
                color = Gl0rgMuted,
                fontSize = 14.sp
            )
        }
    }
}
