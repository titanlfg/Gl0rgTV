package tv.gl0rg.kick.kick

/**
 * Merges a stream from the livestreams list with the same channel's live data
 * from the channel API. Kick's list API returns bare IVS playback URLs that the
 * CDN rejects with 403; the channel API returns a signed (`?token=`) URL that
 * plays. The channel API's hlsUrl must therefore always win.
 */
object StreamEnrichment {
    fun merge(base: KickStream, live: KickStream?): KickStream =
        if (live == null) {
            base
        } else {
            base.copy(
                title = live.title.ifBlank { base.title },
                category = live.category ?: base.category,
                thumbnailUrl = live.thumbnailUrl ?: base.thumbnailUrl,
                viewerCount = live.viewerCount ?: base.viewerCount,
                hlsUrl = live.hlsUrl ?: base.hlsUrl
            )
        }
}
