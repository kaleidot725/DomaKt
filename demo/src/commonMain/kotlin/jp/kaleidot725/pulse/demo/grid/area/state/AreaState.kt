package jp.kaleidot725.pulse.demo.grid.area.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseState

data class AreaState(
    val position: AreaPosition = AreaPosition.TopLeft,
    val count: Int = 0,
    /**
     * Where the last pulse this area reacted to was fired. Compared against [position] it says
     * whether this area was the source or a neighbour, so neither needs storing. Null until the
     * first pulse arrives, and again after a reset.
     */
    val lastOrigin: AreaPosition? = null,
    val setupCount: Int = 0,
    /** Bumped on every pulse this area reacts to, so the UI can restart its flash animation. */
    val pulseId: Long = 0,
) : PulseState
