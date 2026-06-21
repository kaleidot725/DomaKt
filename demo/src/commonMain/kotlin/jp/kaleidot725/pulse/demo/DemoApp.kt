package jp.kaleidot725.pulse.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorState
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.mvi.PulseApp
import jp.kaleidot725.pulse.mvi.PulseStoreRetention

private sealed interface DemoRoute : NavKey {
    data object Home : DemoRoute

    data object Counter : DemoRoute

    data object LifecycleDetails : DemoRoute
}

@Composable
fun DemoApp() {
    val repository = remember { CounterRepository() }
    val store = remember { CounterOperatorStore(repository) }
    val container = remember { CounterContainer(stores = listOf(store)) }
    val backStack = remember { mutableStateListOf<DemoRoute>(DemoRoute.Home) }
    var counterRetention by remember { mutableStateOf<PulseStoreRetention?>(null) }

    val openCounter: () -> Unit = {
        if (counterRetention == null) counterRetention = store.retain()
        backStack.add(DemoRoute.Counter)
    }
    val popLast: () -> Unit = {
        when (backStack.removeLastOrNull()) {
            DemoRoute.Counter -> {
                counterRetention?.release()
                counterRetention = null
            }

            else -> Unit
        }
    }

    MaterialTheme {
        PulseApp(container = container) { onRefresh, onBroadcast ->
            NavDisplay(
                backStack = backStack,
                onBack = popLast,
                entryProvider =
                    entryProvider {
                        entry<DemoRoute.Home> {
                            HomeScreen(
                                store = store,
                                onOpenCounter = openCounter,
                                onBroadcast = { onBroadcast(CounterAppBroadcast.Refresh) },
                            )
                        }
                        entry<DemoRoute.Counter> {
                            CounterApp(
                                store = store,
                                onShowLifecycleDetails = { backStack.add(DemoRoute.LifecycleDetails) },
                                onCloseCounter = popLast,
                                onRefresh = onRefresh,
                                onBroadcast = { onBroadcast(CounterAppBroadcast.Refresh) },
                            )
                        }
                        entry<DemoRoute.LifecycleDetails> {
                            LifecycleDetailsScreen(
                                store = store,
                                onBackToCounter = popLast,
                                onBackToHome = {
                                    while (backStack.size > 1) popLast()
                                },
                            )
                        }
                    },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    store: CounterOperatorStore,
    onOpenCounter: () -> Unit,
    onBroadcast: () -> Unit,
) {
    val state by store.state.collectAsState()

    DemoPage(title = "PulseMVI + Navigation 3") {
        Text(
            text = "The Store lives above the Navigation 3 back stack. Only the Counter destination observes it with PulseContent.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LifecycleStatus(state)
        Button(
            onClick = onOpenCounter,
            modifier = Modifier.fillMaxWidth().testTag("open-counter"),
        ) {
            Text("Open counter")
        }
        OutlinedButton(
            onClick = onBroadcast,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Broadcast while counter is closed")
        }
    }
}

@Composable
private fun LifecycleDetailsScreen(
    store: CounterOperatorStore,
    onBackToCounter: () -> Unit,
    onBackToHome: () -> Unit,
) {
    val state by store.state.collectAsState()

    DemoPage(title = "Store after navigation") {
        Text(
            text =
                "Counter is not composed, but its route is still on the Navigation 3 back stack. " +
                    "The route retention keeps setup active.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LifecycleStatus(state)
        Button(
            onClick = onBackToCounter,
            modifier = Modifier.fillMaxWidth().testTag("back-to-counter"),
        ) {
            Text("Back to counter")
        }
        OutlinedButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back to home")
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
internal fun LifecycleStatus(state: CounterOperatorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Store active: ${if (state.isSetupActive) "yes" else "no"}", modifier = Modifier.testTag("store-active"))
            Text("Setup runs: ${state.setupCount}", modifier = Modifier.testTag("setup-count"))
            Text("Stop runs: ${state.stopCount}", modifier = Modifier.testTag("stop-count"))
            Text("Count retained: ${state.count}", modifier = Modifier.testTag("retained-count"))
            Text("Broadcasts received: ${state.broadcastCount}")
            Text(state.lastLifecycleEvent, style = MaterialTheme.typography.bodySmall)
        }
    }
}
