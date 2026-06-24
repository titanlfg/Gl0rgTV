package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
                subtitle = "Scan the QR code with a phone on the same Wi-Fi."
            )
            Spacer(Modifier.height(28.dp))
            Row {
                localLoginUrl?.let { QrCode(it) }
                Spacer(Modifier.width(24.dp))
                Column {
                    InfoTile(
                        title = "Open this address",
                        body = localLoginUrl ?: "Starting local login server..."
                    )
                    Spacer(Modifier.height(16.dp))
                    InfoTile(
                        title = "Log in on phone",
                        body = "The page opens Kick in your browser, then returns you to paste the session value."
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            InfoTile(
                title = "Submit token",
                body = "Paste the copied Kick Cookie header into the token box and submit. Gl0rgTV closes this screen after a valid session."
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
