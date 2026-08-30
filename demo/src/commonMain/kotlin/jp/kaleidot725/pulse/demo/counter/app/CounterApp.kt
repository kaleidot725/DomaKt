package jp.kaleidot725.pulse.demo.counter.app

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorContent
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorStore
import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.mvi.PulseHost
import jp.kaleidot725.pulse.mvi.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.rememberPulseStore

/**
 * Owns everything this destination needs: its Store, its Container and its [PulseHost] subtree.
 *
 * The Navigation 3 entry decorator gives the destination its own `ViewModelStoreOwner`, so the Store
 * created here lives exactly as long as the route stays on the back stack.
 */
@Composable
fun CounterApp(onShowCounterDetails: (Int) -> Unit) {
    val store = rememberPulseStore { CounterOperatorStore(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }

    PulseHost(container = container) { _, _ ->
        DemoPage(title = "Counter") {
            CounterOperatorContent(
                store = store,
                onShowCounterDetails = onShowCounterDetails,
            )
        }
    }
}
