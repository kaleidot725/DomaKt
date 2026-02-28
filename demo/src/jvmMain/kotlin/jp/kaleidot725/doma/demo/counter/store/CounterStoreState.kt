package jp.kaleidot725.doma.demo.counter.store

import jp.kaleidot725.doma.mvi.DomaState

data class CounterStoreState(
    val count: Int = 0,
) : DomaState
