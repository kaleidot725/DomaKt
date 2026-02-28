package jp.kaleidot725.doma.demo.counter.store

import jp.kaleidot725.doma.mvi.DomaEvent

sealed interface CounterStoreEvent : DomaEvent {
    data class ShowMessage(val message: String) : CounterStoreEvent
}
