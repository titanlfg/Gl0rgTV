package tv.gl0rg.kick.ui

import android.content.Context

/** Lightweight persistence for the selected [ThemeOption]. */
object ThemePreferences {
    private const val PREFS = "gl0rgtv_prefs"
    private const val KEY_THEME = "theme"

    fun load(context: Context): ThemeOption =
        ThemeOption.fromStorageKey(
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, null)
        )

    fun save(context: Context, option: ThemeOption) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, option.storageKey)
            .apply()
    }
}
