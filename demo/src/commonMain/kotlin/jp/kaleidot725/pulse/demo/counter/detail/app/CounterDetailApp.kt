package jp.kaleidot725.pulse.demo.counter.detail.app

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.app.CounterContainer
import jp.kaleidot725.pulse.demo.counter.detail.app.content.CounterDetailContent
import jp.kaleidot725.pulse.demo.counter.detail.app.content.CounterDetailStore
import jp.kaleidot725.pulse.mvi.PulseApp
import jp.kaleidot725.pulse.mvi.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.rememberPulseStore

/**
 * The second destination, built the same way as the first one: its own Store, Container and
 * [PulseApp] subtree.
 *
 * Popping this route clears the entry's `ViewModelStoreOwner`, so the Store is cancelled and a
 * later visit starts a fresh one. The counter destination underneath is untouched.
 */
@Composable
fun CounterDetailApp(
    count: Int,
    onBack: () -> Unit,
) {
    val store = rememberPulseStore { CounterDetailStore(count) }
    val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }

    PulseApp(container = container) { _, _ ->
        DemoPage(title = "Count details") {
            CounterDetailContent(store = store, onBack = onBack)
        }
    }
}
