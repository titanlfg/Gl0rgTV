package tv.gl0rg.kick.kick

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup

object KickHtmlParsers {
    private val json = Json { ignoreUnknownKeys = true }
    private val blockedChannelSlugs = setOf(
        "about",
        "terms-of-service",
        "privacy-policy",
        "community-guidelines",
        "jobs",
        "support",
        "search",
        "following",
        "categories"
    )
    private val channelSlugPattern = Regex("[A-Za-z0-9_][A-Za-z0-9_-]*")

    fun parseChannel(html: String): KickChannel? {
        val nextData = Jsoup.parse(html)
            .selectFirst("script#__NEXT_DATA__")
            ?.data()
            ?.trim()
            .orEmpty()

        if (nextData.isBlank()) return null

        val root = runCatching { json.parseToJsonElement(nextData).jsonObject }.getOrNull() ?: return null
        val channel = root.objectAt("props")
            ?.objectAt("pageProps")
            ?.objectAt("channel")
            ?: return null

        val slug = channel.string("slug") ?: return null
        val displayName = channel.objectAt("user")?.string("username")
            ?: channel.string("username")
            ?: slug

        val streamObj = channel.objectAt("stream")
        val stream = streamObj?.let {
            val isLive = it.boolean("is_live") ?: false
            if (!isLive) {
                null
            } else {
                KickStream(
                    slug = slug,
                    title = it.string("stream_title") ?: it.string("title") ?: "Untitled stream",
                    category = it.objectAt("category")?.string("name"),
                    thumbnailUrl = it.string("thumbnail"),
                    viewerCount = it.int("viewer_count"),
                    isMature = it.boolean("is_mature") ?: it.boolean("has_mature_content") ?: false,
                    hlsUrl = it.string("playback_url") ?: it.string("source")
                )
            }
        }

        return KickChannel(
            slug = slug,
            displayName = displayName,
            avatarUrl = channel.string("profile_picture"),
            bannerUrl = channel.string("banner_picture"),
            stream = stream
        )
    }

    fun parseChannelLinks(html: String): List<KickChannel> {
        return Jsoup.parse(html)
            .select("a[href^=/]")
            .mapNotNull { element ->
                val slug = element.attr("href").trim('/').substringBefore("?").substringBefore("/")
                val label = element.text().trim()
                when {
                    slug.isBlank() -> null
                    slug in blockedChannelSlugs -> null
                    !slug.matches(channelSlugPattern) -> null
                    else -> KickChannel(
                        slug = slug,
                        displayName = label.ifBlank { slug },
                        avatarUrl = null,
                        bannerUrl = null,
                        stream = null
                    )
                }
            }
            .distinctBy { it.slug }
    }

    private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

    private fun JsonObject.objectAt(key: String): JsonObject? = this[key]?.jsonObjectOrNull()

    private fun JsonObject.primitive(key: String): JsonPrimitive? = this[key] as? JsonPrimitive

    private fun JsonObject.string(key: String): String? =
        primitive(key)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? =
        primitive(key)?.intOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        primitive(key)?.booleanOrNull
}
