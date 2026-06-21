# Architecture

PulseMVI follows the MVI (Model-View-Intent) pattern and adds three coordination primitives: **Broadcast**, **Unicast**, and **View Refresh**.

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│                   Compose UI                        │
│                                                     │
│   User Interaction                                  │
│        │                                            │
│        ▼                                            │
│   onAction(action)  ──────────▶  PulseStore         │
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

When multiple Stores need to react to the same event, use `PulseContainer.broadcast()`:

```
Container.broadcast(MyBroadcast.Sync)
        │
        ├──▶ StoreA.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
        │
        └──▶ StoreB.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
```

## Unicast Flow

When a child Store needs to notify its parent Container, use `PulseStore.unicast()`:

```
StoreA.unicast(MyUnicast.SaveRequested)
        │
        └──▶ Container.onReceived(SaveRequested)
                  │
                  ├──▶ broadcast(...)
                  └──▶ refresh()
```

## View Refresh Flow

`Container.refresh()` forces the Compose view tree to reconstruct. Store states are **preserved** — only the Composables are re-created:

```
Container.refresh()
        │
        └──▶ PulseApp detects new key
                  │
                  └──▶ PulseContent's rendered subtree re-created (via `key()`)
                            │
                            └──▶ Store remains attached; onSetup() is not repeated
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `PulseState` | Immutable snapshot of UI data |
| `PulseAction` | User intent — what the user wants to do |
| `PulseEvent` | One-time side effect — navigation, dialog, snackbar |
| `PulseBroadcast` | Cross-Store notification from Container |
| `PulseUnicast` | Child-to-parent notification from Store |
| `PulseStore` | Owns state; handles actions and broadcasts; can emit unicasts |
| `PulseContainer` | Coordinates Stores; enables broadcast, unicast handling, and refresh |
| `PulseApp` | Compose wrapper that propagates container key |
| `PulseContent` | Compose wrapper that observes a Store |

## Lifecycle

```
PulseContent appears
        │
        └──▶ Store attached
                  │
                  └──▶ First active observer calls onSetup()
                                │
                                └──▶ coroutineScope active

PulseContent disappears
        │
        └──▶ Store detached
                  │
                  └──▶ Last active observer cancels + recreates coroutineScope
                                (Store state is preserved for reuse)
```

::: tip
`onSetup()` runs once for the first active `PulseContent`. It runs again after all observers leave composition and a new observer enters, such as when returning to a mobile screen. `refresh()` alone does not restart it.
:::
