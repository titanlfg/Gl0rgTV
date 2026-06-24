package tv.gl0rg.kick.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class PlayerMode(val storageKey: String) {
    NativeFirst("native_first"),
    WebViewOnly("webview_only"),
    Auto("auto");

    companion object {
        fun fromStorageKey(value: String?): PlayerMode =
            entries.firstOrNull { it.storageKey == value } ?: NativeFirst
    }
}

enum class StreamQuality(val storageKey: String) {
    Auto("auto"),
    Source("source"),
    High("high"),
    Medium("medium"),
    Low("low");

    companion object {
        fun fromStorageKey(value: String?): StreamQuality =
            entries.firstOrNull { it.storageKey == value } ?: Auto
    }
}

data class AppSettings(
    val playerMode: PlayerMode = PlayerMode.NativeFirst,
    val quality: StreamQuality = StreamQuality.Auto,
    val autoplay: Boolean = true,
    val showMatureGate: Boolean = true,
    val startScreen: String = "home"
)

interface SettingsRepository {
    suspend fun load(): AppSettings
    suspend fun save(settings: AppSettings)
}

class InMemorySettingsRepository : SettingsRepository {
    private var settings = AppSettings()

    override suspend fun load(): AppSettings = settings

    override suspend fun save(settings: AppSettings) {
        this.settings = settings
    }
}

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override suspend fun load(): AppSettings =
        dataStore.data.map { preferences ->
            AppSettings(
                playerMode = PlayerMode.fromStorageKey(preferences[PLAYER_MODE]),
                quality = StreamQuality.fromStorageKey(preferences[QUALITY]),
                autoplay = preferences[AUTOPLAY] ?: true,
                showMatureGate = preferences[MATURE_GATE] ?: true,
                startScreen = preferences[START_SCREEN] ?: "home"
            )
        }.first()

    override suspend fun save(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PLAYER_MODE] = settings.playerMode.storageKey
            preferences[QUALITY] = settings.quality.storageKey
            preferences[AUTOPLAY] = settings.autoplay
            preferences[MATURE_GATE] = settings.showMatureGate
            preferences[START_SCREEN] = settings.startScreen
        }
    }

    private companion object {
        val PLAYER_MODE = stringPreferencesKey("player_mode")
        val QUALITY = stringPreferencesKey("quality")
        val AUTOPLAY = booleanPreferencesKey("autoplay")
        val MATURE_GATE = booleanPreferencesKey("mature_gate")
        val START_SCREEN = stringPreferencesKey("start_screen")
    }
}
