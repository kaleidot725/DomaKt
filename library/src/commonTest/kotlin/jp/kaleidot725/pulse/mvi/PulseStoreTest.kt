package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PulseStoreTest {
    @Test
    fun usesConfiguredCoroutineDispatcher() {
        val store = TestStore(coroutineDispatcher = Dispatchers.Unconfined)

        assertSame(Dispatchers.Unconfined, store.coroutineScope.coroutineContext[ContinuationInterceptor])
    }

    @Test
    fun setupFollowsPulseContentAttachments() {
        val store = TestStore()

        assertEquals(0, store.setupCount)

        store.attach()
        val firstSetupJob = requireNotNull(store.setupJob)
        store.attach()

        assertEquals(1, store.setupCount)
        store.detach()
        assertFalse(firstSetupJob.isCancelled)

        store.detach()
        assertTrue(firstSetupJob.isCancelled)

        store.attach()
        assertEquals(2, store.setupCount)
        store.detach()
    }

    @Test
    fun stateIsPreservedAcrossAttachments() {
        val store = TestStore()

        store.attach()
        store.update { copy(value = 42) }
        store.detach()
        store.attach()

        assertEquals(TestState(value = 42), store.currentState)
        store.detach()
    }
}

private data class TestState(
    val value: Int = 0,
) : PulseState

private data object TestAction : PulseAction

private data object TestEvent : PulseEvent

private data object TestBroadcast : PulseBroadcast

private data object TestUnicast : PulseUnicast

private class TestStore(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PulseStore<TestState, TestAction, TestEvent, TestBroadcast, TestUnicast>(
        initialUiState = TestState(),
        coroutineDispatcher = coroutineDispatcher,
    ) {
    var setupCount: Int = 0
    var setupJob: Job? = null

    override fun onSetup() {
        setupCount += 1
        setupJob = coroutineScope.launch { awaitCancellation() }
    }

    override fun onAction(uiAction: TestAction) = Unit
}
