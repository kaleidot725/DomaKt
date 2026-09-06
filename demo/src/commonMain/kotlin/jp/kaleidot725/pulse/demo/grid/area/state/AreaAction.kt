package jp.kaleidot725.pulse.demo.grid.area.state

import jp.kaleidot725.pulse.mvi.PulseAction

sealed interface AreaAction : PulseAction {
    data object Pulse : AreaAction
}
