package jp.kaleidot725.pulse.demo.grid.screen

import jp.kaleidot725.pulse.demo.grid.screen.state.GridBroadcast
import jp.kaleidot725.pulse.demo.grid.screen.state.GridUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * Turns one area's Unicast into a Broadcast for all four.
 *
 * A tapped area applies the tap to itself and reports it upwards; the Container sends it back down
 * to everyone. The broadcast goes to all four rather than to a computed set of neighbours: the
 * Container does not know the layout, so each area decides what the pulse means for it — including
 * the origin, which drops the copy of its own tap. Adding a fifth area would not change this class.
 */
class GridContainer(
    viewModels: List<PulseViewModel<*, *, *, GridBroadcast, GridUnicast>>,
) : PulseContainer<GridBroadcast, GridUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: GridUnicast) {
        when (unicast) {
            is GridUnicast.Pulsed -> broadcast(GridBroadcast.Pulse(unicast.origin))
        }
    }
}
