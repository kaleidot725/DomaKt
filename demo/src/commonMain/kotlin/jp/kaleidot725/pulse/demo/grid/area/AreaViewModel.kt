package jp.kaleidot725.pulse.demo.grid.area

import jp.kaleidot725.pulse.demo.grid.area.state.AreaAction
import jp.kaleidot725.pulse.demo.grid.area.state.AreaEvent
import jp.kaleidot725.pulse.demo.grid.area.state.AreaState
import jp.kaleidot725.pulse.demo.grid.screen.state.GridBroadcast
import jp.kaleidot725.pulse.demo.grid.screen.state.GridUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * One quadrant. Four of these share a [GridContainer], and none of them knows the others exist.
 *
 * A tap is this area's own business: [sendPulse] counts it locally and only then announces it as a
 * Unicast. The Container broadcasts that to everyone, this area included, so [receivePulse] drops
 * the copy that came from itself — it has already been counted, and counting it twice would count
 * the tap twice. What the round trip is for is the areas that did not hear the tap first hand.
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
            AreaAction.Pulse -> sendPulse()
        }
    }

    override fun onReceive(broadcast: GridBroadcast) {
        when (broadcast) {
            is GridBroadcast.Pulse -> receivePulse(broadcast.origin)
            GridBroadcast.Reset -> reset()
        }
    }

    private fun sendPulse() {
        val position = currentState.position
        count(firedAt = position)
        unicast(GridUnicast.Pulsed(position))
    }

    private fun receivePulse(origin: AreaPosition) {
        val position = currentState.position
        // Its own pulse comes back with everyone else's copy. [sendPulse] already counted it.
        if (origin == position) return
        if (origin !in position.neighbors) return

        count(firedAt = origin)
    }

    private fun reset() {
        update { copy(count = 0, lastOrigin = null, pulseId = pulseId + 1) }
    }

    private fun count(firedAt: AreaPosition) {
        val before = currentState.count
        update { copy(count = count + 1, lastOrigin = firedAt, pulseId = pulseId + 1) }

        if (before < CHARGED_AT && currentState.count >= CHARGED_AT) {
            event(AreaEvent.Charged(currentState.position, currentState.count))
        }
    }
}

private const val CHARGED_AT = 12
