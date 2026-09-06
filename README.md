# PulseMVI

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/kaleidot725/PulseMVI.svg)](https://jitpack.io/#kaleidot725/PulseMVI)

A lightweight MVI library for **Compose Desktop**.
Designed for Desktop's multi-Composable layouts, PulseMVI adds **Broadcast** to notify all ViewModels simultaneously, **Unicast** to send child ViewModel messages up to the Container, and **View Refresh** to reconstruct the view tree on demand.

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
- Compose Multiplatform project

## Installation

### JitPack (Recommended)

Add the JitPack repository to your build configuration:

#### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.kaleidot725:PulseMVI:Tag")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.kaleidot725:PulseMVI:Tag'
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
    <artifactId>PulseMVI</artifactId>
    <version>Tag</version>
</dependency>
```

> **Note**: Replace `Tag` with the desired version tag (e.g., `v1.0.0`) or a specific commit hash.

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

Instantiate ViewModels in the entry point, then use `PulseApp` for layout and `PulseContent` inside it to observe each ViewModel. `PulseContent` automatically responds to `refresh()` when nested inside `PulseApp`.

**Entry point** — create ViewModels once and pass them down:

```kotlin
fun main() = application {
    val repository = remember { CounterRepository() }
    val viewModel = remember { CounterViewModel(repository) }
    val container = remember { CounterContainer(viewModels = listOf(viewModel)) }

    Window(onCloseRequest = ::exitApplication, title = "Counter") {
        MaterialTheme {
            CounterApp(container = container, viewModel = viewModel)
        }
    }
}
```

**App composable** — wrap with `PulseApp` and expose refresh/broadcast controls:

```kotlin
@Composable
fun CounterApp(container: CounterContainer, viewModel: CounterViewModel) {
    PulseApp(container = container) { onRefresh, onBroadcast ->
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
| `onSetup()` | Called when the ViewModel is first subscribed to |
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

#### PulseApp

Manages a `PulseContainer` and provides `onRefresh` and `onBroadcast` callbacks to the content block. `PulseContent` placed inside automatically responds to `refresh()`.

```kotlin
PulseApp(container = myContainer) { onRefresh, onBroadcast ->
    // Compose UI
    PulseContent(viewModel = myViewModel) { state, onAction ->
        // Compose UI
    }
}
```

#### PulseContent

Observes a `PulseViewModel` and provides state and action dispatcher to the content block. Automatically cancels the ViewModel's coroutine scope when removed from composition.

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

See the [`demo`](demo/) module for a complete counter application demonstrating ViewModel, Container, Broadcast, and Unicast in action.

Run the demo:

```bash
./gradlew :demo:run
```

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
./gradlew :library:publishToMavenLocal
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
