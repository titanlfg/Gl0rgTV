package tv.gl0rg.kick.kick

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KickSessionProviderTest {
    @Test
    fun sessionDetectsKickCookie() {
        val provider = FakeKickSessionProvider("session=abc; path=/")

        assertTrue(provider.hasSession())
        assertEquals("session=abc; path=/", provider.cookieHeader())
    }

    @Test
    fun nonAuthCookieDoesNotCountAsSession() {
        val provider = FakeKickSessionProvider("__cf_bm=abc; theme=dark")

        assertFalse(provider.hasSession())
    }

    @Test
    fun clearRemovesSession() {
        val provider = FakeKickSessionProvider("session=abc")

        provider.clear()

        assertFalse(provider.hasSession())
        assertEquals("", provider.cookieHeader())
    }

    @Test
    fun clearCallbackRunsAfterSessionRemoved() {
        val provider = FakeKickSessionProvider("session=abc")
        var callbackSawSession = true

        provider.clear {
            callbackSawSession = provider.hasSession()
        }

        assertFalse(callbackSawSession)
    }
}
