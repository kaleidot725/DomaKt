package jp.kaleidot725.pulse.demo.grid.screen

import jp.kaleidot725.pulse.demo.grid.screen.state.GridBroadcast
import jp.kaleidot725.pulse.demo.grid.screen.state.GridUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * Turns one area's Unicast into a Broadcast for all four.
 *
 * This is the whole pulse: a tapped area does not raise its own count, it reports upwards, and the
 * Container sends the pulse back down to everyone. Each area then decides what the pulse means for
 * it, so adding a fifth area would not change this class.
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
