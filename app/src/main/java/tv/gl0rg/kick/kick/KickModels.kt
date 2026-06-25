package tv.gl0rg.kick.kick

data class KickStream(
    val slug: String,
    val title: String,
    val category: String?,
    val thumbnailUrl: String?,
    val viewerCount: Int?,
    val isMature: Boolean,
    val hlsUrl: String?
)

data class KickChannel(
    val slug: String,
    val displayName: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val stream: KickStream?
) {
    val safeDisplayName: String
        get() = displayName.ifBlank { slug }
}

data class KickVideo(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val views: Int?,
    val durationMillis: Long?,
    val playbackUrl: String?
)

data class KickSearchResults(
    val liveChannels: List<KickChannel>,
    val channels: List<KickChannel>
)
