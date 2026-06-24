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
                                    html("Missing cookie", "Paste the Kick Cookie header, then submit again.")
                                } else {
                                    sessionProvider.importCookieHeader(cookie)
                                    if (sessionProvider.hasSession()) {
                                        onLogin()
                                        html("Gl0rgTV linked", "Return to your Android TV.")
                                    } else {
                                        html("Cookie saved", "Gl0rgTV did not detect a Kick session cookie. Check the pasted value.")
                                    }
                                }
                            } else {
                                formHtml()
                            }
                            it.getOutputStream().write(httpResponse(response).toByteArray(StandardCharsets.UTF_8))
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
        <head><meta name="viewport" content="width=device-width, initial-scale=1"><title>Gl0rgTV Login</title></head>
        <body style="font-family:sans-serif;background:#53FC18;color:#071007;padding:24px">
        <h1>Gl0rgTV</h1>
        <p>Paste your Kick Cookie header from a logged-in browser session.</p>
        <p style="font-size:14px">It can start with <code>Cookie:</code> or just contain values like <code>kick_session=...</code>.</p>
        <form method="post">
        <textarea name="cookie" rows="8" style="width:100%;font-size:16px"></textarea>
        <button style="font-size:18px;margin-top:12px;padding:12px 18px">Link Gl0rgTV</button>
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
