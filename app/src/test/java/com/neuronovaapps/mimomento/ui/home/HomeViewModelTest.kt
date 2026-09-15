package com.neuronovaapps.mimomento.ui.home

import com.neuronovaapps.mimomento.data.local.JournalStorage
import com.neuronovaapps.mimomento.data.local.ProgressStorage
import com.neuronovaapps.mimomento.data.model.JournalEntry
import com.neuronovaapps.mimomento.data.model.JournalMood
import com.neuronovaapps.mimomento.data.model.ProgressEvent
import com.neuronovaapps.mimomento.data.model.ProgressEventType
import com.neuronovaapps.mimomento.data.repository.LocalJournalRepository
import com.neuronovaapps.mimomento.data.repository.LocalProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class HomeViewModelTest {

    private val testTimeZone: TimeZone = TimeZone.getTimeZone("UTC")

    private fun timeAt(
        year: Int,
        month: Int, // 1-based (1 = Jan, 12 = Dec)
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
    ): Long {
        val cal = Calendar.getInstance(testTimeZone).apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private class FakeJournalStorage(
        initialEntries: List<JournalEntry> = emptyList(),
    ) : JournalStorage {
        var entries = initialEntries.toMutableList()

        override suspend fun readEntries(): List<JournalEntry> = entries.toList()

        override suspend fun writeEntries(entries: List<JournalEntry>) {
            this.entries = entries.toMutableList()
        }
    }

    private class FakeProgressStorage(
        initialEvents: List<ProgressEvent> = emptyList(),
    ) : ProgressStorage {
        var events = initialEvents.toMutableList()

        override suspend fun readEvents(): List<ProgressEvent> = events.toList()

        override suspend fun writeEvents(events: List<ProgressEvent>) {
            this.events = events.toMutableList()
        }
    }

    private fun createTestHarness(
        initialEntries: List<JournalEntry> = emptyList(),
        initialEvents: List<ProgressEvent> = emptyList(),
        fixedNow: Long = timeAt(2026, 9, 15, 12, 0),
    ): Triple<HomeViewModel, LocalJournalRepository, LocalProgressRepository> {
        val journalStorage = FakeJournalStorage(initialEntries)
        val journalRepo = LocalJournalRepository(
            storage = journalStorage,
            timeProvider = { fixedNow },
        )

        val progressStorage = FakeProgressStorage(initialEvents)
        val progressRepo = LocalProgressRepository(
            storage = progressStorage,
            journalRepository = journalRepo,
            timeProvider = { fixedNow },
            timeZone = testTimeZone,
        )

        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = HomeViewModel(
            journalRepository = journalRepo,
            progressRepository = progressRepo,
            timeProvider = { fixedNow },
            timeZone = testTimeZone,
            dispatcher = Dispatchers.Unconfined,
            externalScope = testScope,
        )

        return Triple(viewModel, journalRepo, progressRepo)
    }

    @Test
    fun zeroEntriesToday_reportsZeroEntriesToday() = runBlocking {
        val (viewModel, _, _) = createTestHarness()

        val state = viewModel.uiState.first()
        assertEquals(0, state.journalEntriesToday)
    }

    @Test
    fun oneEntryToday_reportsOneEntryToday() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        val todayEntry = JournalEntry(
            id = "entry-1",
            text = "Reflexión de la mañana",
            mood = JournalMood.CALM,
            createdAt = timeAt(2026, 9, 15, 8, 30),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEntries = listOf(todayEntry),
            fixedNow = now,
        )

        val state = viewModel.uiState.first { it.journalEntriesToday == 1 }
        assertEquals(1, state.journalEntriesToday)
    }

    @Test
    fun multipleEntriesToday_reportsExactCount() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        val entries = listOf(
            JournalEntry("1", "Mañana", JournalMood.GOOD, timeAt(2026, 9, 15, 7, 0)),
            JournalEntry("2", "Tarde", JournalMood.CALM, timeAt(2026, 9, 15, 11, 0)),
            JournalEntry("3", "Noche", JournalMood.TIRED, timeAt(2026, 9, 15, 11, 45)),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEntries = entries,
            fixedNow = now,
        )

        val state = viewModel.uiState.first { it.journalEntriesToday == 3 }
        assertEquals(3, state.journalEntriesToday)
    }

    @Test
    fun entryCreatedYesterdayAndEditedToday_doesNotCountAsToday() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        // Creada el 14 de septiembre (ayer) y editada el 15 (hoy)
        val entry = JournalEntry(
            id = "entry-yesterday",
            text = "Reflexión de ayer editada hoy",
            mood = JournalMood.CHEERFUL,
            createdAt = timeAt(2026, 9, 14, 20, 0),
            updatedAt = timeAt(2026, 9, 15, 10, 0),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEntries = listOf(entry),
            fixedNow = now,
        )

        val state = viewModel.uiState.first()
        assertEquals(0, state.journalEntriesToday)
    }

    @Test
    fun entriesWithinAndOutsideWeeklyWindow_countsOnlyWithinWindow() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        // Ventana de 7 días: del 9 al 15 de septiembre inclusive
        val entries = listOf(
            JournalEntry("1", "Hoy", JournalMood.GOOD, timeAt(2026, 9, 15, 9, 0)),
            JournalEntry("2", "Hace 3 días", JournalMood.CALM, timeAt(2026, 9, 12, 14, 0)),
            JournalEntry("3", "Hace 6 días (límite ventana)", JournalMood.NEUTRAL, timeAt(2026, 9, 9, 1, 0)),
            // Fuera de la ventana semanal (hace 8 días)
            JournalEntry("4", "Fuera de semana", JournalMood.SAD, timeAt(2026, 9, 7, 10, 0)),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEntries = entries,
            fixedNow = now,
        )

        val state = viewModel.uiState.first { it.journalEntriesThisWeek == 3 }
        assertEquals(3, state.journalEntriesThisWeek)
        assertEquals(1, state.journalEntriesToday)
    }

    @Test
    fun emptyWeeklyActivity_reportsZeroMetrics() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        val (viewModel, _, _) = createTestHarness(fixedNow = now)

        val state = viewModel.uiState.first()
        assertEquals(0, state.activeDaysThisWeek)
        assertEquals(0, state.devotionalsThisWeek)
        assertEquals(0, state.journalEntriesThisWeek)
    }

    @Test
    fun weeklyActivityWithData_reportsActiveDaysFromProgressSummary() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        val events = listOf(
            ProgressEvent("e1", ProgressEventType.DEVOTIONAL_COMPLETED, timeAt(2026, 9, 15, 10, 0)),
            ProgressEvent("e2", ProgressEventType.PRAYER_OPENED, timeAt(2026, 9, 14, 10, 0)),
            ProgressEvent("e3", ProgressEventType.DEVOTIONAL_OPENED, timeAt(2026, 9, 13, 10, 0)),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEvents = events,
            fixedNow = now,
        )

        val state = viewModel.uiState.first { it.activeDaysThisWeek == 3 }
        assertEquals(3, state.activeDaysThisWeek)
    }

    @Test
    fun devotionalCompletedWithinWeek_isCountedInDevotionalsThisWeek() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        val events = listOf(
            // Apertura y finalización del mismo devocional: solo COMPLETED debe ser la métrica significativa
            ProgressEvent("e1", ProgressEventType.DEVOTIONAL_OPENED, timeAt(2026, 9, 14, 10, 0), "dev-101"),
            ProgressEvent("e2", ProgressEventType.DEVOTIONAL_COMPLETED, timeAt(2026, 9, 14, 10, 15), "dev-101"),
            // Segundo devocional completado hoy
            ProgressEvent("e3", ProgressEventType.DEVOTIONAL_COMPLETED, timeAt(2026, 9, 15, 9, 0), "dev-102"),
        )
        val (viewModel, _, _) = createTestHarness(
            initialEvents = events,
            fixedNow = now,
        )

        val state = viewModel.uiState.first { it.devotionalsThisWeek == 2 }
        assertEquals(2, state.devotionalsThisWeek)
    }

    @Test
    fun eventOutsideWeeklyWindow_isNotCountedInWeeklyMetrics() = runBlocking {
        val now = timeAt(2026, 9, 15, 12, 0)
        // Evento ocurrido el 7 de septiembre (hace 8 días, fuera de la ventana de 7 días 9..15)
        val outsideEvent = ProgressEvent(
            id = "e-old",
            type = ProgressEventType.DEVOTIONAL_COMPLETED,
            timestamp = timeAt(2026, 9, 7, 10, 0),
            referenceId = "dev-old",
        )
        val (viewModel, _, _) = createTestHarness(
            initialEvents = listOf(outsideEvent),
            fixedNow = now,
        )

        val state = viewModel.uiState.first()
        assertEquals(0, state.devotionalsThisWeek)
        assertEquals(0, state.activeDaysThisWeek)
    }

    @Test
    fun reactiveUpdate_whenJournalEntryIsCreated_updatesStateAutomatically() = runBlocking {
        var currentTime = timeAt(2026, 9, 15, 10, 0)
        val journalStorage = FakeJournalStorage()
        val journalRepo = LocalJournalRepository(
            storage = journalStorage,
            timeProvider = { currentTime },
        )
        val progressStorage = FakeProgressStorage()
        val progressRepo = LocalProgressRepository(
            storage = progressStorage,
            journalRepository = journalRepo,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = HomeViewModel(
            journalRepository = journalRepo,
            progressRepository = progressRepo,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
            dispatcher = Dispatchers.Unconfined,
            externalScope = testScope,
        )

        val initialState = viewModel.uiState.first()
        assertEquals(0, initialState.journalEntriesToday)
        assertEquals(0, initialState.journalEntriesThisWeek)

        // Crear una nueva entrada en el diario para hoy
        currentTime = timeAt(2026, 9, 15, 11, 0)
        journalRepo.createEntry("Nueva reflexión creada hoy", JournalMood.GOOD)

        val updatedState = viewModel.uiState.first { it.journalEntriesToday == 1 }
        assertEquals(1, updatedState.journalEntriesToday)
        assertEquals(1, updatedState.journalEntriesThisWeek)
    }

    @Test
    fun reactiveUpdate_whenProgressRecordsActivity_updatesWeeklyStateAutomatically() = runBlocking {
        var currentTime = timeAt(2026, 9, 15, 10, 0)
        val journalStorage = FakeJournalStorage()
        val journalRepo = LocalJournalRepository(
            storage = journalStorage,
            timeProvider = { currentTime },
        )
        val progressStorage = FakeProgressStorage()
        val progressRepo = LocalProgressRepository(
            storage = progressStorage,
            journalRepository = journalRepo,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = HomeViewModel(
            journalRepository = journalRepo,
            progressRepository = progressRepo,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
            dispatcher = Dispatchers.Unconfined,
            externalScope = testScope,
        )

        val initialState = viewModel.uiState.first()
        assertEquals(0, initialState.activeDaysThisWeek)
        assertEquals(0, initialState.devotionalsThisWeek)

        // Registrar finalización de devocional hoy
        progressRepo.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-test")

        val updatedState = viewModel.uiState.first { it.devotionalsThisWeek == 1 }
        assertEquals(1, updatedState.devotionalsThisWeek)
        assertEquals(1, updatedState.activeDaysThisWeek)
    }
}
