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
    fun counterDetailsShowsSelectedCountAndReturns() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("counter-value").assertTextEquals("0")

        composeRule.onNodeWithText("+").performClick()
        waitForTaggedText("counter-value", "1")

        composeRule.onNodeWithTag("open-counter-details").performClick()
        waitForTaggedText("counter-detail-value", "1")

        composeRule.onNodeWithTag("back-to-counter").performClick()
        waitForTaggedText("counter-value", "1")

        composeRule.onNodeWithText("+").performClick()
        waitForTaggedText("counter-value", "2")
        composeRule.onNodeWithTag("open-counter-details").performClick()
        waitForTaggedText("counter-detail-value", "2")
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
