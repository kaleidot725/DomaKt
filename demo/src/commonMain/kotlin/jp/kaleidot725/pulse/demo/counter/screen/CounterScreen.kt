package jp.kaleidot725.pulse.demo.counter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.demo.counter.screen.content.CounterOperatorContent
import jp.kaleidot725.pulse.demo.counter.screen.content.CounterOperatorViewModel
import jp.kaleidot725.pulse.mvi.PulseHost
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseViewModel

/**
 * Owns everything this destination needs: its ViewModel, its Container and its [PulseHost] subtree.
 *
 * The Navigation 3 entry decorator gives the destination its own `ViewModelStoreOwner`, so the Store
 * created here lives exactly as long as the route stays on the back stack.
 */
@Composable
fun CounterScreen(onShowCounterDetails: (Int) -> Unit) {
    val viewModel = rememberPulseViewModel { CounterOperatorViewModel(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    val snackbarHostState = remember { SnackbarHostState() }

    PulseHost(container = container) { _, _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            DemoPage(title = "Counter") {
                CounterOperatorContent(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onShowCounterDetails = onShowCounterDetails,
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
