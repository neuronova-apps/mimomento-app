package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.local.ProgressStorage
import com.neuronova.mimomento.data.model.DayActivity
import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.MonthlyActivitySummary
import com.neuronova.mimomento.data.model.ProgressEvent
import com.neuronova.mimomento.data.model.ProgressEventType
import com.neuronova.mimomento.data.model.ProgressSummary
import com.neuronova.mimomento.data.model.WeeklyActivitySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

/**
 * Repositorio de dominio para gestionar eventos cuantitativos y calcular métricas de Progreso.
 * Desacoplado de la UI y del mecanismo físico de persistencia.
 */
interface ProgressRepository {
    /**
     * Flujo reactivo con la lista completa de eventos registrados.
     */
    val eventsFlow: Flow<List<ProgressEvent>>

    /**
     * Registra un nuevo evento cuantitativo de actividad.
     *
     * @param type Tipo de actividad realizada.
     * @param referenceId Identificador anónimo opcional del recurso asociado.
     */
    suspend fun recordEvent(
        type: ProgressEventType,
        referenceId: String? = null,
    ): Result<ProgressEvent>

    /**
     * Obtiene la lista actual de eventos registrados.
     */
    suspend fun getEvents(): List<ProgressEvent>

    /**
     * Observa el resumen de progreso consolidado de forma reactiva.
     */
    fun observeSummary(): Flow<ProgressSummary>

    /**
     * Obtiene el resumen de progreso consolidado al momento actual.
     */
    suspend fun getSummary(): ProgressSummary
}

/**
 * Implementación local del repositorio de progreso respaldada por ProgressStorage y combinada con JournalRepository.
 */
