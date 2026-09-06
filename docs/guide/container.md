# Container

`PulseContainer` sits above one or more ViewModels and provides three coordination capabilities: **broadcast**, **unicast handling**, and **view refresh**.

## Creating a Container

Pass the list of ViewModels you want to coordinate:

```kotlin
class AppContainer(
    viewModels: List<PulseViewModel<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(viewModels = viewModels)
```

Instantiate it at the same level as your ViewModels:

```kotlin
val sidebarViewModel = remember { SidebarViewModel() }
val contentViewModel = remember { ContentViewModel() }
val container = remember {
    AppContainer(viewModels = listOf(sidebarViewModel, contentViewModel))
}
```

## Broadcast

Send a typed message to **all** registered ViewModels simultaneously:

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

Every ViewModel in the list receives `onReceive(AppBroadcast.UserLoggedOut)` and can react independently.

### When to use Broadcast

- Synchronizing state across multiple ViewModels (e.g., theme change, locale change)
- Notifying all ViewModels of a global event (e.g., session expiry, network reconnected)
- Propagating data that multiple ViewModels need (e.g., updated user profile)

## View Refresh

Force the entire Compose view tree under `PulseApp` to reconstruct:

```kotlin
container.refresh()
```

::: tip What gets reset?
- **Compose state** (e.g., `remember { }` inside Composables) is **reset**
- **ViewModel state** (values in `PulseViewModel.state`) is **preserved**
:::

### When to use Refresh

- Applying a theme or locale change that affects the whole layout
- Recovering from a corrupted Compose state
- Forcing re-creation of composables that don't respond to state changes

## Using inside PulseApp

`PulseApp` reads the Container's internal key and wraps content in a `CompositionLocalProvider`. `PulseContent` composables inside `PulseApp` automatically respond to `refresh()`:

```kotlin
PulseApp(container = appContainer) { onRefresh, onBroadcast ->
    // onRefresh() calls container.refresh()
    // onBroadcast(b) calls container.broadcast(b)

    Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
        Text("Sync All")
    }
    Button(onClick = { onRefresh() }) {
        Text("Refresh View")
    }
}
```
