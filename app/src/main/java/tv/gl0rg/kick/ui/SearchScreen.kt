package tv.gl0rg.kick.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenChannel: (String) -> Unit,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    val query = remember { mutableStateOf("") }
    Column(modifier = modifier.padding(32.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query.value,
            onValueChange = { query.value = it },
            label = { Text("Channel or category") }
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onOpenChannel(query.value) },
            enabled = query.value.isNotBlank()
        ) {
            Text("Open Channel")
        }
        statusMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it)
        }
    }
}
