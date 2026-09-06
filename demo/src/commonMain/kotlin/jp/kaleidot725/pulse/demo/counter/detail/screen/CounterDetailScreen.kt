package jp.kaleidot725.pulse.demo.counter.detail.screen

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.CounterDetailContent
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.CounterDetailViewModel
import jp.kaleidot725.pulse.demo.counter.screen.CounterContainer
import jp.kaleidot725.pulse.mvi.PulseHost
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseViewModel

/**
 * The second destination, built the same way as the first one: its own ViewModel, Container and
 * [PulseHost] subtree.
 *
 * Popping this route clears the entry's `ViewModelStoreOwner`, so the Store is cancelled and a
 * later visit starts a fresh one. The counter destination underneath is untouched.
 */
@Composable
fun CounterDetailScreen(
    count: Int,
    onBack: () -> Unit,
) {
    val viewModel = rememberPulseViewModel { CounterDetailViewModel(count) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    PulseHost(container = container) { _, _ ->
        DemoPage(title = "Count details") {
            CounterDetailContent(viewModel = viewModel, onBack = onBack)
        }
    }
}
