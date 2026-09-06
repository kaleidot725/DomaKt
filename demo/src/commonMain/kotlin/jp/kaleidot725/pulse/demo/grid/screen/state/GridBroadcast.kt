package jp.kaleidot725.pulse.demo.grid.screen.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed interface GridBroadcast : PulseBroadcast {
    /**
     * Sent to every area, the origin included. Each one works out from [origin] what to do: the
     * origin ignores it, having already applied the tap itself, a neighbor gains a point, and an
     * area out of reach does nothing. The Container therefore never needs to know the grid layout.
     */
    data class Pulse(
        val origin: AreaPosition,
    ) : GridBroadcast

    data object Reset : GridBroadcast
}
