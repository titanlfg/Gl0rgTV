package tv.gl0rg.kick.kick

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
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
                viewerCount = it.int("viewer_count"),
                isMature = it.boolean("is_mature") ?: false,
                hlsUrl = playbackUrl
            )
        }

        return KickChannel(
            slug = slug,
            displayName = user?.string("username") ?: slug,
            avatarUrl = user?.string("profile_pic"),
            bannerUrl = root.objectAt("banner_image")?.string("url"),
            stream = stream
        )
    }

    private fun JsonObject.objectAt(key: String): JsonObject? =
        this[key] as? JsonObject

    private fun JsonObject.arrayObject(key: String, index: Int): JsonObject? =
        runCatching { this[key]?.jsonArray?.get(index)?.jsonObject }.getOrNull()

    private fun JsonObject.primitive(key: String): JsonPrimitive? = this[key] as? JsonPrimitive

    private fun JsonObject.string(key: String): String? =
        primitive(key)?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String): Int? =
        primitive(key)?.intOrNull

    private fun JsonObject.boolean(key: String): Boolean? =
        primitive(key)?.booleanOrNull
}
