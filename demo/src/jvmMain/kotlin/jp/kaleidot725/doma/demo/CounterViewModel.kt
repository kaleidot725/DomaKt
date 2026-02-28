package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.MVIBase

class CounterViewModel : MVIBase<CounterState, CounterAction, CounterSideEffect>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        // no-op
    }

    override fun onRefresh() {
        // no-op
    }

    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> {
                update { copy(count = count + 1) }
                if (currentState.count % 10 == 0) {
                    sideEffect(CounterSideEffect.ShowMessage("${currentState.count} reached!"))
                }
            }
            CounterAction.Decrement -> {
                update { copy(count = count - 1) }
                if (currentState.count % 10 == 0) {
                    sideEffect(CounterSideEffect.ShowMessage("${currentState.count} reached!"))
                }
            }
            CounterAction.Reset -> {
                update { copy(count = 0) }
                sideEffect(CounterSideEffect.ShowMessage("Counter reset!"))
            }
        }
    }
}
