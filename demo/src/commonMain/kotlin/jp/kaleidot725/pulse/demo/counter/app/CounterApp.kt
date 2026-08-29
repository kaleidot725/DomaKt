package jp.kaleidot725.pulse.demo.counter.app

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorContent
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorStore

@Composable
fun CounterApp(
    store: CounterOperatorStore,
    onShowCounterDetails: (Int) -> Unit,
) {
    DemoPage(title = "Counter") {
        CounterOperatorContent(
            store = store,
            onShowCounterDetails = onShowCounterDetails,
        )
    }
}
