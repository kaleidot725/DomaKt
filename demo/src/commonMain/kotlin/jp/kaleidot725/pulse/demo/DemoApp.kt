package jp.kaleidot725.pulse.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import jp.kaleidot725.pulse.demo.grid.screen.GridScreen
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseNavEntryDecorators

private data class GridRoute(
    val depth: Int,
) : NavKey

/**
 * Keeps the back stack across a composition restart. The route is a single int, so a hand written
 * saver is cheaper than pulling in kotlinx-serialization for `rememberNavBackStack`.
 */
private val GridBackStackSaver: Saver<SnapshotStateList<GridRoute>, Any> =
    listSaver(
        save = { backStack -> backStack.map { it.depth } },
        restore = { saved ->
            mutableStateListOf(*saved.map { GridRoute(it) }.toTypedArray())
        },
    )

/**
 * Hosts the back stack only. Every entry is its own grid, with its own Container and its own four
 * ViewModels, and [rememberPulseNavEntryDecorators] scopes them to the entry: "New Area" pushes a
 * grid that starts at zero, and going back finds the previous one exactly as it was left.
 */
@Composable
fun DemoApp() {
    val backStack = rememberSaveable(saver = GridBackStackSaver) { mutableStateListOf(GridRoute(depth = 1)) }
    val popLast: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    MaterialTheme {
        NavDisplay(
            backStack = backStack,
            onBack = popLast,
            entryDecorators = rememberPulseNavEntryDecorators(),
            entryProvider =
                entryProvider {
                    entry<GridRoute> { route ->
                        GridScreen(
                            depth = route.depth,
                            onNewArea = { backStack.add(GridRoute(route.depth + 1)) },
                            onBack = popLast.takeIf { route.depth > 1 },
                        )
                    }
                },
        )
    }
}
