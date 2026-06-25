package tv.gl0rg.kick.kick

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

object KickJsonParsers {
    private val json = Json { ignoreUnknownKeys = true }

    fun parsePublicChannel(jsonText: String): KickChannel? {
        val root = runCatching { json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return null
        val slug = root.string("slug") ?: return null
        val user = root.objectAt("user")
        val livestream = root.objectAt("livestream")
        val playbackUrl = root.string("playback_url")

        val stream = livestream?.let {
            KickStream(
                slug = slug,
                title = it.string("session_title") ?: it.string("stream_title") ?: "Untitled stream",
                category = it.arrayObject("categories", 0)?.string("name"),
                thumbnailUrl = it.objectAt("thumbnail")?.string("url") ?: it.string("thumbnail"),
                viewerCount = it.viewerCount(),
                isMature = it.boolean("is_mature") ?: false,
                hlsUrl = playbackUrl
            )
        }

        return KickChannel(
            slug = slug,
            displayName = user?.string("username") ?: slug,
            avatarUrl = user?.string("profile_pic") ?: user?.string("profilePic") ?: user?.string("profilepic"),
            bannerUrl = root.objectAt("banner_image")?.string("url"),
            stream = stream
        )
    }

    fun parseSearchResults(jsonText: String): KickSearchResults {
        val root = runCatching { json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return KickSearchResults(emptyList(), emptyList())
        val channels = root.array("channels")
            .mapNotNull { it as? JsonObject }
            .mapNotNull { it.toSearchChannel() }
        return KickSearchResults(
            liveChannels = channels.filter { it.stream != null },
            channels = channels
        )
    }

    fun parseLiveStreams(jsonText: String): List<KickStream> {
        val root = runCatching { json.parseToJsonElement(jsonText).jsonObject }.getOrNull() ?: return emptyList()
        return root.array("data")
            .mapNotNull { it as? JsonObject }
            .mapNotNull { it.toLiveStream() }
    }

    fun parseChannelVideos(jsonText: String): List<KickVideo> {
        val element = runCatching { json.parseToJsonElement(jsonText) }.getOrNull() ?: return emptyList()
        val items = when (element) {
            is JsonArray -> element
            is JsonObject -> element.array("data")
            else -> emptyList()
        }
        return items
            .mapNotNull { it as? JsonObject }
            .mapNotNull { item ->
                val video = item.objectAt("video")
                val id = video?.long("id") ?: item.long("id") ?: return@mapNotNull null
                KickVideo(
                    id = id,
                    title = item.string("session_title") ?: item.string("title") ?: "Untitled video",
                    thumbnailUrl = item.objectAt("thumbnail")?.string("src") ?: item.objectAt("thumbnail")?.string("url") ?: item.string("thumbnail"),
                    views = item.int("views") ?: video?.int("views"),
                    durationMillis = item.long("duration"),
                    playbackUrl = item.string("source")
                )
            }
    }

    private fun JsonObject.toSearchChannel(): KickChannel? {
        val slug = string("slug") ?: return null
        val user = objectAt("user")
        val isLive = boolean("isLive") ?: boolean("is_live") ?: false
        val stream = if (isLive) {
            KickStream(
                slug = slug,
                title = "Live now",
                category = null,
                thumbnailUrl = null,
                viewerCount = null,
                isMature = false,
                hlsUrl = string("playback_url")
            )
        } else {
            null
        }
        return KickChannel(
            slug = slug,
            displayName = user?.string("username") ?: slug,
            avatarUrl = user?.string("profilePic") ?: user?.string("profile_pic") ?: user?.string("profilepic"),
            bannerUrl = objectAt("banner_image")?.string("url"),
            stream = stream
        )
    }

    private fun JsonObject.toLiveStream(): KickStream? {
        val channel = objectAt("channel")
        val slug = channel?.string("slug") ?: string("slug") ?: return null
        val livestream = objectAt("livestream")
        val category = arrayObject("categories", 0)?.string("name")
            ?: livestream?.arrayObject("categories", 0)?.string("name")
        return KickStream(
            slug = slug,
            title = string("session_title")
                ?: string("stream_title")
                ?: livestream?.string("session_title")
                ?: livestream?.string("stream_title")
                ?: "Untitled stream",
            category = category,
            thumbnailUrl = objectAt("thumbnail")?.string("src")
                ?: objectAt("thumbnail")?.string("url")
                ?: livestream?.objectAt("thumbnail")?.string("src")
                ?: livestream?.objectAt("thumbnail")?.string("url")
                ?: string("thumbnail")
                ?: livestream?.string("thumbnail"),
            viewerCount = viewerCount() ?: livestream?.viewerCount(),
            isMature = boolean("is_mature")
                ?: boolean("has_mature_content")
                ?: livestream?.boolean("is_mature")
                ?: false,
            hlsUrl = channel?.string("playback_url")
                ?: string("playback_url")
                ?: livestream?.string("playback_url")
                ?: string("source")
        )
    }

    private fun JsonObject.objectAt(key: String): JsonObject? =
        this[key] as? JsonObject

    private fun JsonObject.array(key: String): List<kotlinx.serialization.json.JsonElement> =
        runCatching { this[key]?.jsonArray?.toList() }.getOrNull().orEmpty()

    private fun JsonObject.arrayObject(key: String, index: Int): JsonObject? =
        runCatching { this[key]?.jsonArray?.get(index)?.jsonObject }.getOrNull()

    private fun JsonObject.primitive(key: String): JsonPrimitive? = this[key] as? JsonPrimitive

    private fun JsonObject.string(key: String): String? =
        primitive(key)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? =
        primitive(key)?.intOrNull ?: primitive(key)?.contentOrNull?.filter { it.isDigit() }?.toIntOrNull()

    private fun JsonObject.viewerCount(): Int? =
        int("viewer_count")
            ?: int("viewerCount")
            ?: int("viewers")
            ?: int("viewers_count")
            ?: int("viewer_count_live")
            ?: int("live_viewers")
            ?: int("current_viewers")

    private fun JsonObject.long(key: String): Long? =
        primitive(key)?.longOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        primitive(key)?.booleanOrNull
}
