package jp.kaleidot725.doma.demo.counter.app.state

import jp.kaleidot725.doma.mvi.DomaAction

sealed interface CounterAppAction : DomaAction {
    data object Restart: CounterAppAction
}
