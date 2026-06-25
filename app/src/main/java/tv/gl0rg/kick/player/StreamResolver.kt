package tv.gl0rg.kick.player

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tv.gl0rg.kick.kick.KickStream

sealed interface PlaybackRoute {
    data class Native(val hlsUrl: String, val isLive: Boolean = true) : PlaybackRoute
    data class WebViewFallback(val url: String) : PlaybackRoute
}

object StreamResolver {
    fun resolve(stream: KickStream): PlaybackRoute {
        val hlsUrl = stream.hlsUrl
        return if (hlsUrl != null && hlsUrl.toHttpUrlOrNull()?.encodedPath?.endsWith(".m3u8", ignoreCase = true) == true) {
            PlaybackRoute.Native(hlsUrl, isLive = true)
        } else {
            PlaybackRoute.WebViewFallback("https://kick.com/${stream.slug}")
        }
    }
}
