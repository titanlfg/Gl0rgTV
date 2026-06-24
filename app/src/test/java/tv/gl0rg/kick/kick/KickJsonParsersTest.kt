package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class KickJsonParsersTest {
    @Test
    fun parsesPublicChannelApiJson() {
        val json = """
            {
              "slug": "xqc",
              "playback_url": "https://video.example/live.m3u8",
              "banner_image": {"url": "https://img.example/banner.webp"},
              "livestream": {
                "session_title": "Ranked games",
                "viewer_count": 1234,
                "thumbnail": {"url": "https://img.example/thumb.webp"},
                "is_mature": false,
                "categories": [{"name": "Just Chatting"}]
              },
              "user": {
                "username": "xQc",
                "profile_pic": "https://img.example/avatar.webp"
              }
            }
        """.trimIndent()

        val channel = KickJsonParsers.parsePublicChannel(json)

        requireNotNull(channel)
        assertEquals("xqc", channel.slug)
        assertEquals("xQc", channel.safeDisplayName)
        assertEquals("Ranked games", channel.stream?.title)
        assertEquals("https://video.example/live.m3u8", channel.stream?.hlsUrl)
        assertEquals(1234, channel.stream?.viewerCount)
        assertEquals("Just Chatting", channel.stream?.category)
    }
}
