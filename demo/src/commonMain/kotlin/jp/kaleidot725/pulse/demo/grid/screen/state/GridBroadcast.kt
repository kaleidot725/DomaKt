package jp.kaleidot725.pulse.demo.grid.screen.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed interface GridBroadcast : PulseBroadcast {
    /**
     * Sent to every area. Each one works out from [origin] whether it is the source, a neighbor, or
     * out of reach, so the Container never needs to know the grid layout.
     */
    data class Pulse(
        val origin: AreaPosition,
    ) : GridBroadcast

    data object Reset : GridBroadcast
}
