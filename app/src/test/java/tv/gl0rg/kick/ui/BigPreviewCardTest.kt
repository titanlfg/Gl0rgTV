package tv.gl0rg.kick.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Headless Compose UI test (Robolectric) — runs in CI with no emulator. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BigPreviewCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsNameAndViewerCount() {
        composeRule.setContent {
            Gl0rgTheme(option = ThemeOption.Kick) {
                BigPreviewCard(
                    name = "alice",
                    subtitle = "Just Chatting",
                    viewers = "4.2K",
                    imageUrl = null,
                    avatarUrl = null,
                    previewHlsUrl = null,
                    onClick = {}
                )
            }
        }
        composeRule.onNodeWithText("alice").assertIsDisplayed()
        composeRule.onNodeWithText("4.2K viewers").assertIsDisplayed()
    }
}
