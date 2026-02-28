package jp.kaleidot725.doma.demo.counter.platform

import jp.kaleidot725.doma.mvi.DomaAction

sealed interface CounterPlatformAction : DomaAction {
    data object Increment : CounterPlatformAction
    data object Decrement : CounterPlatformAction
    data object Reset : CounterPlatformAction
}
