package tv.gl0rg.kick.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.gl0rg.kick.kick.KickChannel
import tv.gl0rg.kick.kick.KickStream

class HomeFeedTest {
    private fun channel(slug: String, live: Boolean) = KickChannel(
        slug = slug,
        displayName = slug,
        avatarUrl = null,
        bannerUrl = null,
        stream = if (live) {
            KickStream(slug, "title", "Just Chatting", null, 1, false, null)
        } else {
            null
        }
    )

    @Test
    fun liveFollowed_returnsOnlyLiveFavorites_neverRandom() {
        val favorites = listOf(channel("alice", true), channel("bob", false), channel("carol", true))
        val result = HomeFeed.liveFollowed(favorites)
        assertEquals(listOf("alice", "carol"), result.map { it.slug })
    }

    @Test
    fun liveFollowed_emptyWhenNoneLive() {
        assertTrue(HomeFeed.liveFollowed(listOf(channel("bob", false))).isEmpty())
    }

    @Test
    fun liveFollowed_emptyWhenNoFavorites() {
        assertTrue(HomeFeed.liveFollowed(emptyList()).isEmpty())
    }
}
