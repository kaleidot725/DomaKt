package jp.kaleidot725.pulse.demo

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class DemoNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navigationRetainsSetupUntilRouteIsRemoved() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("store-active").assertTextEquals("Store active: no")
        composeRule.onNodeWithTag("setup-count").assertTextEquals("Setup runs: 0")

        composeRule.onNodeWithTag("open-counter").performClick()
        waitForTaggedText("store-active", "Store active: yes")
        waitForTaggedText("setup-count", "Setup runs: 1")

        composeRule.onNodeWithText("+").performClick()
        waitForTaggedText("counter-value", "1")

        composeRule.onNodeWithTag("open-lifecycle-details").performClick()
        waitForTaggedText("store-active", "Store active: yes")
        waitForTaggedText("stop-count", "Stop runs: 0")
        composeRule.onNodeWithTag("setup-count").assertTextEquals("Setup runs: 1")
        composeRule.onNodeWithTag("retained-count").assertTextEquals("Count retained: 1")

        composeRule.onNodeWithTag("back-to-counter").performClick()
        waitForTaggedText("store-active", "Store active: yes")
        waitForTaggedText("setup-count", "Setup runs: 1")
        composeRule.onNodeWithTag("counter-value").assertTextEquals("1")

        composeRule.onNodeWithText("Refresh subtree").performClick()
        composeRule.onNodeWithTag("setup-count").assertTextEquals("Setup runs: 1")

        composeRule.onNodeWithTag("close-counter").performClick()
        waitForTaggedText("store-active", "Store active: no")
        waitForTaggedText("stop-count", "Stop runs: 1")

        composeRule.onNodeWithTag("open-counter").performClick()
        waitForTaggedText("setup-count", "Setup runs: 2")
        composeRule.onNodeWithTag("counter-value").assertTextEquals("1")
    }

    private fun waitForTaggedText(
        tag: String,
        text: String,
    ) {
        composeRule.waitUntilExactlyOneExists(
            matcher = hasTestTag(tag) and hasText(text),
            timeoutMillis = 5_000,
        )
    }
}
