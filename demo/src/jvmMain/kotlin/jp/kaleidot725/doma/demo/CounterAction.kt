package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.MVIAction

sealed interface CounterAction : MVIAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
}
