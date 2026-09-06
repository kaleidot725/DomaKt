# Architecture

PulseMVI follows the MVI (Model-View-Intent) pattern and adds three Desktop-specific primitives: **Broadcast**, **Unicast**, and **View Refresh**.

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│                   Compose UI                        │
│                                                     │
│   User Interaction                                  │
│        │                                            │
│        ▼                                            │
│   onAction(action)  ──────────▶  PulseViewModel         │
│                                      │              │
│                               onAction()            │
│                                      │              │
│                               update { }            │
│                                      │              │
│                            StateFlow<State>         │
│                                      │              │
│        ◀──────────────────────────── │              │
│   PulseContent re-renders            │              │
│                                      │              │
│                               event(effect)         │
│                                      │              │
│        ◀──────────── onEvent ──────── │              │
│   Handle side effect                                │
└─────────────────────────────────────────────────────┘
```

## Broadcast Flow

When multiple ViewModels need to react to the same event, use `PulseContainer.broadcast()`:

```
Container.broadcast(MyBroadcast.Sync)
        │
        ├──▶ ViewModelA.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
        │
        └──▶ ViewModelB.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
```

## Unicast Flow

When a child ViewModel needs to notify its parent Container, use `PulseViewModel.unicast()`:

```
ViewModelA.unicast(MyUnicast.SaveRequested)
        │
        └──▶ Container.onReceived(SaveRequested)
                  │
                  ├──▶ broadcast(...)
                  └──▶ refresh()
```

## View Refresh Flow

`Container.refresh()` forces the Compose view tree to reconstruct. ViewModel states are **preserved** — only the Composables are re-created:

```
Container.refresh()
        │
        └──▶ PulseApp detects new key
                  │
                  └──▶ PulseContent re-created (via `key()`)
                            │
                            └──▶ ViewModel.cancel() then ViewModel re-subscribes
                                      │
                                      └──▶ onSetup() called again
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `PulseState` | Immutable snapshot of UI data |
| `PulseAction` | User intent — what the user wants to do |
| `PulseEvent` | One-time side effect — navigation, dialog, snackbar |
| `PulseBroadcast` | Cross-ViewModel notification from Container |
| `PulseUnicast` | Child-to-parent notification from ViewModel |
| `PulseViewModel` | Owns state; handles actions and broadcasts; can emit unicasts |
| `PulseContainer` | Coordinates ViewModels; enables broadcast, unicast handling, and refresh |
| `PulseApp` | Compose wrapper that propagates container key |
| `PulseContent` | Compose wrapper that observes a ViewModel |

## Lifecycle

```
PulseContent appears
        │
        └──▶ ViewModel.state subscribed  ──▶  onSetup() called
                                               │
                                        coroutineScope active

PulseContent disappears
        │
        └──▶ ViewModel.cancel() called
                  │
                  └──▶ coroutineScope cancelled + recreated
                            (ViewModel is ready to be reused)
```

::: tip
`onSetup()` is called every time the ViewModel is first subscribed to — including after a `refresh()`. Use it to start your data-collection coroutines.
:::
