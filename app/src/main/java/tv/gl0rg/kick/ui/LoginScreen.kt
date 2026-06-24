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
                title = "Link Kick Session",
                subtitle = "Use a phone or computer on the same network. No TV browser needed."
            )
            Spacer(Modifier.height(28.dp))
            InfoTile(
                title = "Open this address",
                body = localLoginUrl ?: "Starting local login server..."
            )
            Spacer(Modifier.height(20.dp))
            InfoTile(
                title = "Paste Cookie header",
                body = "Log into Kick in your normal browser, copy the Kick Cookie header, paste it into the page, then submit."
            )
            Spacer(Modifier.height(24.dp))
            TvButton("New Login Link", onClick = onRestartServer)
            Spacer(Modifier.height(18.dp))
            StatusText(statusMessage)
            Spacer(Modifier.height(18.dp))
            Text(
                text = "Session lasts until Kick expires the cookie or you sign out.",
                color = Gl0rgMuted,
                fontSize = 14.sp
            )
        }
    }
}
