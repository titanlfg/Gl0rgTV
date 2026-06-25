package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tv.gl0rg.kick.kick.KickChannel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenChannel: (String) -> Unit,
    results: List<KickChannel>,
    history: List<String>,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    val query = remember { mutableStateOf("") }
    TvShell(
        modifier = modifier,
        navActions = listOf(
            TvNavAction("Back", onClick = onBack),
            TvNavAction("Search", selected = true) {}
        )
    ) {
        Column {
            ScreenTitle(
                title = "Search",
                subtitle = "Enter a Kick channel name or paste a Kick channel URL."
            )
            Spacer(Modifier.height(30.dp))
            OutlinedTextField(
                value = query.value,
                onValueChange = { query.value = it },
                singleLine = true,
                label = { Text("Channel") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Gl0rgText,
                    unfocusedTextColor = Gl0rgText,
                    focusedBorderColor = KickGreen,
                    unfocusedBorderColor = Color(0xFF3B453C),
                    focusedLabelColor = KickGreen,
                    unfocusedLabelColor = Gl0rgMuted,
                    cursorColor = KickGreen
                ),
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(64.dp)
                    .border(1.dp, Color(0xFF263027), RoundedCornerShape(6.dp))
            )
            Spacer(Modifier.height(20.dp))
            TvButton(
                label = "Search",
                onClick = { onSearch(query.value) },
                enabled = query.value.isNotBlank()
            )
            if (history.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text("History", color = Gl0rgText)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    history.take(4).forEach { previous ->
                        TvButton(
                            label = previous,
                            onClick = {
                                query.value = previous
                                onSearch(previous)
                            },
                            modifier = Modifier.width(160.dp)
                        )
                    }
                    TvButton("Clear", onClick = onClearHistory, modifier = Modifier.width(120.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            StatusText(statusMessage)
            Spacer(Modifier.height(26.dp))
            SearchResultList(
                title = "Results",
                channels = results,
                emptyText = "No results yet.",
                onOpenChannel = onOpenChannel
            )
        }
    }
}

@Composable
private fun SearchResultList(
    title: String,
    channels: List<KickChannel>,
    emptyText: String,
    onOpenChannel: (String) -> Unit
) {
    Column {
        Text(text = title, color = Gl0rgText)
        Spacer(Modifier.height(12.dp))
        if (channels.isEmpty()) {
            Text(text = emptyText, color = Gl0rgMuted)
        } else {
            channels.take(8).forEach { channel ->
                TvButton(
                    label = "${channel.safeDisplayName}  /${channel.slug}${if (channel.stream != null) "  LIVE" else ""}",
                    onClick = { onOpenChannel(channel.slug) },
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(58.dp)
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}
