package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamEnrichmentTest {
    private val bare = KickStream(
        slug = "absi",
        title = "list title",
        category = null,
        thumbnailUrl = null,
        viewerCount = 10,
        isMature = false,
        hlsUrl = "https://cdn.example/channel.m3u8"
    )
    private val tokened = KickStream(
        slug = "absi",
        title = "live title",
        category = "IRL",
        thumbnailUrl = "thumb",
        viewerCount = 12,
        isMature = false,
        hlsUrl = "https://cdn.example/channel.m3u8?token=signed"
    )

    @Test
    fun tokenedChannelUrlReplacesBareListUrl() {
        val merged = StreamEnrichment.merge(bare, tokened)
        assertEquals("https://cdn.example/channel.m3u8?token=signed", merged.hlsUrl)
        assertEquals("live title", merged.title)
        assertEquals("IRL", merged.category)
    }

    @Test
    fun keepsBaseWhenChannelHasNoLiveData() {
        assertEquals(bare, StreamEnrichment.merge(bare, null))
    }

    @Test
    fun keepsBaseHlsWhenLiveLacksUrl() {
        val merged = StreamEnrichment.merge(bare, tokened.copy(hlsUrl = null))
        assertEquals("https://cdn.example/channel.m3u8", merged.hlsUrl)
    }
}
