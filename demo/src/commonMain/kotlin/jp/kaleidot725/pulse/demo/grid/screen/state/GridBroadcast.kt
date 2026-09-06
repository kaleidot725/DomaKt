package jp.kaleidot725.pulse.demo.grid.screen.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed interface GridBroadcast : PulseBroadcast {
    /**
     * Sent to every area, the origin included. Each one compares [origin] against its own position
     * to work out what to do: the origin ignores it, having already counted the tap itself, a
     * neighbour counts it, and the diagonal does nothing. The Container therefore never needs to
     * know the grid layout.
     */
    data class Pulse(
        val origin: AreaPosition,
    ) : GridBroadcast

    data object Reset : GridBroadcast
}
