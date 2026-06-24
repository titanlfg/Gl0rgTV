package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Button(onClick = onSignOut) { Text("Sign Out") }
        Text("Player mode: Native first")
        Text("Quality: Auto")
        Text("Mature gate: On")
        Text("Unofficial Kick viewer. Not affiliated with Kick.")
    }
}
