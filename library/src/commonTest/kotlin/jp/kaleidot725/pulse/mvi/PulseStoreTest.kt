package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.isActive
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
    fun setupIsNotRunUntilTheOwnerStartsIt() {
        val store = TestStore()

        assertEquals(0, store.setupCount)

        store.onSetup()

        assertEquals(1, store.setupCount)
        assertFalse(requireNotNull(store.setupJob).isCancelled)
    }

    @Test
    fun cancelStopsWorkStartedInSetup() {
        val store = TestStore()

        store.onSetup()
        val setupJob = requireNotNull(store.setupJob)
        store.cancel()

        assertTrue(setupJob.isCancelled)
    }

    @Test
    fun closeLeavesNoScopeToLaunchInto() {
        val store = TestStore()

        store.onSetup()
        val setupJob = requireNotNull(store.setupJob)
        store.close()

        assertTrue(setupJob.isCancelled)
        assertFalse(store.coroutineScope.isActive)
    }

    @Test
    fun stateIsPreservedAcrossSetups() {
        val store = TestStore()

        store.onSetup()
        store.update { copy(value = 42) }
        store.cancel()
        store.onSetup()

        assertEquals(TestState(value = 42), store.currentState)
        assertEquals(2, store.setupCount)
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
