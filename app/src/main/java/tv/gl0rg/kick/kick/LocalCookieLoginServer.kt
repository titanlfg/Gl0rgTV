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

class LocalCookieLoginServer(
    private val sessionProvider: KickSessionProvider
) {
    private var serverSocket: ServerSocket? = null
    private var job: Job? = null

    val url: String?
        get() = serverSocket?.localPort?.let { port -> "http://${bestLocalAddress()}:$port" }

    fun start(scope: CoroutineScope, onLogin: () -> Unit, onError: (String) -> Unit) {
        if (serverSocket != null) return
        runCatching {
            serverSocket = ServerSocket(0)
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
                                val cookie = formValue(body, "cookie")
                                if (cookie.isBlank()) {
                                    httpResponse(html("Missing cookie", "Paste the Kick Cookie header, then submit again."))
                                } else {
                                    sessionProvider.importCookieHeader(cookie)
                                    if (sessionProvider.hasSession()) {
                                        onLogin()
                                        httpResponse(html("Gl0rgTV linked", "Return to your Android TV."))
                                    } else {
                                        httpResponse(html("Cookie saved", "Gl0rgTV did not detect a Kick session cookie. Check the pasted value."))
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
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>Gl0rgTV Login</title>
          <style>
            body{font-family:Arial,sans-serif;background:#53FC18;color:#071007;padding:24px;line-height:1.35}
            h1{font-size:34px;margin:0 0 24px}
            li{margin:14px 0}
            a.login{display:block;text-align:center;font-size:28px;font-weight:800;color:#071007;margin:22px 0}
            textarea{width:100%;font-size:16px;box-sizing:border-box}
            button{font-size:20px;font-weight:800;margin:18px auto 0;display:block;padding:14px 42px;background:#071007;color:white;border:0}
            code{background:white;padding:2px 4px}
          </style>
        </head>
        <body>
        <h1>Follow these steps to login:</h1>
        <ol>
          <li><b>Read all steps first.</b></li>
          <li>Tap the link below. It opens the real Kick site. Log in there as usual.</li>
        </ol>
        <a class="login" href="https://kick.com/">Login with Kick</a>
        <ol start="3">
          <li>After Kick is logged in, copy the Kick request <b>Cookie</b> header. On desktop: open DevTools, Network, select a kick.com request, Request Headers, then copy Cookie.</li>
          <li>Come back to this page and paste the whole Cookie value into the box below.</li>
          <li>Press Submit. On success, Gl0rgTV closes the login screen.</li>
        </ol>
        <p>Cookie header:</p>
        <form method="post">
        <textarea name="cookie" rows="7" placeholder="Cookie: kick_session=...; laravel_session=..."></textarea>
        <button>Submit</button>
        </form>
        </body>
        </html>
    """.trimIndent()

    private fun html(title: String, body: String): String = """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width, initial-scale=1"><title>$title</title></head>
        <body style="font-family:sans-serif;background:#53FC18;color:#071007;padding:24px">
        <h1>$title</h1><p>$body</p><p>You can close this browser tab.</p>
        </body></html>
    """.trimIndent()

    private fun httpResponse(body: String): String =
        "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nContent-Length: ${body.toByteArray(StandardCharsets.UTF_8).size}\r\nConnection: close\r\n\r\n$body"

    private fun redirect(location: String): String =
        "HTTP/1.1 302 Found\r\nLocation: $location\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"

    private fun formValue(body: String, key: String): String =
        body.split("&")
            .firstOrNull { it.substringBefore("=") == key }
            ?.substringAfter("=", "")
            ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
            .orEmpty()

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
