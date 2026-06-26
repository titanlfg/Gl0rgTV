package tv.gl0rg.kick.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme token set for Gl0rgTV. One source of truth for every surface and accent
 * color so the whole app can be re-skinned by swapping a [Gl0rgColors] instance.
 */
data class Gl0rgColors(
    val background: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val surfaceFocus: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val accentText: Color,
    val secondary: Color,
    val live: Color,
    val outline: Color
)

enum class ThemeOption(val storageKey: String, val displayName: String, val colors: Gl0rgColors) {
    Kick(
        storageKey = "kick",
        displayName = "Kick Green",
        colors = Gl0rgColors(
            background = Color(0xFF060807),
            surface = Color(0xFF101611),
            surfaceAlt = Color(0xFF151B16),
            surfaceFocus = Color(0xFF1C261C),
            text = Color(0xFFF3F7F1),
            muted = Color(0xFFAAB4A8),
            accent = Color(0xFF53FC18),
            accentText = Color(0xFF060807),
            secondary = Color(0xFFE90073),
            live = Color(0xFFE1003C),
            outline = Color(0xFF263127)
        )
    ),
    Midnight(
        storageKey = "midnight",
        displayName = "Midnight",
        colors = Gl0rgColors(
            background = Color(0xFF07090F),
            surface = Color(0xFF121626),
            surfaceAlt = Color(0xFF171C30),
            surfaceFocus = Color(0xFF1E2540),
            text = Color(0xFFEAEEF7),
            muted = Color(0xFF9AA3BD),
            accent = Color(0xFF4DA3FF),
            accentText = Color(0xFF07090F),
            secondary = Color(0xFF7C5CFF),
            live = Color(0xFFFF4D6D),
            outline = Color(0xFF26304A)
        )
    ),
    Mono(
        storageKey = "mono",
        displayName = "Mono",
        colors = Gl0rgColors(
            background = Color(0xFF0A0A0A),
            surface = Color(0xFF151515),
            surfaceAlt = Color(0xFF1C1C1C),
            surfaceFocus = Color(0xFF262626),
            text = Color(0xFFFAFAFA),
            muted = Color(0xFF9A9A9A),
            accent = Color(0xFFFFFFFF),
            accentText = Color(0xFF0A0A0A),
            secondary = Color(0xFF8A8A8A),
            live = Color(0xFFFF5252),
            outline = Color(0xFF2A2A2A)
        )
    );

    companion object {
        fun fromStorageKey(value: String?): ThemeOption =
            entries.firstOrNull { it.storageKey == value } ?: Kick
    }
}

val LocalGl0rgColors = staticCompositionLocalOf { ThemeOption.Kick.colors }

/** Convenient accessor: `Gl0rg.colors.accent`. */
object Gl0rg {
    val colors: Gl0rgColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGl0rgColors.current
}

@Composable
fun Gl0rgTheme(option: ThemeOption, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalGl0rgColors provides option.colors, content = content)
}
