package jp.kaleidot725.pulse.demo.counter.app.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.LifecycleStatus
import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorAction
import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorEvent
import jp.kaleidot725.pulse.mvi.PulseContent
import kotlinx.coroutines.launch

@Composable
fun CounterOperatorContent(
    store: CounterOperatorStore,
    onShowLifecycleDetails: () -> Unit,
    onCloseCounter: () -> Unit,
    onRefresh: () -> Unit,
    onBroadcast: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    PulseContent(
        store = store,
        onEvent = { event ->
            when (event) {
                is CounterOperatorEvent.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        },
    ) { state, onAction ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LifecycleStatus(state)
            Text(
                text = state.count.toString(),
                modifier = Modifier.testTag("counter-value"),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onAction(CounterOperatorAction.Decrement) }) {
                    Text("-")
                }
                Button(onClick = { onAction(CounterOperatorAction.Increment) }) {
                    Text("+")
                }
                OutlinedButton(onClick = { onAction(CounterOperatorAction.Reset) }) {
                    Text("Reset")
                }
            }
            Button(
                onClick = onShowLifecycleDetails,
                modifier = Modifier.fillMaxWidth().testTag("open-lifecycle-details"),
            ) {
                Text("Open lifecycle details")
            }
            OutlinedButton(
                onClick = onCloseCounter,
                modifier = Modifier.fillMaxWidth().testTag("close-counter"),
            ) {
                Text("Remove counter from stack")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onRefresh) {
                    Text("Refresh subtree")
                }
                OutlinedButton(onClick = onBroadcast) {
                    Text("Send broadcast")
                }
            }
            Text(
                text = "Refresh and covered destinations do not rerun onSetup while Counter remains in the back stack.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}
