package tv.gl0rg.kick.kick

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/** Coarse state of the phone-assisted login, surfaced on the TV screen. */
enum class LoginPhase { Waiting, InvalidCookie, Linked }

class LocalCookieLoginServer(
    private val sessionProvider: KickSessionProvider
) {
    private var serverSocket: ServerSocket? = null
    private var job: Job? = null

    val url: String?
        get() = serverSocket?.localPort?.let { port -> "http://${bestLocalAddress()}:$port" }

    fun start(
        scope: CoroutineScope,
        onLogin: () -> Unit,
        onError: (String) -> Unit,
        onPhase: (LoginPhase) -> Unit = {}
    ) {
        if (serverSocket != null) return
        runCatching {
            serverSocket = ServerSocket(0)
            onPhase(LoginPhase.Waiting)
            job = scope.launch(Dispatchers.IO) {
                while (serverSocket?.isClosed == false) {
                    runCatching {
                        val socket = serverSocket?.accept() ?: return@launch
                        socket.use {
                            val reader = BufferedReader(InputStreamReader(it.getInputStream()))
                            val requestLine = reader.readLine().orEmpty()
                            val headers = mutableMapOf<String, String>()
                            var line = reader.readLine()
                            while (!line.isNullOrBlank()) {
                                val key = line.substringBefore(":").lowercase(Locale.US)
                                val value = line.substringAfter(":", "").trim()
                                headers[key] = value
                                line = reader.readLine()
                            }
                            val contentLength = headers["content-length"]?.toIntOrNull() ?: 0
                            val body = if (contentLength > 0) CharArray(contentLength).also { reader.read(it) }.concatToString() else ""
                            val response = if (requestLine.startsWith("POST")) {
                                val cookie = normalizeCookieHeader(formValue(body, "cookie"))
                                if (cookie.isBlank()) {
                                    onPhase(LoginPhase.InvalidCookie)
                                    httpResponse(resultHtml("Missing cookie", "Paste the Kick Cookie header, then submit again.", ok = false))
                                } else {
                                    sessionProvider.importCookieHeader(cookie)
                                    if (sessionProvider.hasSession()) {
                                        onPhase(LoginPhase.Linked)
                                        onLogin()
                                        httpResponse(resultHtml("Gl0rgTV linked", "You're all set. Return to your Android TV.", ok = true))
                                    } else {
                                        onPhase(LoginPhase.InvalidCookie)
                                        httpResponse(resultHtml("No session found", "That value had no Kick session cookie. Copy the full Cookie header and try again.", ok = false))
                                    }
                                }
                            } else if (requestLine.startsWith("GET /kick")) {
                                redirect("https://kick.com/")
                            } else {
                                httpResponse(formHtml())
                            }
                            it.getOutputStream().write(response.toByteArray(StandardCharsets.UTF_8))
                        }
                    }.onFailure { onError(it.message ?: "local_login_server_error") }
                }
            }
        }.onFailure { onError(it.message ?: "local_login_server_start_failed") }
    }

    fun stop() {
        job?.cancel()
        job = null
        serverSocket?.close()
        serverSocket = null
    }

    private fun formHtml(): String = """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Gl0rgTV Login</title>
          <style>
            :root{--bg:#060807;--card:#101611;--line:#263127;--green:#53FC18;--text:#F3F7F1;--muted:#AAB4A8}
            *{box-sizing:border-box}
            body{margin:0;font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;background:var(--bg);color:var(--text);line-height:1.5}
            .wrap{max-width:560px;margin:0 auto;padding:24px 18px 64px}
            .brand{display:flex;align-items:center;gap:10px;font-weight:900;font-size:26px;color:var(--green);margin:6px 0 22px}
            .brand .bar{width:34px;height:5px;background:var(--green);border-radius:3px}
            h1{font-size:22px;margin:0 0 6px}
            p.sub{color:var(--muted);margin:0 0 22px}
            .step{background:var(--card);border:1px solid var(--line);border-radius:14px;padding:16px 18px;margin:0 0 14px}
            .step .n{display:inline-flex;align-items:center;justify-content:center;width:26px;height:26px;border-radius:50%;background:var(--green);color:#060807;font-weight:900;margin-right:10px}
            .step h2{display:inline;font-size:17px}
            .step .body{color:var(--muted);font-size:14px;margin:10px 0 0}
            a.cta{display:block;text-align:center;background:var(--green);color:#060807;font-weight:900;font-size:20px;text-decoration:none;padding:16px;border-radius:12px;margin:18px 0}
            textarea{width:100%;min-height:120px;background:#0c120d;color:var(--text);border:1px solid var(--line);border-radius:10px;padding:12px;font-size:15px;font-family:ui-monospace,Menlo,Consolas,monospace}
            button{width:100%;background:var(--green);color:#060807;font-weight:900;font-size:18px;border:0;border-radius:12px;padding:16px;margin-top:14px;cursor:pointer}
            code{background:#0c120d;border:1px solid var(--line);border-radius:6px;padding:1px 6px;font-size:13px}
            .hint{color:var(--muted);font-size:13px;margin-top:10px}
          </style>
        </head>
        <body>
          <div class="wrap">
            <div class="brand"><span>Gl0rgTV</span><span class="bar"></span></div>
            <h1>Link your Kick account</h1>
            <p class="sub">Do this on the phone or computer showing this page. Read all steps first.</p>

            <div class="step"><span class="n">1</span><h2>Log in to Kick</h2>
              <div class="body">Tap the button below to open the real Kick site and sign in as usual.</div>
            </div>
            <a class="cta" href="https://kick.com/">Open Kick &amp; sign in &rarr;</a>

            <div class="step"><span class="n">2</span><h2>Copy your Cookie header</h2>
              <div class="body">
                Computer (easiest): press <code>F12</code> &rarr; <b>Network</b> &rarr; refresh kick.com &rarr;
                click any kick.com request &rarr; <b>Request Headers</b> &rarr; copy the full <code>Cookie</code> value.
              </div>
            </div>

            <div class="step"><span class="n">3</span><h2>Paste &amp; submit</h2>
              <div class="body">Paste the whole Cookie value below and submit. On success your TV finishes automatically.</div>
              <form method="post">
                <textarea name="cookie" placeholder="kick_session=...; session=...; laravel_session=..."></textarea>
                <button type="submit">Link Gl0rgTV</button>
              </form>
              <p class="hint">Your cookie stays on your local network — it goes straight to your TV, never to a server.</p>
            </div>
          </div>
        </body>
        </html>
    """.trimIndent()

    private fun resultHtml(title: String, body: String, ok: Boolean): String {
        val accent = if (ok) "#53FC18" else "#E1003C"
        return """
        <!doctype html>
        <html lang="en"><head><meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1"><title>$title</title>
        <style>
          body{margin:0;font-family:-apple-system,Segoe UI,Roboto,Arial,sans-serif;background:#060807;color:#F3F7F1;
               display:flex;min-height:100vh;align-items:center;justify-content:center;text-align:center}
          .card{max-width:420px;padding:32px 24px}
          .badge{width:72px;height:72px;border-radius:50%;background:$accent;color:#060807;font-size:40px;font-weight:900;
                 display:flex;align-items:center;justify-content:center;margin:0 auto 20px}
          h1{font-size:24px;margin:0 0 10px;color:$accent}
          p{color:#AAB4A8;font-size:15px}
        </style></head>
        <body><div class="card">
          <div class="badge">${if (ok) "&#10003;" else "!"}</div>
          <h1>$title</h1><p>$body</p><p>You can close this tab.</p>
        </div></body></html>
        """.trimIndent()
    }

    private fun httpResponse(body: String): String =
        "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${body.toByteArray(StandardCharsets.UTF_8).size}\r\nConnection: close\r\n\r\n$body"

    private fun redirect(location: String): String =
        "HTTP/1.1 302 Found\r\nLocation: $location\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"

    private fun formValue(body: String, key: String): String =
        body.split("&")
            .firstOrNull { it.substringBefore("=") == key }
            ?.substringAfter("=", "")
            ?.replace("+", " ")
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
            .orEmpty()

    /** Accepts a raw paste (with or without a leading "Cookie:" label and stray newlines). */
    private fun normalizeCookieHeader(raw: String): String =
        raw.trim()
            .removePrefix("Cookie:")
            .removePrefix("cookie:")
            .replace("\r", " ")
            .replace("\n", " ")
            .trim()

    private fun bestLocalAddress(): String =
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { address ->
                !address.isLoopbackAddress && address.hostAddress.orEmpty().indexOf(':') < 0
            }
            ?.hostAddress
            ?: "127.0.0.1"
}
