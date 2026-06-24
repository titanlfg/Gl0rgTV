package tv.gl0rg.kick.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.file.Files

class SettingsRepositoryTest {
    @Test
    fun defaultsPreferNativePlayerAutoQualityAndMatureGate() = runTest {
        val repo = InMemorySettingsRepository()

        val settings = repo.load()

        assertEquals(PlayerMode.NativeFirst, settings.playerMode)
        assertEquals(StreamQuality.Auto, settings.quality)
        assertEquals(true, settings.autoplay)
        assertEquals(true, settings.showMatureGate)
        assertEquals("home", settings.startScreen)
    }

    @Test
    fun savesPlayerMode() = runTest {
        val repo = InMemorySettingsRepository()

        repo.save(repo.load().copy(playerMode = PlayerMode.WebViewOnly))

        assertEquals(PlayerMode.WebViewOnly, repo.load().playerMode)
    }

    @Test
    fun dataStoreRepositoryRoundTripsAllFields() = runTest {
        val repo = DataStoreSettingsRepository(testDataStore())
        val saved = AppSettings(
            playerMode = PlayerMode.WebViewOnly,
            quality = StreamQuality.High,
            autoplay = false,
            showMatureGate = false,
            startScreen = "favorites"
        )

        repo.save(saved)

        assertEquals(saved, repo.load())
    }

    @Test
    fun dataStoreRepositoryFallsBackForUnknownEnumKeys() = runTest {
        val dataStore = testDataStore()
        val repo = DataStoreSettingsRepository(dataStore)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("player_mode")] = "renamed_mode"
            preferences[stringPreferencesKey("quality")] = "renamed_quality"
            preferences[booleanPreferencesKey("autoplay")] = false
        }

        val settings = repo.load()

        assertEquals(PlayerMode.NativeFirst, settings.playerMode)
        assertEquals(StreamQuality.Auto, settings.quality)
        assertEquals(false, settings.autoplay)
    }

    private fun testDataStore() = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        produceFile = {
            Files.createTempDirectory("gl0rgtv-settings")
                .resolve("settings.preferences_pb")
                .toFile()
        }
    )
}
