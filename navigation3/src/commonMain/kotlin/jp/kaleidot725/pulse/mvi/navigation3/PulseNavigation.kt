package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

/**
 * Creates a [PulseViewModel] scoped to the current [ViewModelStoreOwner].
 *
 * The instance is kept in the owner's `ViewModelStore`, so a composition restart does not recreate
 * it: state is preserved and `onSetup()` is not repeated. Its scope is cancelled when the owner is
 * cleared.
 *
 * Which owner is in scope decides how long it lives. Under the host owner that is the whole screen;
 * under a Navigation 3 entry — see [rememberPulseNavEntryDecorators] — it is as long as the route
 * stays on the back stack.
 *
 * [PulseViewModel] is a `ViewModel`, so `viewModel()` and `koinViewModel()` create it just as well.
 * This adds a default [key] of the class's qualified name; pass an explicit one when the same type
 * is used more than once under a single owner.
 */
@Composable
public inline fun <reified VM : PulseViewModel<*, *, *, *, *>> rememberPulseViewModel(
    key: String? = null,
    noinline factory: () -> VM,
): VM =
    viewModel(
        viewModelStoreOwner = rememberPulseViewModelStoreOwner(),
        key = key ?: (VM::class.qualifiedName ?: VM::class.simpleName ?: "PulseViewModel"),
        factory = viewModelFactory { initializer { factory() } },
    )

/**
 * Creates a [PulseContainer] scoped to the current [ViewModelStoreOwner].
 *
 * Keeps the Container's Unicast subscriptions alive across recompositions and composition restarts;
 * `close()` runs when the owner is cleared.
 *
 * [key] defaults to the Container's qualified class name.
 */
@Composable
public inline fun <reified Container : PulseContainer<*, *>> rememberPulseContainer(
    key: String? = null,
    noinline factory: () -> Container,
): Container =
    viewModel(
        viewModelStoreOwner = rememberPulseViewModelStoreOwner(),
        key = key ?: (Container::class.qualifiedName ?: Container::class.simpleName ?: "PulseContainer"),
        factory = viewModelFactory { initializer { factory() } },
    )

/**
 * Returns the host [ViewModelStoreOwner].
 *
 * The Compose Desktop host provides one. Embedding Compose somewhere that does not means there is
 * nothing to own a lifetime, so this fails rather than inventing an owner.
 */
@PublishedApi
@Composable
internal fun rememberPulseViewModelStoreOwner(): ViewModelStoreOwner =
    checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner in scope. Provide one with " +
            "CompositionLocalProvider(LocalViewModelStoreOwner provides owner), or drive the " +
            "PulseViewModel lifecycle yourself with the pulsemvi artifact alone."
    }

/**
 * The [NavEntryDecorator] list `NavDisplay` needs for PulseMVI ViewModels to be scoped to a back
 * stack entry.
 *
 * `NavDisplay` defaults `entryDecorators` to the saveable state holder alone, so passing the
 * ViewModel decorator on its own would drop saveable state. This keeps both:
 *
 * ```kotlin
 * NavDisplay(
 *     backStack = backStack,
 *     entryDecorators = rememberPulseNavEntryDecorators(),
 *     entryProvider = entryProvider { ... },
 * )
 * ```
 *
 * A [PulseViewModel] created with [rememberPulseViewModel] inside a destination then lives exactly
 * as long as its route stays on the back stack.
 */
@Composable
public fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>> =
    listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    )
