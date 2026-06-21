package jp.kaleidot725.pulse.demo.counter.app.content

import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorAction
import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorEvent
import jp.kaleidot725.pulse.demo.counter.app.content.state.CounterOperatorState
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppUnicast
import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.mvi.PulseStore
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

class CounterOperatorStore(
    private val repository: CounterRepository,
) : PulseStore<CounterOperatorState, CounterOperatorAction, CounterOperatorEvent, CounterAppBroadcast, CounterAppUnicast>(
        initialUiState = CounterOperatorState(),
    ) {
    override fun onSetup() {
        val setupNumber = currentState.setupCount + 1
        update {
            copy(
                setupCount = setupNumber,
                isSetupActive = true,
                lastLifecycleEvent = "onSetup #$setupNumber started.",
            )
        }
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                repository.count.collect { count ->
                    update { copy(count = count) }
                    if (count == 10) event(CounterOperatorEvent.ShowMessage("10 Count"))
                }
            } finally {
                val stopNumber = currentState.stopCount + 1
                update {
                    copy(
                        stopCount = stopNumber,
                        isSetupActive = false,
                        lastLifecycleEvent = "Setup scope #$setupNumber stopped after navigation.",
                    )
                }
            }
        }
    }

    override fun onAction(uiAction: CounterOperatorAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterOperatorAction.Increment -> {
                    repository.increment()
                }

                CounterOperatorAction.Decrement -> {
                    repository.decrement()
                }

                CounterOperatorAction.Reset -> {
                    repository.reset()
                    unicast(CounterAppUnicast.ResetRequested)
                }
            }
        }
    }

    override fun onReceive(broadcast: CounterAppBroadcast) {
        val broadcastNumber = currentState.broadcastCount + 1
        when (broadcast) {
            is CounterAppBroadcast.Refresh -> {
                update {
                    copy(
                        broadcastCount = broadcastNumber,
                        lastLifecycleEvent = "Broadcast #$broadcastNumber received.",
                    )
                }
            }

            is CounterAppBroadcast.ResetNotified -> {
                update {
                    copy(
                        broadcastCount = broadcastNumber,
                        lastLifecycleEvent = "Reset unicast reached the Container and was broadcast back.",
                    )
                }
            }
        }
    }
}
