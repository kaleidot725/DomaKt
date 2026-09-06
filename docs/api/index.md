# API Overview

PulseMVI exposes a small, focused API surface. Everything you need is in the `jp.kaleidot725.pulse.mvi` package.

## Classes

| Class | Description |
|---|---|
| [`PulseViewModel`](/api/pulse-viewmodel) | Abstract base class for managing UI state |
| [`PulseContainer`](/api/pulse-container) | Coordinates multiple ViewModels |

## Composables

| Composable | Description |
|---|---|
| [`PulseApp`](/api/composables#pulseapp) | Wraps a Container; enables refresh and broadcast callbacks |
| [`PulseContent`](/api/composables#pulsecontent) | Observes a ViewModel; provides state and action dispatcher |

## Marker Interfaces

| Interface | Description |
|---|---|
| [`PulseState`](/api/interfaces#pulsestate) | Marks a class as a ViewModel's UI state |
| [`PulseAction`](/api/interfaces#pulseaction) | Marks a class as a user action |
| [`PulseEvent`](/api/interfaces#pulseevent) | Marks a class as a one-time side effect |
| [`PulseBroadcast`](/api/interfaces#pulsebroadcast) | Marks a class as a Container broadcast message |
| [`PulseUnicast`](/api/interfaces#pulseunicast) | Marks a class as a child-to-parent unicast message |
