package jp.kaleidot725.doma.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
public fun <Broadcast : DomaBroadcast> DomaContainer(
    storeContainer: DomaStoreContainer<Broadcast>,
    content: @Composable ((Broadcast) -> Unit) -> Unit = { _ -> },
) {
    val onBroadcast = storeContainer::onBroadcast
    content(onBroadcast)
}

@Composable
public fun <State : DomaState, Action: DomaAction, Event : DomaEvent, Broadcast : DomaBroadcast> DomaContent(
    store: DomaStore<State,Action, Event, Broadcast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val state by store.state.collectAsState()
    val onAction = store::onAction
    LaunchedEffect(store) { store.event.collect { onEvent(it) } }
    content(state, onAction)
}
