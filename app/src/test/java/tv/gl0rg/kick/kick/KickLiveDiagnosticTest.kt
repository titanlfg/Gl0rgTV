package tv.gl0rg.kick.kick

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test

/**
 * Diagnostic (does not assert) that runs against LIVE kick.com in CI to find
 * where the stream pipeline breaks. Output is printed to stdout (KICK_DIAG / RAW
 * markers) and surfaced in the build log via testLogging.showStandardStreams.
 */
class KickLiveDiagnosticTest {
    private val client = OkHttpClient()
    private val kick = WebKickClient(client, FakeKickSessionProvider(""))

    @Test
    fun diagnose() = runBlocking {
        val out = StringBuilder("\n===== KICK_DIAG =====\n")

        // Raw API status — detects Cloudflare/403 blocking of server-side requests.
        for (slug in listOf("xqc", "trainwreckstv")) {
            runCatching {
                client.newCall(
                    Request.Builder()
                        .url("https://kick.com/api/v2/channels/$slug")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .build()
                ).execute().use { r ->
                    val body = r.body?.string().orEmpty()
                    out.appendLine("RAW api/v2/channels/$slug -> HTTP ${r.code}, len=${body.length}, head=${body.take(140).replace("\n", "\\n")}")
                }
            }.onFailure { out.appendLine("RAW api/v2/channels/$slug FAILED: $it") }
        }

        // Through the app client.
        for (slug in listOf("xqc", "trainwreckstv")) {
            val res = kick.getChannel(slug)
            out.appendLine("getChannel($slug) -> ${res::class.simpleName} ${(res as? KickResult.Failure)?.reason ?: ""}")
            if (res is KickResult.Success) {
                val hls = res.value.stream?.hlsUrl
                out.appendLine("  live=${res.value.stream != null} hlsUrl=$hls")
                if (hls != null) {
                    runCatching {
                        client.newCall(
                            Request.Builder().url(hls)
                                .header("User-Agent", "Mozilla/5.0 (Android TV; Gl0rgTV)")
                                .header("Origin", "https://kick.com")
                                .header("Referer", "https://kick.com/")
                                .build()
                        ).execute().use { r ->
                            val head = r.body?.string()?.take(80)?.replace("\n", "\\n")
                            out.appendLine("  HLS GET -> HTTP ${r.code}, head=$head")
                        }
                    }.onFailure { out.appendLine("  HLS GET FAILED: $it") }
                }
            }
        }

        var sampleHls: String? = null
        var liveSlug: String? = null
        when (val live = kick.getLiveStreams()) {
            is KickResult.Success -> {
                val first = live.value.firstOrNull { it.hlsUrl != null }
                sampleHls = first?.hlsUrl
                liveSlug = first?.slug
                out.appendLine("getLiveStreams -> n=${live.value.size}, withHls=${live.value.count { it.hlsUrl != null }}, liveSlug=$liveSlug, sampleHls=$sampleHls")
            }
            is KickResult.Failure -> out.appendLine("getLiveStreams -> FAILURE ${live.reason}")
        }

        // For a channel that IS live: print the FULL playback_url from the channel
        // API (does it carry a ?token= that the list API omits?) and probe it.
        liveSlug?.let { slug ->
            runCatching {
                client.newCall(
                    Request.Builder()
                        .url("https://kick.com/api/v2/channels/$slug")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .build()
                ).execute().use { r ->
                    val body = r.body?.string().orEmpty()
                    val playback = Regex("\"playback_url\"\\s*:\\s*\"([^\"]+)\"").find(body)
                        ?.groupValues?.get(1)?.replace("\\/", "/")
                    out.appendLine("LIVE channel api/$slug -> HTTP ${r.code}, playback_url=$playback")
                    if (playback != null) {
                        runCatching {
                            client.newCall(
                                Request.Builder().url(playback)
                                    .header("User-Agent", "Mozilla/5.0 (Android TV; Gl0rgTV)")
                                    .build()
                            ).execute().use { pr ->
                                val head = pr.body?.string()?.take(80)?.replace("\n", "\\n")
                                out.appendLine("LIVE channel playback GET -> HTTP ${pr.code}, head=$head")
                            }
                        }.onFailure { out.appendLine("LIVE channel playback GET FAILED: $it") }
                    }
                }
            }.onFailure { out.appendLine("LIVE channel api/$slug FAILED: $it") }
        }

        // Fetch a real live HLS master playlist two ways to see if the kick.com
        // Origin/Referer headers (what the player sends) get rejected by the IVS CDN.
        sampleHls?.let { hls ->
            fun probe(label: String, withKickHeaders: Boolean) {
                runCatching {
                    val b = Request.Builder().url(hls)
                        .header("User-Agent", "Mozilla/5.0 (Android TV; Gl0rgTV)")
                    if (withKickHeaders) {
                        b.header("Origin", "https://kick.com").header("Referer", "https://kick.com/")
                    }
                    client.newCall(b.build()).execute().use { r ->
                        val head = r.body?.string()?.take(60)?.replace("\n", "\\n")
                        out.appendLine("HLS[$label] -> HTTP ${r.code}, head=$head")
                    }
                }.onFailure { out.appendLine("HLS[$label] FAILED: $it") }
            }
            probe("plain", withKickHeaders = false)
            probe("kickHeaders", withKickHeaders = true)
        }

        out.appendLine("===== /KICK_DIAG =====")
        println(out)
    }
}
