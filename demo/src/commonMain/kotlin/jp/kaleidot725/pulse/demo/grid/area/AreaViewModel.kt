package jp.kaleidot725.pulse.demo.grid.area

import jp.kaleidot725.pulse.demo.grid.area.state.AreaAction
import jp.kaleidot725.pulse.demo.grid.area.state.AreaEvent
import jp.kaleidot725.pulse.demo.grid.area.state.AreaState
import jp.kaleidot725.pulse.demo.grid.area.state.PulseRole
import jp.kaleidot725.pulse.demo.grid.screen.state.GridBroadcast
import jp.kaleidot725.pulse.demo.grid.screen.state.GridUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * One quadrant. Four of these share a [GridContainer], and none of them knows the others exist.
 *
 * A tap does not change this area directly: [onAction] sends the tap up as a Unicast, the Container
 * broadcasts it back, and [onReceive] applies it. The round trip is what lets a single tap raise
 * three different counts by three different amounts.
 */
class AreaViewModel(
    position: AreaPosition,
) : PulseViewModel<AreaState, AreaAction, AreaEvent, GridBroadcast, GridUnicast>(
        initialUiState = AreaState(position = position),
    ) {
    override fun onSetup() {
        update { copy(setupCount = setupCount + 1) }
    }

    override fun onAction(uiAction: AreaAction) {
        when (uiAction) {
            AreaAction.Pulse -> unicast(GridUnicast.Pulsed(currentState.position))
        }
    }

    override fun onReceive(broadcast: GridBroadcast) {
        when (broadcast) {
            is GridBroadcast.Pulse -> receive(broadcast.origin)
            GridBroadcast.Reset -> update { copy(count = 0, lastRole = null, pulseId = pulseId + 1) }
        }
    }

    private fun receive(origin: AreaPosition) {
        val position = currentState.position
        val role =
            when {
                origin == position -> PulseRole.Origin
                origin in position.neighbors -> PulseRole.Neighbor
                else -> return
            }

        val gain = if (role == PulseRole.Origin) ORIGIN_GAIN else NEIGHBOR_GAIN
        val before = currentState.count
        update { copy(count = count + gain, lastRole = role, pulseId = pulseId + 1) }

        if (before < CHARGED_AT && currentState.count >= CHARGED_AT) {
            event(AreaEvent.Charged(position, currentState.count))
        }
    }

    private companion object {
        const val ORIGIN_GAIN = 2
        const val NEIGHBOR_GAIN = 1
        const val CHARGED_AT = 12
    }
}