class LocalProgressRepository(
    private val storage: ProgressStorage,
    private val journalRepository: JournalRepository? = null,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val timeZone: TimeZone = TimeZone.getDefault(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : ProgressRepository {

    private val mutex = Mutex()
    private val _eventsFlow = MutableStateFlow<List<ProgressEvent>>(emptyList())
    override val eventsFlow: Flow<List<ProgressEvent>> = _eventsFlow.asStateFlow()

    private var isInitialized = false

    private suspend fun ensureLoaded(): List<ProgressEvent> {
        if (!isInitialized) {
            val loaded = storage.readEvents()
            _eventsFlow.value = loaded.sortedBy { it.timestamp }
            isInitialized = true
        }
        return _eventsFlow.value
    }

    override suspend fun recordEvent(
        type: ProgressEventType,
        referenceId: String?,
    ): Result<ProgressEvent> = mutex.withLock {
        ensureLoaded()
        val event = ProgressEvent(
            id = idGenerator(),
            type = type,
            timestamp = timeProvider(),
            referenceId = referenceId?.trim()?.takeIf { it.isNotEmpty() },
        )

        val updated = (_eventsFlow.value + event).sortedBy { it.timestamp }
        try {
            storage.writeEvents(updated)
            _eventsFlow.value = updated
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEvents(): List<ProgressEvent> = mutex.withLock {
        ensureLoaded()
    }

    override fun observeSummary(): Flow<ProgressSummary> {
        val journalFlow = journalRepository?.entriesFlow ?: flowOf(emptyList())
        return combine(eventsFlow, journalFlow) { events, journalEntries ->
            calculateSummary(
                events = events,
                journalEntries = journalEntries,
                nowMillis = timeProvider(),
                timeZone = timeZone,
            )
        }
    }

    override suspend fun getSummary(): ProgressSummary = mutex.withLock {
        ensureLoaded()
        val journalEntries = journalRepository?.getEntries().orEmpty()
        calculateSummary(
            events = _eventsFlow.value,
            journalEntries = journalEntries,
            nowMillis = timeProvider(),
            timeZone = timeZone,
        )
    }

    companion object {
        /**
         * Calcula el resumen agregado a partir de los eventos y las entradas del diario.
         * Función pura para facilitar pruebas deterministas independientes del tiempo del sistema.
         */
        fun calculateSummary(
            events: List<ProgressEvent>,
            journalEntries: List<JournalEntry>,
            nowMillis: Long,
            timeZone: TimeZone = TimeZone.getDefault(),
        ): ProgressSummary {
            val devotionalsCompletedCount = events.count { it.type == ProgressEventType.DEVOTIONAL_COMPLETED }
            val devotionalsOpenedCount = events.count { it.type == ProgressEventType.DEVOTIONAL_OPENED }
            val prayersOpenedCount = events.count { it.type == ProgressEventType.PRAYER_OPENED }
            val journalEntriesCount = journalEntries.size

            val calendar = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
            }

            // Agrupar fechas únicas de actividad por día calendario
            val activeDayKeys = mutableSetOf<String>()

            events.forEach { event ->
                val dayKey = getCalendarDayKey(event.timestamp, timeZone)
                activeDayKeys.add(dayKey)
            }

            journalEntries.forEach { entry ->
                val dayKey = getCalendarDayKey(entry.createdAt, timeZone)
                activeDayKeys.add(dayKey)
            }

            val totalActiveDays = activeDayKeys.size

            // Cálculo de constancia / racha actual
            val currentStreakDays = calculateStreak(
                activeDayKeys = activeDayKeys,
                nowMillis = nowMillis,
                timeZone = timeZone,
            )

            // Resumen de los últimos 7 días
            val weeklyActivity = calculateWeeklyActivity(
                events = events,
                journalEntries = journalEntries,
                nowMillis = nowMillis,
                timeZone = timeZone,
            )

            // Resumen del mes actual
            val monthlyActivity = calculateMonthlyActivity(
                events = events,
                journalEntries = journalEntries,
                nowMillis = nowMillis,
                timeZone = timeZone,
            )

            return ProgressSummary(
                totalActiveDays = totalActiveDays,
                journalEntriesCount = journalEntriesCount,
                devotionalsCompletedCount = devotionalsCompletedCount,
                devotionalsOpenedCount = devotionalsOpenedCount,
                prayersOpenedCount = prayersOpenedCount,
                weeklyActivity = weeklyActivity,
                monthlyActivity = monthlyActivity,
                currentStreakDays = currentStreakDays,
            )
        }

        /**
         * Genera una clave única para el día calendario: "yyyy-DDD" en la zona horaria dada.
         */
        fun getCalendarDayKey(timestamp: Long, timeZone: TimeZone): String {
            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = timestamp
            }
            val year = cal.get(Calendar.YEAR)
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            return "$year-$dayOfYear"
        }

        /**
         * Obtiene la marca temporal correspondiente al inicio del día (00:00:00.000) de un Calendar dado.
         */
        private fun getStartOfDayMillis(cal: Calendar): Long {
            val clone = cal.clone() as Calendar
            clone.set(Calendar.HOUR_OF_DAY, 0)
            clone.set(Calendar.MINUTE, 0)
            clone.set(Calendar.SECOND, 0)
            clone.set(Calendar.MILLISECOND, 0)
            return clone.timeInMillis
        }

        /**
         * Regla de constancia:
         * 1. Contar días consecutivos con actividad.
         * 2. Si hoy tiene actividad, contar desde hoy hacia atrás.
         * 3. Si hoy no tiene actividad pero ayer sí, continuar la racha desde ayer hacia atrás.
         * 4. Si ni hoy ni ayer tienen actividad, constancia = 0.
         */
        fun calculateStreak(
            activeDayKeys: Set<String>,
            nowMillis: Long,
            timeZone: TimeZone,
        ): Int {
            if (activeDayKeys.isEmpty()) return 0

            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
            }

            val todayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"

            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"

            val (startCal, hasStartingPoint) = when {
                activeDayKeys.contains(todayKey) -> {
                    val c = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
                    Pair(c, true)
                }
                activeDayKeys.contains(yesterdayKey) -> {
                    val c = Calendar.getInstance(timeZone).apply {
                        timeInMillis = nowMillis
                        add(Calendar.DAY_OF_YEAR, -1)
                    }
                    Pair(c, true)
                }
                else -> Pair(cal, false)
            }

            if (!hasStartingPoint) return 0

            var streak = 0
            while (true) {
                val key = "${startCal.get(Calendar.YEAR)}-${startCal.get(Calendar.DAY_OF_YEAR)}"
                if (activeDayKeys.contains(key)) {
                    streak++
                    startCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
            return streak
        }

        /**
         * Calcula el resumen de los últimos 7 días terminando en el día actual (nowMillis).
         */
        fun calculateWeeklyActivity(
            events: List<ProgressEvent>,
            journalEntries: List<JournalEntry>,
            nowMillis: Long,
            timeZone: TimeZone,
        ): WeeklyActivitySummary {
            val days = mutableListOf<DayActivity>()
            var activeDaysCount = 0

            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
            }

            // Los 7 días van desde hoy - 6 hasta hoy (en orden cronológico ascendente)
            for (offset in 6 downTo 0) {
                val dayCal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = nowMillis
                    add(Calendar.DAY_OF_YEAR, -offset)
                }
                val dayStart = getStartOfDayMillis(dayCal)
                val dayEnd = dayStart + 86_400_000L - 1L
                val dayKey = "${dayCal.get(Calendar.YEAR)}-${dayCal.get(Calendar.DAY_OF_YEAR)}"

                val dayEvents = events.filter { it.timestamp in dayStart..dayEnd }
                val dayJournals = journalEntries.filter { it.createdAt in dayStart..dayEnd }

                val hasJournal = dayJournals.isNotEmpty() || dayEvents.any {
                    it.type == ProgressEventType.JOURNAL_CREATED || it.type == ProgressEventType.JOURNAL_EDITED
                }
                val hasDevotional = dayEvents.any {
                    it.type == ProgressEventType.DEVOTIONAL_OPENED || it.type == ProgressEventType.DEVOTIONAL_COMPLETED
                }
                val hasPrayer = dayEvents.any {
                    it.type == ProgressEventType.PRAYER_OPENED
                }

                val totalCount = dayEvents.size + dayJournals.size
                if (totalCount > 0) {
                    activeDaysCount++
                }

                days.add(
                    DayActivity(
                        dateMillis = dayStart,
                        eventCount = totalCount,
                        hasJournal = hasJournal,
                        hasDevotional = hasDevotional,
                        hasPrayer = hasPrayer,
                    ),
                )
            }

            return WeeklyActivitySummary(
                days = days,
                activeDaysCount = activeDaysCount,
            )
        }

        /**
         * Calcula el resumen del mes calendario en curso.
         */
        fun calculateMonthlyActivity(
            events: List<ProgressEvent>,
            journalEntries: List<JournalEntry>,
            nowMillis: Long,
            timeZone: TimeZone,
        ): MonthlyActivitySummary {
            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
            }
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)

            val monthActiveDays = mutableSetOf<Int>()
            var totalEventsInMonth = 0

            events.forEach { event ->
                val eventCal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = event.timestamp
                }
                if (eventCal.get(Calendar.YEAR) == currentYear && eventCal.get(Calendar.MONTH) == currentMonth) {
                    monthActiveDays.add(eventCal.get(Calendar.DAY_OF_MONTH))
                    totalEventsInMonth++
                }
            }

            journalEntries.forEach { entry ->
                val entryCal = Calendar.getInstance(timeZone).apply {
                    timeInMillis = entry.createdAt
                }
                if (entryCal.get(Calendar.YEAR) == currentYear && entryCal.get(Calendar.MONTH) == currentMonth) {
                    monthActiveDays.add(entryCal.get(Calendar.DAY_OF_MONTH))
                    totalEventsInMonth++
                }
            }

            return MonthlyActivitySummary(
                activeDaysCount = monthActiveDays.size,
                totalEventsCount = totalEventsInMonth,
            )
        }
    }
}
