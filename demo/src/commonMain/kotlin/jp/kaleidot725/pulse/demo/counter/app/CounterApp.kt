package jp.kaleidot725.pulse.demo.counter.app

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorContent
import jp.kaleidot725.pulse.demo.counter.app.content.CounterOperatorStore

@Composable
fun CounterApp(
    store: CounterOperatorStore,
    onShowLifecycleDetails: () -> Unit,
    onCloseCounter: () -> Unit,
    onRefresh: () -> Unit,
    onBroadcast: () -> Unit,
) {
    DemoPage(title = "Counter destination") {
        CounterOperatorContent(
            store = store,
            onShowLifecycleDetails = onShowLifecycleDetails,
            onCloseCounter = onCloseCounter,
            onRefresh = onRefresh,
            onBroadcast = onBroadcast,
        )
    }
}
