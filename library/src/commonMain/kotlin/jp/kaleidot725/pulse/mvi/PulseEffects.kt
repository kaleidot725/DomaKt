package jp.kaleidot725.pulse.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Drives [store]'s lifecycle from the composition: [PulseStore.onSetup] when this composable enters,
 * [PulseStore.cancel] when it leaves.
 *
 * Use it when you create the Store yourself and nothing else owns its lifetime:
 *
 * ```kotlin
 * val store = remember { CounterStore(repository) }
 * PulseStoreEffect(store)
 *
 * PulseContent(store = store) { state, onAction -> ... }
 * ```
 *
 * The Store then lives exactly as long as this composition, so leaving and re-entering it repeats
 * `onSetup()` and an Android configuration change starts over. Depend on `pulsemvi-navigation3` and
 * use its `rememberPulseStore` instead when the Store should outlive the composition.
 */
@Composable
public fun PulseStoreEffect(store: PulseStore<*, *, *, *, *>) {
    DisposableEffect(store) {
        store.onSetup()
        onDispose { store.cancel() }
    }
}

/**
 * Closes [container] when this composable leaves the composition, stopping its Unicast collection.
 *
 * The counterpart of [PulseStoreEffect] for a Container you create yourself. A Container has no
 * setup step, so this only handles the teardown.
 */
@Composable
public fun PulseContainerEffect(container: PulseContainer<*, *>) {
    DisposableEffect(container) {
        onDispose { container.close() }
    }
}
