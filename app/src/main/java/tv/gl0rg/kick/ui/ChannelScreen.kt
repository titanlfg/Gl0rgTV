package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.gl0rg.kick.kick.KickChannel

@Composable
fun ChannelScreen(
    channel: KickChannel,
    onWatch: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp)) {
        Text(channel.safeDisplayName)
        Text(channel.stream?.title ?: "Offline")
        Button(onClick = onWatch) { Text("Watch") }
        Button(onClick = onFavorite) { Text("Favorite") }
    }
}
