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
    fun liveStreamsCapsResolvedChannelRequests() = runTest {
        MockWebServer().use { server ->
            server.enqueue(
                MockResponse().setBody(
                    (1..30).joinToString(prefix = "<html><body>", postfix = "</body></html>") {
                        """<a href="/channel$it">Channel $it</a>"""
                    }
                )
            )
            repeat(48) {
                server.enqueue(MockResponse().setBody("{}"))
            }
            server.start()
            val client = WebKickClient(OkHttpClient(), FakeKickSessionProvider(), server.url("/"))

            val result = client.getLiveStreams()

            assertTrue(result is KickResult.Success)
            assertEquals(49, server.requestCount)
        }
    }
}
