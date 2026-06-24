package tv.gl0rg.kick.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateVersionsTest {
    @Test
    fun detectsNewerSemanticVersion() {
        assertTrue(UpdateVersions.isNewer("0.1.2", "0.1.1"))
        assertTrue(UpdateVersions.isNewer("v1.0.0", "0.9.9"))
    }

    @Test
    fun rejectsSameOrOlderVersion() {
        assertFalse(UpdateVersions.isNewer("0.1.2", "0.1.2"))
        assertFalse(UpdateVersions.isNewer("0.1.1", "0.1.2"))
    }
}
