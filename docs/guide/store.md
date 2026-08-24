# Store

`PulseStore` is the core building block of PulseMVI. It manages the UI state for a single screen or section of your app.

## Creating a Store

Extend `PulseStore` with your five type parameters:

```kotlin
class MyStore : PulseStore<MyState, MyAction, MyEvent, MyBroadcast, MyUnicast>(
    initialUiState = MyState(),
) {
    override fun onSetup() { /* start coroutines here */ }
    override fun onAction(uiAction: MyAction) { /* handle user intents */ }
    override fun onReceive(broadcast: MyBroadcast) { /* react to broadcasts */ }
}
```

## Lifecycle Hooks

### `onSetup()`

Called when the first `PulseContent` observing the Store enters composition. Use this to start long-running coroutines such as repository flows:

```kotlin
override fun onSetup() {
    coroutineScope.launch {
        repository.data.collect { data ->
            update { copy(items = data) }
        }
    }
}
```

::: tip
Create the Store with `rememberPulseStore` so it survives Android configuration changes: it is owned by a `ViewModel`, which keeps a retention handle for the Store's whole lifetime, so rotation preserves state and does not repeat `onSetup()`.

The Store scope is cancelled after the last active `PulseContent` leaves composition unless a `store.retain()` handle is active. For Navigation 3, retain when the route enters the back stack and release only when the route is removed. Covering the route or refreshing its subtree then does not repeat setup.
:::

### `onAction(uiAction)`

Called each time the UI dispatches an action. Keep this non-blocking — launch coroutines for async work:

```kotlin
override fun onAction(uiAction: MyAction) {
    coroutineScope.launch {
        when (uiAction) {
            MyAction.Load -> loadData()
            is MyAction.Select -> selectItem(uiAction.id)
        }
    }
}
```

### `onReceive(broadcast)`

Called when the parent Container delivers a broadcast. You can update state or emit events:

```kotlin
override fun onReceive(broadcast: MyBroadcast) {
    when (broadcast) {
        MyBroadcast.Refresh -> update { copy(isRefreshing = true) }
        is MyBroadcast.UserChanged -> update { copy(userId = broadcast.id) }
    }
}
```

## Updating State

Use `update { }` to produce the next immutable state. The lambda receives the current state as `this`:

```kotlin
update { copy(count = count + 1, isLoading = false) }
```

## Emitting Events

Use `event()` to send a one-time side effect to the UI:

```kotlin
event(MyEvent.NavigateTo(Screen.Detail))
event(MyEvent.ShowError("Something went wrong"))
```

## Accessing Current State

Read the latest state snapshot synchronously via `currentState`:

```kotlin
override fun onAction(uiAction: MyAction) {
    if (currentState.isLoading) return  // guard check
    coroutineScope.launch { /* ... */ }
}
```
