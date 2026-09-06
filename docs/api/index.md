# API Overview

PulseMVI exposes a small, focused API surface. The core artifact lives in `jp.kaleidot725.pulse.mvi`; `rememberPulseViewModel`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators` come from `pulsemvi-navigation3`, in `jp.kaleidot725.pulse.mvi.navigation3`.

## Classes

| Class | Description |
|---|---|
| [`PulseViewModel`](/api/pulse-viewmodel) | Abstract base class for managing UI state |
| [`PulseContainer`](/api/pulse-container) | Coordinates multiple ViewModels |

## Composables

| Composable | Description |
|---|---|
| [`PulseHost`](/api/composables#pulsehost) | Scopes a Container to a subtree; enables refresh and broadcast callbacks |
| [`PulseContent`](/api/composables#pulsecontent) | Observes a ViewModel; provides state and action dispatcher |
| [`rememberPulseViewModel`](/api/composables#rememberpulseviewmodel) | ViewModel scoped to a `ViewModelStoreOwner` — `pulsemvi-navigation3` |
| [`rememberPulseContainer`](/api/composables#rememberpulsecontainer) | Container scoped to a `ViewModelStoreOwner` — `pulsemvi-navigation3` |
| [`rememberPulseNavEntryDecorators`](/api/composables#rememberpulsenaventrydecorators) | `NavDisplay` decorators that scope ViewModels to a back stack entry — `pulsemvi-navigation3` |

## Marker Interfaces

| Interface | Description |
|---|---|
| [`PulseState`](/api/interfaces#pulsestate) | Marks a class as a ViewModel's UI state |
| [`PulseAction`](/api/interfaces#pulseaction) | Marks a class as a user action |
| [`PulseEvent`](/api/interfaces#pulseevent) | Marks a class as a one-time side effect |
| [`PulseBroadcast`](/api/interfaces#pulsebroadcast) | Marks a class as a Container broadcast message |
| [`PulseUnicast`](/api/interfaces#pulseunicast) | Marks a class as a child-to-parent unicast message |
