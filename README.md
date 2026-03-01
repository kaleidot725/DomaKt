# Doma

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/kaleidot725/Doma.svg)](https://jitpack.io/#kaleidot725/Doma)

A lightweight MVI (Model-View-Intent) library for Compose Multiplatform, providing a clean coroutine-based architecture for building reactive UIs.

## Features

- 🏗️ **MVI Architecture** - Clear separation of State, Action, Event, and Telegram
- 🔄 **Platform & Store** - Two complementary components for coordinating UI logic
- 📡 **Telegram Broadcasting** - Type-safe messaging from Platform to Stores
- ⚡ **Coroutine-Based** - Built on Kotlin Coroutines and StateFlow
- 🎨 **Compose Integration** - Ready-to-use Composable helpers for Platform and Store

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
    implementation("com.github.kaleidot725:Doma:Tag")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.kaleidot725:Doma:Tag'
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
    <artifactId>Doma</artifactId>
    <version>Tag</version>
</dependency>
```

> **Note**: Replace `Tag` with the desired version tag (e.g., `v1.0.0`) or a specific commit hash.

## Architecture

Doma provides two complementary components:

- **DomaPlatform** — Handles user actions and coordinates multiple Stores. Broadcasts `DomaTelegram` messages to notify Stores of changes.
- **DomaStore** — Manages UI state for a specific part of the screen. Receives telegrams from the Platform and updates its own state.

```
User Action
    │
    ▼
DomaPlatform.onAction()
    │
    ├─── broadcast(telegram) ──▶ DomaStore.onReceive(telegram)
    │                                    │
    │                              update { ... }
    │                                    │
    └─────────────────────────────── UI re-renders
```

## Quick Start

### 1. Define State, Action, Event, and Telegram

```kotlin
// State: the UI state managed by Store
data class CounterState(val count: Int = 0) : DomaState

// Action: user intents sent to Platform
sealed class CounterAction : DomaAction {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data object Reset : CounterAction()
}

// Event: one-time side effects emitted from Store
sealed class CounterEvent : DomaEvent {
    data class ShowMessage(val message: String) : CounterEvent()
}

// Telegram: messages broadcast from Platform to Stores
sealed class CounterTelegram : DomaTelegram {
    data object Incremented : CounterTelegram()
    data object Decremented : CounterTelegram()
    data object Reset : CounterTelegram()
}
```

### 2. Create a Store

`DomaStore` manages the UI state. Override `onSetup` to initialize subscriptions, and `onReceive` to react to telegrams from the Platform.

```kotlin
class CounterStore(
    private val repository: CounterRepository,
) : DomaStore<CounterState, CounterAction, CounterEvent, CounterTelegram>(
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

    override fun onReceive(telegram: CounterTelegram) {
        // React to telegrams broadcast from Platform
    }

    override fun onAction(uiAction: CounterAction) {
        // No-op: actions are handled by the Platform
    }
}
```

### 3. Create a Platform

`DomaPlatform` receives user actions and coordinates Stores via `broadcast`.

```kotlin
class CounterPlatform(
    stores: List<DomaStore<*, *, *, CounterTelegram>>,
    private val repository: CounterRepository,
) : DomaPlatform<CounterState, CounterAction, CounterEvent, CounterTelegram>(
    stores = stores,
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> {
                repository.increment()
                broadcast(CounterTelegram.Incremented)
            }
            CounterAction.Decrement -> {
                repository.decrement()
                broadcast(CounterTelegram.Decremented)
            }
            CounterAction.Reset -> {
                repository.reset()
                broadcast(CounterTelegram.Reset)
            }
        }
    }
}
```

### 4. Connect to Compose UI

Use `DomaPlatformContent` to manage the Platform lifecycle, and `DomaStoreContent` to observe Store state.

```kotlin
@Composable
fun CounterScreen() {
    val repository = remember { CounterRepository() }
    val store = remember { CounterStore(repository) }
    val platform = remember {
        CounterPlatform(stores = listOf(store), repository = repository)
    }

    DomaPlatformContent(platforms = platform) { _, onAction ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            DomaStoreContent(store = store) { state, _ ->
                Text(
                    text = "${state.count}",
                    fontSize = 72.sp,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onAction(CounterAction.Decrement) }) { Text("-") }
                Button(onClick = { onAction(CounterAction.Increment) }) { Text("+") }
            }

            Button(onClick = { onAction(CounterAction.Reset) }) { Text("Reset") }
        }
    }
}
```

## API Reference

### DomaStore

Base class for managing UI state within a specific screen component.

| Member | Description |
|---|---|
| `state: StateFlow<UiState>` | The current UI state as a StateFlow |
| `currentState: UiState` | Snapshot of the current UI state |
| `event: Flow<Event>` | Stream of one-time side effects |
| `coroutineScope` | CoroutineScope tied to the Store's lifecycle |
| `onSetup()` | Called when the Store is first subscribed to |
| `onReceive(telegram)` | Called when the Platform broadcasts a telegram |
| `onAction(uiAction)` | Called when a user action is dispatched |
| `update { }` | Updates the UI state |
| `event(effect)` | Emits a one-time side effect |

### DomaPlatform

Base class for handling user actions and coordinating multiple Stores.

| Member | Description |
|---|---|
| `state: StateFlow<UiState>` | The current UI state as a StateFlow |
| `currentState: UiState` | Snapshot of the current UI state |
| `event: Flow<Event>` | Stream of one-time side effects |
| `coroutineScope` | CoroutineScope tied to the Platform's lifecycle |
| `onSetup()` | Called when the Platform is first subscribed to |
| `onReset()` | Called when the Platform is disposed |
| `onAction(uiAction)` | Called when a user action is dispatched |
| `broadcast(telegram)` | Broadcasts a telegram to all registered Stores |
| `update { }` | Updates the UI state |
| `event(effect)` | Emits a one-time side effect |

### Composable Helpers

#### DomaPlatformContent

Manages the lifecycle of a `DomaPlatform` and provides state and action dispatcher to the content block.

```kotlin
DomaPlatformContent(
    platforms = myPlatform,
    onEvent = { event -> /* handle side effects */ },
) { state, onAction ->
    // Compose UI
}
```

#### DomaStoreContent

Observes a `DomaStore` and provides state and action dispatcher to the content block.

```kotlin
DomaStoreContent(
    store = myStore,
    onEvent = { event -> /* handle side effects */ },
) { state, onAction ->
    // Compose UI
}
```

### DomaTelegram

Marker interface for type-safe messages broadcast from `DomaPlatform` to registered `DomaStore` instances.

```kotlin
sealed class MyTelegram : DomaTelegram {
    data object Updated : MyTelegram()
    data class DataChanged(val value: Int) : MyTelegram()
}
```

## Example Application

See the [`demo`](demo/) module for a complete counter application demonstrating Platform, Store, and Telegram in action.

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
