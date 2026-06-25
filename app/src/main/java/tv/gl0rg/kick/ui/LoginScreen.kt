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
                subtitle = "Scan the QR code or open the address on a computer on the same Wi-Fi."
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
                        title = "Log in to Kick",
                        body = "The helper opens real Kick. After login, copy the Kick Cookie header and return to the helper page."
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            InfoTile(
                title = "Submit Cookie header",
                body = "Computer is easiest: F12, Network, refresh Kick, select a kick.com request, copy Request Headers > Cookie, paste, submit."
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
