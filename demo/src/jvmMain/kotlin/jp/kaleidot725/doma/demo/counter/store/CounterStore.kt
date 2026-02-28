package jp.kaleidot725.doma.demo.counter.store

import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaStore
import kotlinx.coroutines.launch

class CounterStore(
    private val repository: CounterRepository,
) : DomaStore<CounterStoreState, CounterStoreAction, CounterStoreEvent>(
    initialUiState = CounterStoreState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
                if (count != 0 && count % 10 == 0) {
                    event(CounterStoreEvent.ShowMessage("$count reached!"))
                }
            }
        }
    }

    override fun onRefresh() {
        // no-op
    }

    override fun onAction(uiAction: CounterStoreAction) {
        // no-op: actions are handled by CounterPlatform
    }
}
