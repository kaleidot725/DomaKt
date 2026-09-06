package jp.kaleidot725.pulse.demo.counter.screen

import jp.kaleidot725.pulse.demo.counter.screen.state.CounterBroadcast
import jp.kaleidot725.pulse.demo.counter.screen.state.CounterUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels)
