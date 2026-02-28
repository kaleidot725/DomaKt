package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.MVIState

data class CounterState(
    val count: Int = 0,
) : MVIState
