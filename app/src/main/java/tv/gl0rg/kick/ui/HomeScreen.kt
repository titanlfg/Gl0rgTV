package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onSearch: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(32.dp)) {
        Text("Gl0rgTV")
        Spacer(Modifier.height(24.dp))
        Row {
            Button(onClick = onSearch) { Text("Search") }
            Button(onClick = onLogin) { Text("Login") }
            Button(onClick = onSettings) { Text("Settings") }
        }
        Spacer(Modifier.height(32.dp))
        Text("Followed Live")
        Text("Login to load followed channels. Local favorites work without login.")
        Spacer(Modifier.height(24.dp))
        Text("Favorites")
        Text("No favorites yet.")
        Spacer(Modifier.height(24.dp))
        Text("Trending")
        Text("Streams load after Kick integration is connected.")
    }
}
