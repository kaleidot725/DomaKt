package jp.kaleidot725.pulse.demo.grid.area.state

import jp.kaleidot725.pulse.demo.grid.area.AreaPosition
import jp.kaleidot725.pulse.mvi.PulseState

/**
 * How this area was touched by the last pulse, and what that is worth to it. A diagonal area is left
 * out of the pulse entirely, so it has no role of its own.
 */
enum class PulseRole(
    val gain: Int,
) {
    Origin(gain = 2),
    Neighbor(gain = 1),
}

data class AreaState(
    val position: AreaPosition = AreaPosition.TopLeft,
    val count: Int = 0,
    val lastRole: PulseRole? = null,
    val setupCount: Int = 0,
    /** Bumped on every pulse this area reacts to, so the UI can restart its flash animation. */
    val pulseId: Long = 0,
) : PulseState
