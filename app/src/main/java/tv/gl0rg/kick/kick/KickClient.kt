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
    suspend fun getCategoryStreams(categorySlug: String): KickResult<List<KickStream>>
    suspend fun getFollowedChannels(): KickResult<List<KickChannel>>
    suspend fun getChannelVideos(slug: String): KickResult<List<KickVideo>>
}

class WebKickClient(
    private val httpClient: OkHttpClient,
    private val sessionProvider: KickSessionProvider,
    private val baseUrl: HttpUrl = "https://kick.com".toHttpUrl()
) : KickClient {
    override suspend fun getChannel(slug: String): KickResult<KickChannel> = withContext(Dispatchers.IO) {
        val apiRequest = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("api/v2/channels").addPathSegment(slug).build())
            .kickHeaders(referer = "/$slug")
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
            .kickHeaders(referer = "/$slug")
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
            .url(baseUrl.newBuilder().addPathSegments("api/search").encodedQuery("searched_word=$encoded").build())
            .kickHeaders(referer = "/")
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                KickResult.Success(KickJsonParsers.parseSearchResults(response.body?.string().orEmpty()))
            }
        }.getOrElse { KickResult.Failure("search_request_failed", it) }
    }

    override suspend fun getLiveStreams(): KickResult<List<KickStream>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(
                baseUrl.newBuilder()
                    .addPathSegments("stream/livestreams/en")
                    .addQueryParameter("page", "1")
                    .addQueryParameter("limit", "40")
                    .addQueryParameter("sort", "desc")
                    .build()
            )
            .kickHeaders(referer = "/")
            .applySessionCookie()
            .build()

        runCatching {
            val body = httpClient.newCall(request).execute().use { response ->
                response.body?.string().orEmpty()
            }
            KickResult.Success(KickJsonParsers.parseLiveStreams(body).topViewed())
        }.getOrElse { KickResult.Failure("live_streams_request_failed", it) }
    }

    override suspend fun getCategoryStreams(categorySlug: String): KickResult<List<KickStream>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(
                baseUrl.newBuilder()
                    .addPathSegments("stream/livestreams/en")
                    .addQueryParameter("subcategory", categorySlug)
                    .addQueryParameter("page", "1")
                    .addQueryParameter("limit", "40")
                    .addQueryParameter("sort", "desc")
                    .build()
            )
            .kickHeaders(referer = "/browse/$categorySlug")
            .applySessionCookie()
            .build()

        runCatching {
            val body = httpClient.newCall(request).execute().use { response ->
                response.body?.string().orEmpty()
            }
            val parsed = KickJsonParsers.parseLiveStreams(body)
            val categoryMatches = parsed.filter { it.category.matchesCategorySlug(categorySlug) }
            KickResult.Success((categoryMatches.ifEmpty { parsed }).topViewed())
        }.getOrElse { KickResult.Failure("category_streams_request_failed", it) }
    }

    override suspend fun getFollowedChannels(): KickResult<List<KickChannel>> = withContext(Dispatchers.IO) {
        if (!sessionProvider.hasSession()) return@withContext KickResult.Failure("not_logged_in")

        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegment("following").build())
            .kickHeaders(referer = "/following")
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                val channels = KickHtmlParsers.parseChannelLinks(response.body?.string().orEmpty())
                if (channels.isEmpty()) KickResult.Failure("followed_channels_parse_failed") else KickResult.Success(channels)
            }
        }.getOrElse { KickResult.Failure("followed_channels_request_failed", it) }
    }

    override suspend fun getChannelVideos(slug: String): KickResult<List<KickVideo>> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("api/v2/channels").addPathSegment(slug).addPathSegment("videos").build())
            .kickHeaders(referer = "/$slug/videos")
            .applySessionCookie()
            .build()

        runCatching {
            httpClient.newCall(request).execute().use { response ->
                KickResult.Success(KickJsonParsers.parseChannelVideos(response.body?.string().orEmpty()))
            }
        }.getOrElse { KickResult.Failure("channel_videos_request_failed", it) }
    }

    private fun Request.Builder.kickHeaders(referer: String): Request.Builder {
        header("User-Agent", USER_AGENT)
        header("Accept", "application/json, text/plain, */*")
        header("Origin", baseUrl.toString().trimEnd('/'))
        header("Referer", baseUrl.newBuilder().encodedPath(referer).build().toString())
        header("Accept-Language", "en-US,en;q=0.9")
        return this
    }

    private fun Request.Builder.applySessionCookie(): Request.Builder {
        val cookieHeader = sessionProvider.cookieHeader()
        if (cookieHeader.isNotBlank()) {
            header("Cookie", cookieHeader)
        }
        return this
    }

    private companion object {
        const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        const val MAX_LIVE_STREAM_CANDIDATES = 24
    }
}

private fun List<KickStream>.topViewed(): List<KickStream> =
    sortedWith(
        compareByDescending<KickStream> { it.viewerCount ?: -1 }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.slug }
    ).take(24)

private fun String?.matchesCategorySlug(categorySlug: String): Boolean {
    val normalizedCategory = this?.toSlugLike() ?: return false
    return normalizedCategory == categorySlug.toSlugLike()
}

private fun String.toSlugLike(): String =
    lowercase()
        .replace("&", "and")
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
