package tv.gl0rg.kick.library

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryRepositoryTest {
    @Test
    fun favoriteToggleAddsAndRemovesSlug() = runTest {
        val repo = InMemoryLibraryRepository()

        repo.setFavorite("gl0rg", "Gl0rg", "https://img.example/a.png", true)
        assertEquals(listOf("gl0rg"), repo.favoriteSlugs())

        repo.setFavorite("gl0rg", "Gl0rg", "https://img.example/a.png", false)
        assertTrue(repo.favoriteSlugs().isEmpty())
    }

    @Test
    fun recentChannelsKeepNewestFirst() = runTest {
        val repo = InMemoryLibraryRepository()

        repo.markWatched("first", "First", null, watchedAtMillis = 1L)
        repo.markWatched("second", "Second", null, watchedAtMillis = 2L)

        assertEquals(listOf("second", "first"), repo.recentSlugs())
    }
}
