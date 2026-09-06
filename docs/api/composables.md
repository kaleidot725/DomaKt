# Composables

## PulseApp

```kotlin
@Composable
fun <Broadcast : PulseBroadcast, Unicast : PulseUnicast> PulseApp(
    container: PulseContainer<Broadcast, Unicast>,
    content: @Composable (
        onRefresh: () -> Unit,
        onBroadcast: (Broadcast) -> Unit,
    ) -> Unit = { _, _ -> },
)
```

Wraps a `PulseContainer` and provides `onRefresh` and `onBroadcast` callbacks to the content block. All `PulseContent` composables placed inside `PulseApp` automatically respond to `container.refresh()`.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `container` | `PulseContainer<Broadcast, Unicast>` | The container to observe |
| `content` | `@Composable (onRefresh, onBroadcast) -> Unit` | Content block receiving the two callbacks |

### Example

```kotlin
PulseApp(container = appContainer) { onRefresh, onBroadcast ->
    Column {
        Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
            Text("Sync All")
        }
        Button(onClick = { onRefresh() }) {
            Text("Refresh View")
        }
        MyContent(viewModel = myViewModel)
    }
}
```

---

## PulseContent

```kotlin
@Composable
fun <
    State : PulseState,
    Action : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>
PulseContent(
    viewModel: PulseViewModel<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
)
```

Observes a `PulseViewModel` and provides state and an action dispatcher to the content block. Automatically cancels the ViewModel when removed from the composition.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `viewModel` | `PulseViewModel<State, Action, Event, Broadcast, Unicast>` | The ViewModel to observe |
| `onEvent` | `(Event) -> Unit` | Called for each one-time side effect emitted by the ViewModel |
| `content` | `@Composable (State, (Action) -> Unit) -> Unit` | Renders the current state; receives a dispatcher to send actions |

### Lifecycle behavior

- `LaunchedEffect(viewModel)` starts `onSetup()` and begins collecting `event`
- `DisposableEffect(viewModel)` calls `viewModel.cancel()` when the composable leaves the composition
- When inside `PulseApp`, the composable is wrapped in `key(containerKey)` and re-creates on `refresh()`

### Example

```kotlin
PulseContent(
    viewModel = counterViewModel,
    onEvent = { event ->
        when (event) {
            is CounterEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    },
) { state, onAction ->
    Column {
        Text("Count: ${state.count}")
        Button(onClick = { onAction(CounterAction.Increment) }) {
            Text("+")
        }
    }
}
```
