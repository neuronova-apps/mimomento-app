package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.local.FileProgressStorage
import com.neuronova.mimomento.data.local.ProgressStorage
import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.JournalMood
import com.neuronova.mimomento.data.model.ProgressEvent
import com.neuronova.mimomento.data.model.ProgressEventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

class LocalProgressRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

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

    private class FakeJournalRepository(
        initialEntries: List<JournalEntry> = emptyList(),
    ) : JournalRepository {
        private val _entriesFlow = MutableStateFlow(initialEntries)
        override val entriesFlow: Flow<List<JournalEntry>> = _entriesFlow.asStateFlow()

        override suspend fun getEntries(): List<JournalEntry> = _entriesFlow.value
        override suspend fun getEntryById(id: String): JournalEntry? = _entriesFlow.value.firstOrNull { it.id == id }

        override suspend fun createEntry(text: String, mood: JournalMood?): Result<JournalEntry> {
            val entry = JournalEntry(
                id = UUID.randomUUID().toString(),
                text = text,
                mood = mood,
                createdAt = System.currentTimeMillis(),
            )
            _entriesFlow.value = listOf(entry) + _entriesFlow.value
            return Result.success(entry)
        }

        override suspend fun updateEntry(id: String, text: String, mood: JournalMood?): Result<JournalEntry> {
            val list = _entriesFlow.value.toMutableList()
            val index = list.indexOfFirst { it.id == id }
            if (index == -1) return Result.failure(NoSuchElementException())
            val updated = list[index].copy(text = text, mood = mood)
            list[index] = updated
            _entriesFlow.value = list
            return Result.success(updated)
        }

        override suspend fun deleteEntry(id: String): Result<Unit> {
            _entriesFlow.value = _entriesFlow.value.filterNot { it.id == id }
            return Result.success(Unit)
        }

        override suspend fun refresh() {}

        fun setEntries(entries: List<JournalEntry>) {
            _entriesFlow.value = entries
        }
    }

    private class FakeProgressStorage : ProgressStorage {
        var storedEvents = mutableListOf<ProgressEvent>()

        override suspend fun readEvents(): List<ProgressEvent> = storedEvents.toList()

        override suspend fun writeEvents(events: List<ProgressEvent>) {
            storedEvents = events.toMutableList()
        }
    }

    @Test
    fun recordEvent_persistsAndUpdatesFlow() = runBlocking {
        val storage = FakeProgressStorage()
        var fakeTime = 10_000L
        val repository = LocalProgressRepository(
            storage = storage,
            timeProvider = { fakeTime },
            timeZone = testTimeZone,
        )

        val result = repository.recordEvent(
            type = ProgressEventType.DEVOTIONAL_OPENED,
            referenceId = "dev-101",
        )

        assertTrue(result.isSuccess)
        val event = result.getOrThrow()
        assertEquals(ProgressEventType.DEVOTIONAL_OPENED, event.type)
        assertEquals("dev-101", event.referenceId)
        assertEquals(10_000L, event.timestamp)

        val eventsInFlow = repository.eventsFlow.first()
        assertEquals(1, eventsInFlow.size)
        assertEquals(event.id, eventsInFlow[0].id)
        assertEquals(1, storage.storedEvents.size)
    }

    @Test
    fun repositoryRestart_recoversStoredEvents() = runBlocking {
        val storageFile = tempFolder.newFile("restart_test.json")
        val storage = FileProgressStorage(storageFile)
        var currentTime = 1000L

        val repo1 = LocalProgressRepository(
            storage = storage,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )
        repo1.recordEvent(ProgressEventType.PRAYER_OPENED, "guide-1")
        currentTime = 2000L
        repo1.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-1")

        // Crear una nueva instancia del repositorio sobre el mismo almacenamiento
        val repo2 = LocalProgressRepository(
            storage = storage,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        val loaded = repo2.getEvents()
        assertEquals(2, loaded.size)
        assertEquals(ProgressEventType.PRAYER_OPENED, loaded[0].type)
        assertEquals(ProgressEventType.DEVOTIONAL_COMPLETED, loaded[1].type)
    }

    @Test
    fun multipleEventsOnSameDay_countAsSingleActiveDay() = runBlocking {
        val storage = FakeProgressStorage()
        val now = timeAt(2026, 9, 15, hour = 18)
        var currentTime = timeAt(2026, 9, 15, hour = 8)

        val repository = LocalProgressRepository(
            storage = storage,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        // Tres eventos ocurridos a diferentes horas del mismo día
        repository.recordEvent(ProgressEventType.DEVOTIONAL_OPENED, "dev-1")
        currentTime = timeAt(2026, 9, 15, hour = 12)
        repository.recordEvent(ProgressEventType.PRAYER_OPENED, "prayer-1")
        currentTime = timeAt(2026, 9, 15, hour = 16)
        repository.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-1")

        val summary = repository.getSummary()
        assertEquals(1, summary.totalActiveDays)
        assertEquals(3, summary.weeklyActivity.days.last().eventCount)
    }

    @Test
    fun distinctDaysWithEvents_areCountedCorrectly() = runBlocking {
        val storage = FakeProgressStorage()
        val now = timeAt(2026, 9, 15, hour = 12)
        var currentTime = timeAt(2026, 9, 10, hour = 10)

        val repository = LocalProgressRepository(
            storage = storage,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        repository.recordEvent(ProgressEventType.DEVOTIONAL_OPENED, "dev-1")
        currentTime = timeAt(2026, 9, 12, hour = 10)
        repository.recordEvent(ProgressEventType.PRAYER_OPENED, "prayer-1")
        currentTime = timeAt(2026, 9, 15, hour = 10)
        repository.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-2")

        val summary = repository.getSummary()
        assertEquals(3, summary.totalActiveDays)
    }

    @Test
    fun streak_whenActiveToday_countsConsecutiveDaysBackwards() {
        // Hoy: 15 de sep. Actividad: 13, 14 y 15 -> racha = 3
        val now = timeAt(2026, 9, 15, 12)
        val activeKeys = setOf(
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 15), testTimeZone),
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 14), testTimeZone),
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 13), testTimeZone),
        )

        val streak = LocalProgressRepository.calculateStreak(activeKeys, now, testTimeZone)
        assertEquals(3, streak)
    }

    @Test
    fun streak_whenInactiveTodayButActiveYesterday_preservesStreak() {
        // Hoy: 15 de sep (sin actividad). Ayer: 14 y antier 13 -> racha = 2
        val now = timeAt(2026, 9, 15, 12)
        val activeKeys = setOf(
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 14), testTimeZone),
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 13), testTimeZone),
        )

        val streak = LocalProgressRepository.calculateStreak(activeKeys, now, testTimeZone)
        assertEquals(2, streak)
    }

    @Test
    fun streak_whenInactiveTodayAndYesterday_returnsZero() {
        // Hoy: 15 de sep. Actividad el 13 de sep (ni hoy 15 ni ayer 14 hubo actividad) -> racha rota = 0
        val now = timeAt(2026, 9, 15, 12)
        val activeKeys = setOf(
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 13), testTimeZone),
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 12), testTimeZone),
        )

        val streak = LocalProgressRepository.calculateStreak(activeKeys, now, testTimeZone)
        assertEquals(0, streak)
    }

    @Test
    fun streak_whenGapExists_onlyCountsConsecutiveDays() {
        // Hoy: 15 de sep. Actividad: 15 y 13 (falta el 14) -> racha = 1
        val now = timeAt(2026, 9, 15, 12)
        val activeKeys = setOf(
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 15), testTimeZone),
            LocalProgressRepository.getCalendarDayKey(timeAt(2026, 9, 13), testTimeZone),
        )

        val streak = LocalProgressRepository.calculateStreak(activeKeys, now, testTimeZone)
        assertEquals(1, streak)
    }

    @Test
    fun weeklySummary_calculatesLast7DaysProperly() {
        val now = timeAt(2026, 9, 15, 12)
        val events = listOf(
            ProgressEvent("1", ProgressEventType.DEVOTIONAL_OPENED, timeAt(2026, 9, 15, 10)),
            ProgressEvent("2", ProgressEventType.PRAYER_OPENED, timeAt(2026, 9, 14, 10)),
            ProgressEvent("3", ProgressEventType.JOURNAL_CREATED, timeAt(2026, 9, 10, 10)),
            // Evento fuera de los últimos 7 días (día 15 - 8 = día 7)
            ProgressEvent("4", ProgressEventType.DEVOTIONAL_OPENED, timeAt(2026, 9, 7, 10)),
        )

        val weekly = LocalProgressRepository.calculateWeeklyActivity(
            events = events,
            journalEntries = emptyList(),
            nowMillis = now,
            timeZone = testTimeZone,
        )

        assertEquals(7, weekly.days.size)
        // Días activos dentro de los 7 días: día 10, día 14, día 15 -> 3
        assertEquals(3, weekly.activeDaysCount)
        assertTrue(weekly.days.last().hasDevotional) // Hoy (día 15)
        assertTrue(weekly.days[weekly.days.size - 2].hasPrayer) // Ayer (día 14)
    }

    @Test
    fun monthlySummary_countsCurrentMonthAndHandlesMonthBoundary() {
        val now = timeAt(2026, 9, 5, 12) // Septiembre 2026
        val events = listOf(
            ProgressEvent("1", ProgressEventType.DEVOTIONAL_OPENED, timeAt(2026, 9, 2, 10)),
            ProgressEvent("2", ProgressEventType.PRAYER_OPENED, timeAt(2026, 9, 4, 10)),
            ProgressEvent("3", ProgressEventType.JOURNAL_CREATED, timeAt(2026, 9, 4, 18)), // mismo día 4
            // Evento del mes anterior (Agosto)
            ProgressEvent("4", ProgressEventType.DEVOTIONAL_COMPLETED, timeAt(2026, 8, 31, 23)),
        )

        val monthly = LocalProgressRepository.calculateMonthlyActivity(
            events = events,
            journalEntries = emptyList(),
            nowMillis = now,
            timeZone = testTimeZone,
        )

        // En septiembre hubo 2 días activos (día 2 y día 4) con 3 eventos
        assertEquals(2, monthly.activeDaysCount)
        assertEquals(3, monthly.totalEventsCount)
    }

    @Test
    fun journalRepository_isSingleSourceOfTruthForJournalCount() = runBlocking {
        val storage = FakeProgressStorage()
        val journalRepo = FakeJournalRepository(
            initialEntries = listOf(
                JournalEntry("j-1", "Reflexión 1", null, timeAt(2026, 9, 10)),
                JournalEntry("j-2", "Reflexión 2", null, timeAt(2026, 9, 12)),
            ),
        )

        var currentTime = timeAt(2026, 9, 15, 10)
        val repository = LocalProgressRepository(
            storage = storage,
            journalRepository = journalRepo,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        // No hay eventos de JOURNAL_CREATED en storage todavía, pero existen 2 entradas en JournalRepository
        val summary1 = repository.getSummary()
        assertEquals(2, summary1.journalEntriesCount)

        // Simular que el usuario borra una entrada en JournalRepository
        journalRepo.deleteEntry("j-1")

        val summary2 = repository.getSummary()
        assertEquals(1, summary2.journalEntriesCount)
    }

    @Test
    fun devotionalAndPrayerEvents_countedAccurately() = runBlocking {
        val storage = FakeProgressStorage()
        var currentTime = timeAt(2026, 9, 15, 10)
        val repository = LocalProgressRepository(
            storage = storage,
            timeProvider = { currentTime },
            timeZone = testTimeZone,
        )

        repository.recordEvent(ProgressEventType.DEVOTIONAL_OPENED, "dev-1")
        repository.recordEvent(ProgressEventType.DEVOTIONAL_OPENED, "dev-2")
        repository.recordEvent(ProgressEventType.DEVOTIONAL_COMPLETED, "dev-1")
        repository.recordEvent(ProgressEventType.PRAYER_OPENED, "prayer-guide-1")
        repository.recordEvent(ProgressEventType.PRAYER_OPENED, "prayer-route-1")

        val summary = repository.getSummary()
        assertEquals(2, summary.devotionalsOpenedCount)
        assertEquals(1, summary.devotionalsCompletedCount)
        assertEquals(2, summary.prayersOpenedCount)
    }

    @Test
    fun emptyData_returnsEmptySummary() = runBlocking {
        val storage = FakeProgressStorage()
        val repository = LocalProgressRepository(
            storage = storage,
            timeZone = testTimeZone,
        )

        val summary = repository.getSummary()
        assertTrue(summary.isEmpty)
        assertEquals(0, summary.totalActiveDays)
        assertEquals(0, summary.journalEntriesCount)
        assertEquals(0, summary.devotionalsOpenedCount)
        assertEquals(0, summary.devotionalsCompletedCount)
        assertEquals(0, summary.prayersOpenedCount)
        assertEquals(0, summary.currentStreakDays)
        assertEquals(0, summary.weeklyActivity.activeDaysCount)
        assertEquals(0, summary.monthlyActivity.activeDaysCount)
    }
}
