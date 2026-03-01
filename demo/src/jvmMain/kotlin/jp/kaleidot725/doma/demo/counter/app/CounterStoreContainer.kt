package jp.kaleidot725.doma.demo.counter.app

import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.mvi.DomaStore
import jp.kaleidot725.doma.mvi.DomaStoreContainer

class CounterStoreContainer(
    stores: List<DomaStore<*, *, *, CounterAppBroadcast>>,
) : DomaStoreContainer<CounterAppBroadcast>(stores = stores)
