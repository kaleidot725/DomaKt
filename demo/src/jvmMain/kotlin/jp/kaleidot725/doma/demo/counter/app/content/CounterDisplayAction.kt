package jp.kaleidot725.doma.demo.counter.app.content

import jp.kaleidot725.doma.mvi.DomaAction

sealed interface CounterDisplayAction : DomaAction {
    data object Increment : CounterDisplayAction

    data object Decrement : CounterDisplayAction

    data object Reset : CounterDisplayAction
}
