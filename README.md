# PulseMVI

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/kaleidot725/PulseMVI.svg)](https://jitpack.io/#kaleidot725/PulseMVI)

A lightweight MVI library for **Compose Desktop**.
PulseMVI adds **Broadcast** to notify all ViewModels simultaneously, **Unicast** to send child ViewModel messages up to the Container, and **View Refresh** to reconstruct the view tree on demand.

![demo](docs/demo.png)

## Features

- 🏗️ **MVI Architecture** - Clear separation of State, Action, Event, Broadcast, and Unicast
- 🔄 **ViewModel & Container** - ViewModel manages state autonomously; Container coordinates multiple ViewModels
- 📡 **Broadcast** - Type-safe messages delivered from Container to all registered ViewModels simultaneously
- ⬆️ **Unicast** - Optional messages emitted from child ViewModels to the Container
- 🖥️ **View Refresh** - Forces the view tree to reconstruct on demand while preserving ViewModel state
- ⚡ **Coroutine-Based** - Built on Kotlin Coroutines and StateFlow
- 🎨 **Compose Integration** - Ready-to-use Composable helpers with automatic lifecycle management

## Requirements

- Java 17 or higher
- Kotlin 2.0 or higher
- A Compose Desktop (JVM) project

## Installation

### JitPack (Recommended)

Add the JitPack repository to your build configuration:

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.kaleidot725:pulsemvi:Tag")

    // Optional: owner scoped lifetimes and Navigation 3 back stack scoping
    implementation("com.github.kaleidot725:pulsemvi-navigation3:Tag")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.kaleidot725:pulsemvi:Tag'

    // Optional: owner scoped lifetimes and Navigation 3 back stack scoping
    implementation 'com.github.kaleidot725:pulsemvi-navigation3:Tag'
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.kaleidot725</groupId>
    <artifactId>pulsemvi</artifactId>
    <version>Tag</version>
</dependency>

<!-- Optional: owner scoped lifetimes and Navigation 3 back stack scoping -->
<dependency>
    <groupId>com.github.kaleidot725</groupId>
    <artifactId>pulsemvi-navigation3</artifactId>
    <version>Tag</version>
</dependency>
```

> **Note**: Replace `Tag` with the desired version tag (e.g., `v1.0.0`) or a specific commit hash.

### Artifacts

| Artifact | Contents |
|---|---|
| `pulsemvi` | `PulseState`, `PulseAction`, `PulseEvent`, `PulseBroadcast`, `PulseUnicast`, `PulseViewModel`, `PulseContainer`, `PulseHost`, `PulseContent`. Depends on the Compose runtime and coroutines only |
| `pulsemvi-navigation3` | `rememberPulseViewModel`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators`. Adds `androidx.lifecycle` and Navigation 3 |

`PulseViewModel` and `PulseContainer` extend `androidx.lifecycle.ViewModel`, so the core artifact
works with anything that creates one — `viewModel()`, `koinViewModel()`, or a plain `remember`.
`PulseContent` runs `onSetup()` through `setupOnce()` the first time it observes an instance, and
`onCleared()` cancels the scope, so the owning `ViewModelStore` decides the lifetime:

```kotlin
val viewModel = viewModel { CounterViewModel(repository) }

PulseContent(viewModel = viewModel) { state, onAction ->
    // Compose UI
}
```

Add `pulsemvi-navigation3` for `rememberPulseViewModel`, which defaults the key to the qualified
class name, and for `rememberPulseNavEntryDecorators()`, which scopes an instance to a Navigation 3
back stack entry.

## Architecture

PulseMVI provides two complementary components:

- **PulseViewModel** — Manages UI state for a specific screen component. Handles user actions directly, reacts to broadcasts from the Container, and can emit `PulseUnicast` messages up to the Container.
- **PulseContainer** — Coordinates multiple ViewModels. Delivers typed `PulseBroadcast` messages to all registered ViewModels, receives child unicasts, and can trigger a view refresh.

```
User Action
    │
    ▼
PulseViewModel.onAction()
    │
    └── update { ... } ──▶ UI re-renders

PulseContainer.broadcast(broadcast)      ← Notify all ViewModels simultaneously
    │
    └── PulseViewModel.onReceive(broadcast) ──▶ update { ... } ──▶ UI re-renders

PulseViewModel.unicast(unicast)          ← Notify parent Container
    │
    └── PulseContainer.onReceived(unicast) ──▶ broadcast(...) / refresh(...) / app logic

PulseContainer.refresh()                 ← Reconstruct the view tree
    │
    └── View reconstructs (ViewModel state is preserved)
```

## Quick Start

### 1. Define State, Action, Event, Broadcast, and Unicast

```kotlin
// State: the UI state managed by ViewModel
data class CounterState(val count: Int = 0) : PulseState

// Action: user intents dispatched directly to ViewModel
sealed class CounterAction : PulseAction {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data object Reset : CounterAction()
}

// Event: one-time side effects emitted from ViewModel
sealed class CounterEvent : PulseEvent {
    data class ShowMessage(val message: String) : CounterEvent()
}

// Broadcast: messages delivered from Container to all ViewModels
sealed class CounterBroadcast : PulseBroadcast {
    data object Refresh : CounterBroadcast()
    data object ResetNotified : CounterBroadcast()
}

// Unicast: messages emitted from child ViewModel to Container
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}
```

### 2. Create a ViewModel

`PulseViewModel` manages its own UI state and handles user actions. Override `onSetup` to initialize subscriptions, `onAction` to handle user intents, and `onReceive` to react to broadcasts.

Both `PulseViewModel` and `PulseContainer` accept an optional `coroutineDispatcher` constructor argument. It defaults to `Dispatchers.Default`, and tests can pass a test dispatcher.

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
                if (count != 0 && count % 10 == 0) {
                    event(CounterEvent.ShowMessage("$count reached!"))
                }
            }
        }
    }

    override fun onAction(uiAction: CounterAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterAction.Increment -> repository.increment()
                CounterAction.Decrement -> repository.decrement()
                CounterAction.Reset -> {
                    repository.reset()
                    unicast(CounterUnicast.ResetRequested)
                }
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            is CounterBroadcast.Refresh -> event(CounterEvent.ShowMessage("Refreshed!"))
            is CounterBroadcast.ResetNotified -> event(CounterEvent.ShowMessage("Parent received reset"))
        }
    }
}
```

### 3. Create a Container

`PulseContainer` coordinates multiple ViewModels. Use `broadcast` to send a typed message to all registered ViewModels, and `refresh` to reconstruct the view.

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: CounterUnicast) {
        when (unicast) {
            CounterUnicast.ResetRequested -> broadcast(CounterBroadcast.ResetNotified)
        }
    }
}
```

