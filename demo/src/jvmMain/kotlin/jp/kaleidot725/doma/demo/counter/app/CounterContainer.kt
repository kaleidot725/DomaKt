package jp.kaleidot725.doma.demo.counter.app

import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.mvi.DomaContainer
import jp.kaleidot725.doma.mvi.DomaStore

class CounterContainer(
    stores: List<DomaStore<*, *, *, CounterAppBroadcast>>,
) : DomaContainer<CounterAppBroadcast>(stores = stores)
