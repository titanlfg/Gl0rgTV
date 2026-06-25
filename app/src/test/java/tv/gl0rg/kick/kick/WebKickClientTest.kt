package tv.gl0rg.kick.kick

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebKickClientTest {
    @Test
    fun getChannelEncodesSlugAsPathSegment() = runTest {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("{}"))
            server.enqueue(MockResponse().setBody("{}"))
            server.start()
            val client = WebKickClient(OkHttpClient(), FakeKickSessionProvider(), server.url("/"))

            client.getChannel("name with/slash")

            assertEquals("/api/v2/channels/name%20with%2Fslash", server.takeRequest().path)
            assertEquals("/name%20with%2Fslash", server.takeRequest().path)
        }
    }

    @Test
    fun liveStreamsUsesJsonEndpoint() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "data": [
                        {
                          "session_title": "Live show",
                          "viewer_count": 42,
                          "thumbnail": {"src": "https://img.example/thumb.webp"},
                          "channel": {"slug": "gl0rg", "playback_url": "https://video.example/live.m3u8"},
                          "categories": [{"name": "Just Chatting"}]
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
            server.start()
            val client = WebKickClient(OkHttpClient(), FakeKickSessionProvider(), server.url("/"))

            val result = client.getLiveStreams()

            assertTrue(result is KickResult.Success)
            val streams = (result as KickResult.Success).value
            assertEquals("/stream/livestreams/en?page=1&limit=40", server.takeRequest().path)
            assertEquals("gl0rg", streams.single().slug)
            assertEquals("Live show", streams.single().title)
            assertEquals("https://video.example/live.m3u8", streams.single().hlsUrl)
        }
    }

    @Test
    fun categoryStreamsUseSubcategoryFilter() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "data": [
                        {
                          "session_title": "Category show",
                          "viewer_count": 7,
                          "channel": {"slug": "catstream", "playback_url": "https://video.example/cat.m3u8"},
                          "categories": [{"name": "Minecraft"}]
                        }
                      ]
                    }
                    """.trimIndent()
                )
            )
            server.start()
            val client = WebKickClient(OkHttpClient(), FakeKickSessionProvider(), server.url("/"))

            val result = client.getCategoryStreams("minecraft")

            assertTrue(result is KickResult.Success)
            assertEquals("/stream/livestreams/en?subcategory=minecraft&page=1&limit=40", server.takeRequest().path)
            assertEquals("catstream", (result as KickResult.Success).value.single().slug)
        }
    }
}
