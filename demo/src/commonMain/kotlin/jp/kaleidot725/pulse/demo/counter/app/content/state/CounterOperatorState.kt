package jp.kaleidot725.pulse.demo.counter.app.content.state

import jp.kaleidot725.pulse.mvi.PulseState

data class CounterOperatorState(
    val count: Int = 0,
    val setupCount: Int = 0,
    val stopCount: Int = 0,
    val broadcastCount: Int = 0,
    val isSetupActive: Boolean = false,
    val lastLifecycleEvent: String = "Counter has not entered composition yet.",
) : PulseState
