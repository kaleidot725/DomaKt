package jp.kaleidot725.pulse.demo.grid.screen.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseUnicast

sealed interface GridUnicast : PulseUnicast {
    data class Pulsed(
        val origin: AreaPosition,
    ) : GridUnicast
}
