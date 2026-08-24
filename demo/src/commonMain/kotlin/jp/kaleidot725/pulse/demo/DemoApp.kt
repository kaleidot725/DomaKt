package jp.kaleidot725.pulse.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import jp.kaleidot725.pulse.demo.counter.app.CounterApp
import jp.kaleidot725.pulse.demo.counter.app.CounterContainer
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorStore
import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.mvi.PulseApp
import jp.kaleidot725.pulse.mvi.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.rememberPulseStore

private sealed interface DemoRoute : NavKey {
    data object Counter : DemoRoute

    data class CounterDetails(
        val count: Int,
    ) : DemoRoute
}

/**
 * Keeps the back stack across configuration changes. `DemoRoute` is small enough that a hand
 * written saver is cheaper than pulling in kotlinx-serialization for `rememberNavBackStack`.
 */
private val DemoRouteBackStackSaver: Saver<SnapshotStateList<DemoRoute>, Any> =
    listSaver(
        save = { backStack ->
            backStack.map { route ->
                when (route) {
                    DemoRoute.Counter -> "counter"
                    is DemoRoute.CounterDetails -> "counter-details:${route.count}"
                }
            }
        },
        restore = { saved ->
            val routes =
                saved.map { token ->
                    when {
                        token == "counter" -> DemoRoute.Counter
                        else -> DemoRoute.CounterDetails(token.substringAfter(':').toInt())
                    }
                }
            mutableStateListOf(*routes.toTypedArray())
        },
    )

@Composable
fun DemoApp() {
    // Store and Container are owned by ViewModels, so an Android configuration change recreates the
    // composition without recreating them: the count survives and onSetup() is not repeated.
    val store = rememberPulseStore { CounterOperatorStore(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }
    val backStack = rememberSaveable(saver = DemoRouteBackStackSaver) { mutableStateListOf<DemoRoute>(DemoRoute.Counter) }
    val showCounterDetails: (Int) -> Unit = { count ->
        backStack.add(DemoRoute.CounterDetails(count))
    }
    val popLast: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    MaterialTheme {
        PulseApp(container = container) { _, _ ->
            NavDisplay(
                backStack = backStack,
                onBack = popLast,
                entryProvider =
                    entryProvider {
                        entry<DemoRoute.Counter> {
                            CounterApp(
                                store = store,
                                onShowCounterDetails = showCounterDetails,
                            )
                        }
                        entry<DemoRoute.CounterDetails> { route ->
                            CounterDetailsScreen(
                                count = route.count,
                                onBack = popLast,
                            )
                        }
                    },
            )
        }
    }
}

@Composable
internal fun DemoPage(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
private fun CounterDetailsScreen(
    count: Int,
    onBack: () -> Unit,
) {
    DemoPage(title = "Count details") {
        Text(
            text = "Counter value",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = count.toString(),
            modifier = Modifier.testTag("counter-detail-value"),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().testTag("back-to-counter"),
        ) {
            Text("Back")
        }
    }
}
