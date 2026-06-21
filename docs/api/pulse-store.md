# PulseStore

```kotlin
abstract class PulseStore<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    initialUiState: UiState,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
)
```

Abstract base class for managing the UI state of a single screen or section.

Use `unicast()` when the Store needs to send messages up to its parent Container.

## Properties

### `state`

```kotlin
val state: StateFlow<UiState>
```

The current UI state as a hot, read-only `StateFlow`. Collecting it does not change the Store lifecycle.

---

### `currentState`

```kotlin
val currentState: UiState
```

Synchronous snapshot of the current UI state. Equivalent to `state.value`.

---

### `event`

```kotlin
val event: Flow<Event>
```

A cold `Flow` of one-time side effects emitted via `event()`. Collected by `PulseContent`.

---

### `unicast`

```kotlin
val unicast: SharedFlow<Unicast>
```

A hot stream of child-to-parent unicasts emitted via `unicast()`.

---

### `coroutineScope`

```kotlin
val coroutineScope: CoroutineScope
```

A `CoroutineScope` backed by `SupervisorJob` and the dispatcher passed to the constructor. The dispatcher defaults to `Dispatchers.Default`; pass `Dispatchers.Main` or a test dispatcher when required. The owned scope is cancelled and recreated on `cancel()`.

## Methods

### `onSetup()`

```kotlin
open fun onSetup()
```

Called when the Store first gains an active `PulseContent` observer or retention. The scope is cancelled after all observers and retentions are released, and `onSetup()` runs again the next time it becomes active. Override it to start data-collection coroutines. Store state is preserved while inactive.

---

### `onAction(uiAction)`

```kotlin
abstract fun onAction(uiAction: UiAction)
```

Called each time the UI dispatches an action. Launch coroutines for async work here.

---

### `onReceive(broadcast)`

```kotlin
open fun onReceive(broadcast: Broadcast)
```

Called when the parent `PulseContainer` delivers a broadcast. Default implementation does nothing.

---

### `update(block)`

```kotlin
fun update(block: UiState.() -> UiState)
```

Applies an immutable state update. The lambda receives the current state as `this` and must return the next state:

```kotlin
update { copy(count = count + 1) }
```

---

### `event(effect)`

```kotlin
fun event(effect: Event)
```

Emits a one-time side effect to the UI layer. Collected by the `onEvent` lambda in `PulseContent`.

---

### `unicast(unicast)`

```kotlin
fun unicast(unicast: Unicast)
```

Emits a child-to-parent message. The parent `PulseContainer` collects the Store's `unicast` flow and receives it through `onReceived()`.

---

### `cancel()`

```kotlin
fun cancel()
```

Cancels the current `coroutineScope` and prepares the Store for reuse. Called automatically after the last `PulseContent` observer and explicit retention are released. The next activation runs `onSetup()` with a new scope and the preserved state.

---

### `retain()`

```kotlin
fun retain(): PulseStoreRetention
```

Keeps the Store setup active independently of active `PulseContent` observers. This is useful with user-owned navigation back stacks: retain the Store when its route enters the stack and call `release()` when that route is removed. Temporary destination changes then do not repeat `onSetup()`.

## Example

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterStore(
    private val repository: CounterRepository,
) : PulseStore<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
            }
        }
    }

    override fun onAction(uiAction: CounterAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterAction.Increment -> repository.increment()
                CounterAction.Decrement -> repository.decrement()
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            CounterBroadcast.Reset -> update { CounterState() }
        }
    }
}
```

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterStore(
    private val repository: CounterRepository,
) : PulseStore<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Reset -> {
                repository.reset()
                unicast(CounterUnicast.ResetRequested)
            }
            CounterAction.Increment -> repository.increment()
            CounterAction.Decrement -> repository.decrement()
        }
    }
}
```
