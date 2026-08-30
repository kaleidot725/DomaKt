package jp.kaleidot725.pulse.demo.counter.detail.app.content

import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppUnicast
import jp.kaleidot725.pulse.demo.counter.detail.app.content.state.CounterDetailAction
import jp.kaleidot725.pulse.demo.counter.detail.app.content.state.CounterDetailEvent
import jp.kaleidot725.pulse.demo.counter.detail.app.content.state.CounterDetailState
import jp.kaleidot725.pulse.mvi.PulseStore

class CounterDetailStore(
    private val initialCount: Int,
) : PulseStore<CounterDetailState, CounterDetailAction, CounterDetailEvent, CounterAppBroadcast, CounterAppUnicast>(
        initialUiState = CounterDetailState(),
    ) {
    override fun onSetup() {
        update { copy(count = initialCount, setupCount = setupCount + 1) }
    }

    override fun onAction(uiAction: CounterDetailAction) {
        when (uiAction) {
            CounterDetailAction.Reload -> update { copy(reloadCount = reloadCount + 1) }
        }
    }
}
