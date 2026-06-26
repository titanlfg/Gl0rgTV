package tv.gl0rg.kick.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.gl0rg.kick.kick.LoginPhase

@Composable
fun LoginScreen(
    localLoginUrl: String?,
    statusMessage: String?,
    phase: LoginPhase,
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
                subtitle = "Scan the QR code or open the address on a phone or computer on the same Wi-Fi."
            )
            Spacer(Modifier.height(20.dp))
            LoginPhaseChip(phase)
            Spacer(Modifier.height(24.dp))
            Row {
                localLoginUrl?.let { QrCode(it) }
                Spacer(Modifier.width(24.dp))
                Column {
                    InfoTile(
                        title = "1 · Open this address",
                        body = localLoginUrl ?: "Starting local login server…"
                    )
                    Spacer(Modifier.height(16.dp))
                    InfoTile(
                        title = "2 · Log in to Kick",
                        body = "The helper opens the real Kick site. Sign in there as usual."
                    )
                    Spacer(Modifier.height(16.dp))
                    InfoTile(
                        title = "3 · Paste the Cookie header",
                        body = "Computer is easiest: F12 → Network → refresh Kick → pick a kick.com request → copy Request Headers → Cookie → paste → submit."
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            TvButton("New Login Link", onClick = onRestartServer)
            Spacer(Modifier.height(18.dp))
            StatusText(statusMessage)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Session lasts until Kick expires the cookie or you sign out.",
                color = Gl0rgMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun LoginPhaseChip(phase: LoginPhase) {
    val (label, color) = when (phase) {
        LoginPhase.Waiting -> "● Waiting for your phone…" to KickGreen
        LoginPhase.InvalidCookie -> "● Cookie not recognized — try again" to Gl0rgLive
        LoginPhase.Linked -> "✓ Linked! Returning to home…" to KickGreen
    }
    Text(
        text = label,
        color = Gl0rgAccentText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    )
}
