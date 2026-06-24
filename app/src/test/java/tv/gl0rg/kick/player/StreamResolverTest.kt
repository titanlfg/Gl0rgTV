package tv.gl0rg.kick.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tv.gl0rg.kick.kick.KickStream

class StreamResolverTest {
    @Test
    fun directHlsUsesNativePlayback() {
        val stream = KickStream(
            slug = "gl0rg",
            title = "Live",
            category = null,
            thumbnailUrl = null,
            viewerCount = 10,
            isMature = false,
            hlsUrl = "https://video.example/live.m3u8"
        )

        val result = StreamResolver.resolve(stream)

        assertEquals(PlaybackRoute.Native("https://video.example/live.m3u8"), result)
    }

    @Test
    fun signedHlsUsesNativePlayback() {
        val stream = KickStream(
            slug = "gl0rg",
            title = "Live",
            category = null,
            thumbnailUrl = null,
            viewerCount = 10,
            isMature = false,
            hlsUrl = "https://video.example/live.m3u8?token=abc"
        )

        val result = StreamResolver.resolve(stream)

        assertEquals(PlaybackRoute.Native("https://video.example/live.m3u8?token=abc"), result)
    }

    @Test
    fun missingHlsUsesWebViewFallback() {
        val stream = KickStream(
            slug = "gl0rg",
            title = "Live",
            category = null,
            thumbnailUrl = null,
            viewerCount = null,
            isMature = false,
            hlsUrl = null
        )

        val result = StreamResolver.resolve(stream)

        assertTrue(result is PlaybackRoute.WebViewFallback)
    }
}
