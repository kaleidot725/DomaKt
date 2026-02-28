package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.MVISideEffect

sealed interface CounterSideEffect : MVISideEffect {
    data class ShowMessage(val message: String) : CounterSideEffect
}