### 4. Connect to Compose UI

Instantiate ViewModels in the entry point, then use `PulseHost` for layout and `PulseContent` inside it to observe each ViewModel. `PulseContent` automatically responds to `refresh()` when nested inside `PulseHost`.

**Entry point** — create ViewModels once and pass them down:

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Counter") {
        MaterialTheme {
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

            CounterScreen(container = container, viewModel = viewModel)
        }
    }
}
```

**Screen composable** — wrap with `PulseHost` and expose refresh/broadcast controls:

```kotlin
@Composable
fun CounterScreen(container: CounterContainer, viewModel: CounterViewModel) {
    PulseHost(container = container) { onRefresh, onBroadcast ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                Button(onClick = { onRefresh() }) { Text("Refresh View") }
                Button(onClick = { onBroadcast(CounterBroadcast.Refresh) }) { Text("Send Broadcast") }
            }
            CounterContent(viewModel = viewModel, modifier = Modifier.align(Alignment.Center))
        }
    }
}
```

**Content composable** — use `PulseContent` to observe a ViewModel and handle events:

```kotlin
@Composable
fun CounterContent(viewModel: CounterViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box {
        PulseContent(
            viewModel = viewModel,
            onEvent = { event ->
                when (event) {
                    is CounterEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            },
        ) { state, onAction ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${state.count}", fontSize = 72.sp)
                Row {
                    Button(onClick = { onAction(CounterAction.Decrement) }) { Text("−") }
                    Button(onClick = { onAction(CounterAction.Increment) }) { Text("+") }
                }
                OutlinedButton(onClick = { onAction(CounterAction.Reset) }) { Text("Reset") }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}
```

## API Reference

### PulseViewModel

Base class for managing UI state within a specific screen component.

| Member | Description |
|---|---|
| `state: StateFlow<UiState>` | The current UI state as a StateFlow |
| `currentState: UiState` | Snapshot of the current UI state |
| `event: Flow<Event>` | Stream of one-time side effects |
| `coroutineScope` | CoroutineScope tied to the ViewModel's lifecycle |
| `onSetup()` | Called once, by `PulseContent`, the first time it observes the ViewModel |
| `onAction(uiAction)` | Called when a user action is dispatched |
| `onReceive(broadcast)` | Called when the Container broadcasts a message |
| `unicast(unicast)` | Emits a child-to-parent message |
| `update { }` | Updates the UI state |
| `event(effect)` | Emits a one-time side effect |
| `cancel()` | Cancels the coroutine scope and prepares the ViewModel for reuse |

### PulseContainer

Base class for coordinating multiple ViewModels.

| Member | Description |
|---|---|
| `broadcast(broadcast)` | Delivers a broadcast message to all registered ViewModels |
| `onReceived(unicast)` | Called when a child ViewModel emits an unicast |
| `refresh()` | Reconstructs the view while preserving ViewModel state |

### Composable Helpers

#### rememberPulseViewModel / rememberPulseContainer

Creates a ViewModel or a Container held in the `ViewModelStore` of the current
`ViewModelStoreOwner`. A composition restart then reuses the instance rather than rebuilding it:
state is preserved and `onSetup()` is not repeated. The ViewModel scope is cancelled, and
`PulseContainer.close()` is called, only once the owner is cleared.

```kotlin
@Composable
fun CounterScreen() {
    val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    PulseHost(container = container) { _, _ ->
        PulseContent(viewModel = viewModel) { state, onAction ->
            // Compose UI
        }
    }
}
```

The key defaults to the class name. Pass an explicit `key` when the same type is used more than once
under a single owner:

```kotlin
val left = rememberPulseViewModel(key = "left") { CounterViewModel(leftRepository) }
val right = rememberPulseViewModel(key = "right") { CounterViewModel(rightRepository) }
```

Internally both read `LocalViewModelStoreOwner.current` and keep a `ViewModel` in that owner's
`ViewModelStore`. **Whatever owner is in scope at the call site decides how long the ViewModel lives**,
so anything that changes that owner changes the lifetime:

- Under the host owner, the ViewModel lives as long as the screen
- Under a Navigation 3 entry — pass `rememberPulseNavEntryDecorators()` as `NavDisplay`'s
  `entryDecorators` and create the ViewModel inside the destination — it lives as long as the route stays
  on the back stack, and is cancelled when the route is popped
- Under an owner you provide with `CompositionLocalProvider(LocalViewModelStoreOwner provides ...)`,
  it lives as long as you keep that owner

Nothing removes a ViewModel from its owner before the owner is cleared, so creating ViewModels under a long
lived owner accumulates them. Scope them to a narrower owner when a screen creates ViewModels it will not
need again.

The Compose Desktop `Window` provides a `ViewModelStoreOwner`. Embedding Compose somewhere that
does not means nothing owns a lifetime, and `rememberPulseViewModel` says so rather than inventing an
owner: provide one with `CompositionLocalProvider(LocalViewModelStoreOwner provides owner)`, or
create the instance yourself and let the composition hold it.

> **Note**: `rememberPulseViewModel` does not restore state after the process exits. Use
> `rememberSaveable` for anything that has to outlive it.

#### PulseHost

Manages a `PulseContainer` and provides `onRefresh` and `onBroadcast` callbacks to the content block. `PulseContent` placed inside automatically responds to `refresh()`.

```kotlin
PulseHost(container = myContainer) { onRefresh, onBroadcast ->
    // Compose UI
    PulseContent(viewModel = myViewModel) { state, onAction ->
        // Compose UI
    }
}
```

#### PulseContent

Observes a `PulseViewModel` and provides state and action dispatcher to the content block.

`PulseContent` only observes — it never starts or cancels the ViewModel. The setup lifecycle belongs to `rememberPulseViewModel`: `onSetup()` runs once when the ViewModel is created, and the scope is cancelled when the owning `ViewModelStoreOwner` is cleared. Leaving composition, including another Navigation 3 destination covering the route, therefore never repeats setup or loses ViewModel state.

With `rememberPulseNavEntryDecorators()` as `NavDisplay`'s `entryDecorators`, each destination gets its own owner, so a ViewModel created inside a destination lives exactly as long as its route stays on the back stack.

```kotlin
PulseContent(
    viewModel = myViewModel,
    onEvent = { event -> /* handle side effects */ },
) { state, onAction ->
    // Compose UI
}
```

### PulseBroadcast

Marker interface for type-safe messages delivered from `PulseContainer` to all registered `PulseViewModel` instances.

```kotlin
sealed class MyBroadcast : PulseBroadcast {
    data object Refresh : MyBroadcast()
    data class DataChanged(val value: Int) : MyBroadcast()
}
```

### PulseUnicast

Marker interface for type-safe messages emitted from a child `PulseViewModel` to its parent `PulseContainer`.

```kotlin
sealed interface MyUnicast : PulseUnicast {
    data object SaveRequested : MyUnicast
}
```

## Example Application

The [`demo`](demo/) module is a pulse grid: four areas sharing one Container, each unaware of the
others.

Run it:

```bash
./gradlew :demo:run
```

Tapping an area does not raise its own count. It sends the tap up as a Unicast, the Container
broadcasts it back to all four, and each area works out from the origin what the pulse is worth to
it — two points if it is the source, one if it shares an edge, nothing if it is the diagonal. One tap
therefore moves three counts by two different amounts. Colour deepens with the count and each area
flashes as the pulse reaches it.

That covers the whole vocabulary in one gesture:

1. **Unicast** carries the tap from an area up to the Container
2. **Broadcast** carries the pulse back down to every area, which decides for itself what it means
3. **Event** fires when an area passes twelve, and the screen shows a snackbar
4. **Refresh** rebuilds the cells while the counts stay put
5. **Per destination lifetimes** — "New Area" pushes another grid that starts at zero, and the one
   underneath is still there, untouched, when you come back

`demo/src/jvmTest` asserts each of those, including that a composition restart under a surviving
owner keeps the counts and does not repeat `onSetup()`.

## Building

Build the library:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

Publish to local Maven:

```bash
./gradlew :library:publishToMavenLocal :navigation3:publishToMavenLocal
```

## License

```
Copyright 2026 kaleidot725

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
