package jp.kaleidot725.pulse.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.junit.Rule
import org.junit.Test

/**
 * Covers the pulse itself: an area applies its own tap, announces it as a Unicast, the Container
 * broadcasts it back to all four, and each one works out from the origin what to do with it.
 */
@OptIn(ExperimentalTestApi::class)
class PulseGridTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pulseReachesTheEdgeNeighborsButNotTheDiagonal() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()

        // Two, not four: the origin applies its own tap and then drops the broadcast copy of it.
        assertCount("TopLeft", 2)
        assertCount("TopRight", 1)
        assertCount("BottomLeft", 1)
        assertCount("BottomRight", 0)
    }

    @Test
    fun everyAreaCanBeTheOrigin() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-BottomRight").performClick()

        assertCount("BottomRight", 2)
        assertCount("TopRight", 1)
        assertCount("BottomLeft", 1)
        assertCount("TopLeft", 0)
    }

    @Test
    fun pulsesAccumulate() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)
        composeRule.onNodeWithTag("area-TopRight").performClick()

        // TopLeft is a neighbor of TopRight, so the second pulse adds one to its own two.
        assertCount("TopRight", 3)
        assertCount("TopLeft", 3)
        assertCount("BottomRight", 1)
        assertCount("BottomLeft", 1)
    }

    @Test
    fun resetClearsEveryAreaAtOnce() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)

        composeRule.onNodeWithTag("reset").performClick()

        assertCount("TopLeft", 0)
        assertCount("TopRight", 0)
        assertCount("BottomLeft", 0)
        assertCount("BottomRight", 0)
    }

    @Test
    fun refreshRebuildsTheCellsWithoutLosingTheirCounts() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)

        composeRule.onNodeWithTag("refresh").performClick()

        assertCount("TopLeft", 2)
        assertCaption("TopLeft", "setup 1")
    }

    @Test
    fun newAreaStartsFreshAndGoingBackKeepsTheGridUnderneath() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)

        composeRule.onNodeWithTag("new-area").performClick()
        assertTitle("Area 2")
        assertCount("TopLeft", 0)

        composeRule.onNodeWithTag("back").performClick()
        assertTitle("Area 1")
        assertCount("TopLeft", 2)
    }

    @Test
    fun countsSurviveACompositionRestartWhileTheOwnerIsRetained() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)

        // Throw the whole tree away and rebuild it, the way a host driven restart would.
        composeRule.runOnUiThread { generation.value += 1 }
        composeRule.waitForIdle()

        assertCount("TopLeft", 2)
        assertCaption("TopLeft", "setup 1")
    }

    @Test
    fun clearingTheOwnerStartsTheGridOver() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 2)

        composeRule.runOnUiThread {
            generation.value += 1
            owner.viewModelStore.clear()
        }
        composeRule.waitForIdle()

        assertCount("TopLeft", 0)
        assertCaption("TopLeft", "setup 1")
    }

    private fun assertCount(
        position: String,
        count: Int,
    ) = awaitSingleNode(hasTestTag("count-$position") and hasText(count.toString()))

    private fun assertCaption(
        position: String,
        caption: String,
    ) = awaitSingleNode(hasTestTag("caption-$position") and hasText(caption, substring = true))

    private fun assertTitle(title: String) = awaitSingleNode(hasTestTag("grid-title") and hasText(title))

    /**
     * The cells are clickable, which merges their semantics, so the tagged Text nodes inside them
     * only exist in the unmerged tree.
     */
    private fun awaitSingleNode(matcher: SemanticsMatcher) =
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

private class RetainedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
