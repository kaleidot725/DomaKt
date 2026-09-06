package jp.kaleidot725.pulse.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

public abstract class PulseContainer<Broadcast : PulseBroadcast, Unicast : PulseUnicast>(
    private val viewModels: List<PulseViewModel<*, *, *, Broadcast, Unicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)

    private val containerKey: MutableStateFlow<Long> = MutableStateFlow(0L)
    internal val key: StateFlow<Long> = containerKey.asStateFlow()

    init {
        viewModels.forEach { viewModel ->
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                viewModel.unicast.collect { unicast -> onReceived(unicast) }
            }
        }
    }

    public fun refresh() {
        containerKey.value += 1
    }

    public fun broadcast(broadcast: Broadcast) {
        viewModels.forEach { it.onReceive(broadcast) }
    }

    /**
     * Cancels the Container scope and stops collecting Unicast messages from the ViewModels.
     *
     * Call this when the Container is gone for good. [rememberPulseContainer] calls it for you when
     * the owning [androidx.lifecycle.ViewModelStore] is cleared.
     */
    public fun close() {
        coroutineScope.cancel()
    }

    override fun onCleared() {
        close()
    }

    public open fun onReceived(unicast: Unicast) {}
}
