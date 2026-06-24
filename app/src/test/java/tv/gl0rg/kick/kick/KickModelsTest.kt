package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class KickModelsTest {
    @Test
    fun channelDisplayNameFallsBackToSlug() {
        val channel = KickChannel(
            slug = "test-streamer",
            displayName = "",
            avatarUrl = null,
            bannerUrl = null,
            stream = null
        )

        assertEquals("test-streamer", channel.safeDisplayName)
    }

    @Test
    fun kickFailureKeepsReason() {
        val failure = KickResult.Failure("parse_failed")

        assertEquals("parse_failed", failure.reason)
    }
}
