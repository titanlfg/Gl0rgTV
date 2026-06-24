package tv.gl0rg.kick

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaffoldTest {
    @Test
    fun appNameIsGl0rgTV() {
        assertEquals("Gl0rgTV", BuildConfig.APP_DISPLAY_NAME)
    }
}
