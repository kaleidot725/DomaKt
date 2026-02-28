package jp.kaleidot725.doma.demo.counter.platform

import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaBase

class CounterPlatform(
    private val repository: CounterRepository,
) : DomaBase<CounterPlatformState, CounterPlatformAction, CounterPlatformEvent>(
    initialUiState = CounterPlatformState,
) {
    override fun onSetup() {
        // no-op
    }

    override fun onAction(uiAction: CounterPlatformAction) {
        when (uiAction) {
            CounterPlatformAction.Increment -> repository.increment()
            CounterPlatformAction.Decrement -> repository.decrement()
            CounterPlatformAction.Reset -> repository.reset()
        }
    }
}
