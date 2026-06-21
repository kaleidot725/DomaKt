package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public abstract class PulseStore<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    private val initialUiState: UiState,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    public var coroutineScope: CoroutineScope = createCoroutineScope(coroutineDispatcher)
        private set

    private val uiState: MutableStateFlow<UiState> = MutableStateFlow(initialUiState)

    public val state: StateFlow<UiState> = uiState.asStateFlow()

    public val currentState: UiState get() = state.value

    private val _event: Channel<Event> by lazy { Channel() }

    public val event: Flow<Event> = _event.receiveAsFlow()

    private val _unicast: MutableSharedFlow<Unicast> = MutableSharedFlow(extraBufferCapacity = 64)

    public val unicast: SharedFlow<Unicast> = _unicast.asSharedFlow()

    private var attachmentCount: Int = 0

    public open fun onSetup() {}

    public abstract fun onAction(uiAction: UiAction)

    public open fun onReceive(broadcast: Broadcast) {}

    public fun cancel() {
        coroutineScope.cancel()
        coroutineScope = createCoroutineScope(coroutineDispatcher)
    }

    internal fun attach() {
        attachmentCount += 1
        if (attachmentCount == 1) onSetup()
    }

    internal fun detach() {
        check(attachmentCount > 0) { "PulseStore is not attached to a PulseContent." }
        attachmentCount -= 1
        if (attachmentCount == 0) cancel()
    }

    public fun update(block: UiState.() -> UiState) {
        uiState.update { block(it) }
    }

    public fun event(effect: Event) {
        coroutineScope.launch { _event.send(effect) }
    }

    public fun unicast(unicast: Unicast) {
        _unicast.tryEmit(unicast)
    }

    private companion object {
        private fun createCoroutineScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope =
            CoroutineScope(SupervisorJob() + coroutineDispatcher)
    }
}
