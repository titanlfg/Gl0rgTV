package tv.gl0rg.kick.kick

import android.webkit.CookieManager

interface KickSessionProvider {
    fun hasSession(): Boolean
    fun cookieHeader(): String
    fun importCookieHeader(cookieHeader: String)
    fun clear()
    fun clear(onComplete: () -> Unit)
}

class WebViewKickSessionProvider(
    private val cookieManager: CookieManager = CookieManager.getInstance()
) : KickSessionProvider {
    override fun hasSession(): Boolean = KickCookieAuth.hasAuthCookie(cookieHeader())

    override fun cookieHeader(): String = cookieManager.getCookie("https://kick.com").orEmpty()

    override fun importCookieHeader(cookieHeader: String) {
        cookieHeader
            .removePrefix("Cookie:")
            .split(";")
            .map { it.trim() }
            .filter { it.contains("=") }
            .forEach { cookie -> cookieManager.setCookie("https://kick.com", cookie) }
        cookieManager.flush()
    }

    override fun clear() {
        clear {}
    }

    override fun clear(onComplete: () -> Unit) {
        cookieManager.removeAllCookies {
            cookieManager.flush()
            onComplete()
        }
    }
}

class FakeKickSessionProvider(initialCookieHeader: String = "") : KickSessionProvider {
    private var cookieHeader = initialCookieHeader

    override fun hasSession(): Boolean = KickCookieAuth.hasAuthCookie(cookieHeader)

    override fun cookieHeader(): String = cookieHeader

    override fun importCookieHeader(cookieHeader: String) {
        this.cookieHeader = cookieHeader.removePrefix("Cookie:").trim()
    }

    override fun clear() {
        clear {}
    }

    override fun clear(onComplete: () -> Unit) {
        cookieHeader = ""
        onComplete()
    }
}

object KickCookieAuth {
    private val authCookieNames = setOf("session", "kick_session", "laravel_session")

    fun hasAuthCookie(cookieHeader: String): Boolean =
        cookieHeader
            .split(";")
            .map { it.substringBefore("=").trim() }
            .any { it in authCookieNames }
}
