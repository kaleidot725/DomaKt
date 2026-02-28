package jp.kaleidot725.doma.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public abstract class DomaStoreContainer<Broadcast : DomaBroadcast>(
    private val stores: List<DomaStore<*, *, *, Broadcast>>,
) {
    public var coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)
        private set

    public abstract fun onBroadcast(broadcast: Broadcast)
    public fun broadcast(broadcast: Broadcast) {
        stores.forEach { it.onReceive(broadcast) }
    }
}
