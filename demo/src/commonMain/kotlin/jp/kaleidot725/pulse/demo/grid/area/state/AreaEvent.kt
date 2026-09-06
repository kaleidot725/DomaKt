package jp.kaleidot725.pulse.demo.grid.area.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseEvent

sealed interface AreaEvent : PulseEvent {
    data class Charged(
        val position: AreaPosition,
        val count: Int,
    ) : AreaEvent
}
