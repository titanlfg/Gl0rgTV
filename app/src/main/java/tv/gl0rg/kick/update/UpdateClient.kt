package tv.gl0rg.kick.update

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class AppUpdate(
    val latestVersion: String,
    val apkUrl: String,
    val releaseUrl: String
)

sealed interface UpdateCheckResult {
    data class Available(val update: AppUpdate) : UpdateCheckResult
    data class Current(val latestVersion: String) : UpdateCheckResult
    data class Failed(val reason: String, val cause: Throwable? = null) : UpdateCheckResult
}

class GitHubUpdateClient(
    private val httpClient: OkHttpClient,
    private val owner: String = "titanlfg",
    private val repo: String = "Gl0rgTV"
) {
    suspend fun check(currentVersion: String): UpdateCheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/releases/latest")
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Gl0rgTV-Updater")
                .build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext UpdateCheckResult.Failed("release_check_http_${response.code}")
                val root = Json.parseToJsonElement(response.body?.string().orEmpty()).jsonObject
                val latestVersion = root["tag_name"]?.jsonPrimitive?.content?.trimStart('v')
                    ?: return@withContext UpdateCheckResult.Failed("release_tag_missing")
                if (!UpdateVersions.isNewer(latestVersion, currentVersion)) {
                    return@withContext UpdateCheckResult.Current(latestVersion)
                }
                val apkUrl = root["assets"]?.jsonArray
                    ?.firstNotNullOfOrNull { asset ->
                        val assetObject = asset.jsonObject
                        val name = assetObject["name"]?.jsonPrimitive?.content.orEmpty()
                        if (name == "Gl0rgTV.apk") assetObject["browser_download_url"]?.jsonPrimitive?.content else null
                    }
                    ?: return@withContext UpdateCheckResult.Failed("apk_asset_missing")
                val releaseUrl = root["html_url"]?.jsonPrimitive?.content.orEmpty()
                UpdateCheckResult.Available(AppUpdate(latestVersion, apkUrl, releaseUrl))
            }
        }.getOrElse { UpdateCheckResult.Failed("release_check_failed", it) }
    }

    suspend fun download(context: Context, update: AppUpdate): File = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(update.apkUrl)
            .header("User-Agent", "Gl0rgTV-Updater")
            .build()
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val output = File(updatesDir, "Gl0rgTV-${update.latestVersion}.apk")
        httpClient.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "download_http_${response.code}" }
            response.body?.byteStream().use { input ->
                requireNotNull(input) { "download_body_missing" }
                output.outputStream().use { outputStream -> input.copyTo(outputStream) }
            }
        }
        output
    }
}

object UpdateVersions {
    fun isNewer(latest: String, current: String): Boolean {
        val latestParts = latest.toVersionParts()
        val currentParts = current.toVersionParts()
        val maxLength = maxOf(latestParts.size, currentParts.size)
        repeat(maxLength) { index ->
            val latestPart = latestParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    private fun String.toVersionParts(): List<Int> =
        trimStart('v')
            .split(".", "-", "_")
            .mapNotNull { it.toIntOrNull() }
}
