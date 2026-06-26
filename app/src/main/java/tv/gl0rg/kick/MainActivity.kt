package tv.gl0rg.kick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import tv.gl0rg.kick.ui.Gl0rgTheme
import tv.gl0rg.kick.ui.ThemePreferences

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var theme by remember { mutableStateOf(ThemePreferences.load(context)) }
            val colors = theme.colors
            Gl0rgTheme(option = theme) {
                MaterialTheme(
                    colorScheme = darkColorScheme(
                        background = colors.background,
                        surface = colors.surface,
                        primary = colors.accent,
                        onBackground = colors.text,
                        onSurface = colors.text,
                        onPrimary = colors.accentText
                    )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ) {
                        Gl0rgTvApp(
                            themeOption = theme,
                            onThemeChange = { selected ->
                                theme = selected
                                ThemePreferences.save(context, selected)
                            }
                        )
                    }
                }
            }
        }
    }
}
