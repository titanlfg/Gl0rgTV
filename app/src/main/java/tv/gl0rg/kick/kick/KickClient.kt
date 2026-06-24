package tv.gl0rg.kick.kick

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface KickClient {
    suspend fun getChannel(slug: String): KickResult<KickChannel>
    suspend fun searchChannels(query: String): KickResult<KickSearchResults>
    suspend fun getLiveStreams(): KickResult<List<KickStream>>
    suspend fun getFollowedChannels(): KickResult<List<KickChannel>>
}

class WebKickClient(
    private val httpClient: OkHttpClient,
    private val sessionProvider: KickSessionProvider,
    private val baseUrl: HttpUrl = "https://kick.com".toHttpUrl()
) : KickClient {
    override suspend fun getChannel(slug: String): KickResult<KickChannel> = withContext(Dispatchers.IO) {
        val apiRequest = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("api/v2/channels").addPathSegment(slug).build())
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        val apiResult = runCatching {
            httpClient.newCall(apiRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                KickJsonParsers.parsePublicChannel(body)
                    ?.let { KickResult.Success(it) }
                    ?: KickResult.Failure("channel_json_parse_failed")
            }
        }.getOrElse { KickResult.Failure("channel_json_request_failed", it) }

        if (apiResult is KickResult.Success) return@withContext apiResult

        val pageRequest = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegment(slug).build())
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(pageRequest).execute().use { response ->
                val body = response.body?.string().orEmpty()
                KickHtmlParsers.parseChannel(body)
                    ?.let { KickResult.Success(it) }
                    ?: KickResult.Failure("channel_html_parse_failed")
            }
        }.getOrElse { KickResult.Failure("channel_html_request_failed", it) }
    }

    override suspend fun searchChannels(query: String): KickResult<KickSearchResults> = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegment("search").encodedQuery("query=$encoded").build())
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val channels = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty())
                KickResult.Success(KickSearchResults(liveChannels = channels.filter { it.stream != null }, channels = channels))
            }
        }.getOrElse { KickResult.Failure("search_request_failed", it) }
    }

    override suspend fun getLiveStreams(): KickResult<List<KickStream>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl)
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            val body = httpClient.newCall(request).execute().use { response ->
                response.body?.string().orEmpty()
            }
            val streams = KickHtmlParsers.parseChannelLinks(body)
                .take(MAX_LIVE_STREAM_CANDIDATES)
                .mapNotNull { channel ->
                    when (val result = getChannel(channel.slug)) {
                        is KickResult.Success -> result.value.stream
                        is KickResult.Failure -> null
                    }
                }
            KickResult.Success(streams)
        }.getOrElse { KickResult.Failure("live_streams_request_failed", it) }
    }

    override suspend fun getFollowedChannels(): KickResult<List<KickChannel>> = withContext(Dispatchers.IO) {
        if (!sessionProvider.hasSession()) return@withContext KickResult.Failure("not_logged_in")

        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegment("following").build())
            .header("User-Agent", USER_AGENT)
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val channels = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty())
                if (channels.isEmpty()) KickResult.Failure("followed_channels_parse_failed") else KickResult.Success(channels)
            }
        }.getOrElse { KickResult.Failure("followed_channels_request_failed", it) }
    }

    private fun Request.Builder.applySessionCookie(): Request.Builder {
        val cookieHeader = sessionProvider.cookieHeader()
        if (cookieHeader.isNotBlank()) {
            header("Cookie", cookieHeader)
        }
        return this
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Android TV; Gl0rgTV) AppleWebKit/537.36"
        const val MAX_LIVE_STREAM_CANDIDATES = 24
    }
}
