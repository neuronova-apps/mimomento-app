package com.neuronovaapps.mimomento.ui.progress

import com.neuronovaapps.mimomento.data.local.ProgressStorage
import com.neuronovaapps.mimomento.data.model.ProgressEvent
import com.neuronovaapps.mimomento.data.model.ProgressEventType
import com.neuronovaapps.mimomento.data.repository.LocalProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressViewModelTest {

    private class FakeProgressStorage(
        initialEvents: List<ProgressEvent> = emptyList(),
    ) : ProgressStorage {
        var events: MutableList<ProgressEvent> = initialEvents.toMutableList()

        override suspend fun readEvents(): List<ProgressEvent> = events.toList()
        override suspend fun writeEvents(events: List<ProgressEvent>) {
            this.events = events.toMutableList()
        }
    }

    @Test
    fun initialState_whenNoData_emitsReadyWithEmptyState() = runBlocking {
        val storage = FakeProgressStorage()
        val repository = LocalProgressRepository(storage)
        val viewModel = ProgressViewModel(repository, dispatcher = Dispatchers.Default)

        val state = viewModel.uiState.first { it is ProgressUiState.Ready } as ProgressUiState.Ready
        assertTrue(state.isEmpty)
        assertEquals(0, state.summary.totalActiveDays)
    }

    @Test
    fun whenEventsRecorded_emitsReadyWithPopulatedSummary() = runBlocking {
        val storage = FakeProgressStorage()
        val repository = LocalProgressRepository(storage)
        repository.recordEvent(ProgressEventType.DEVOTIONAL_OPENED, "dev-1")
        repository.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-1")

        val viewModel = ProgressViewModel(repository, dispatcher = Dispatchers.Default)

        val state = viewModel.uiState.first {
            it is ProgressUiState.Ready && !it.isEmpty
        } as ProgressUiState.Ready

        assertFalse(state.isEmpty)
        assertEquals(1, state.summary.devotionalsOpenedCount)
        assertEquals(1, state.summary.devotionalsCompletedCount)
        assertEquals(1, state.summary.totalActiveDays)
    }
}
