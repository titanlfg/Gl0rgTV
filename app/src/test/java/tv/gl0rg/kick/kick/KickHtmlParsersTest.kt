package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KickHtmlParsersTest {
    private fun channelHtml(playbackField: String = """"playback_url": "https://video.example/live.m3u8",""") = """
        <html>
          <head>
            <script id="__NEXT_DATA__" type="application/json">
              {
                "props": {
                  "pageProps": {
                    "channel": {
                      "slug": "gl0rg",
                      "user": {"username": "Gl0rg"},
                      "profile_picture": "https://img.example/avatar.png",
                      "banner_picture": "https://img.example/banner.png",
                      "stream": {
                        "is_live": true,
                        "stream_title": "Green stream",
                        "viewer_count": 321,
                        "is_mature": true,
                        "thumbnail": "https://img.example/thumb.png",
                        $playbackField
                        "category": {"name": "Just Chatting"}
                      }
                    }
                  }
                }
              }
            </script>
          </head>
        </html>
    """.trimIndent()

    @Test
    fun parsesChannelFromNextData() {
        val channel = KickHtmlParsers.parseChannel(channelHtml())

        requireNotNull(channel)
        assertEquals("gl0rg", channel.slug)
        assertEquals("Gl0rg", channel.safeDisplayName)
        assertEquals("Green stream", channel.stream?.title)
        assertEquals(321, channel.stream?.viewerCount)
        assertEquals(true, channel.stream?.isMature)
        assertEquals("Just Chatting", channel.stream?.category)
        assertEquals("https://video.example/live.m3u8", channel.stream?.hlsUrl)
    }

    @Test
    fun invalidHtmlReturnsNull() {
        assertNull(KickHtmlParsers.parseChannel("<html></html>"))
    }

    @Test
    fun sourceFieldFallsBackAsHlsUrl() {
        val channel = KickHtmlParsers.parseChannel(
            channelHtml(""""source": "https://video.example/source.m3u8",""")
        )

        requireNotNull(channel)
        assertEquals("https://video.example/source.m3u8", channel.stream?.hlsUrl)
    }

    @Test
    fun parsesChannelLinksFromPage() {
        val page = """
            <html>
              <body>
                <a href="/gl0rg">Gl0rg</a>
                <a href="/xqc">xQc</a>
                <a href="/terms-of-service">Terms</a>
              </body>
            </html>
        """.trimIndent()

        val channels = KickHtmlParsers.parseChannelLinks(page)

        assertEquals(listOf("gl0rg", "xqc"), channels.map { it.slug })
    }
}
