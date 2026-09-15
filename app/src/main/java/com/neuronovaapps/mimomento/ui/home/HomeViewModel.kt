package com.neuronovaapps.mimomento.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronovaapps.mimomento.data.model.JournalEntry
import com.neuronovaapps.mimomento.data.model.ProgressEvent
import com.neuronovaapps.mimomento.data.model.ProgressEventType
import com.neuronovaapps.mimomento.data.model.ProgressSummary
import com.neuronovaapps.mimomento.data.repository.JournalRepository
import com.neuronovaapps.mimomento.data.repository.LocalProgressRepository
import com.neuronovaapps.mimomento.data.repository.ProgressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/**
 * Estado UI reactivo para los resúmenes dinámicos de Inicio.
 *
 * @param journalEntriesToday Número de entradas de diario creadas hoy (usando createdAt).
 * @param activeDaysThisWeek Días activos durante los últimos 7 días provisto por ProgressRepository.
 * @param devotionalsThisWeek Devocionales completados (DEVOTIONAL_COMPLETED) durante los últimos 7 días.
 * @param journalEntriesThisWeek Entradas de diario creadas durante los últimos 7 días.
 */
data class HomeUiState(
    val journalEntriesToday: Int = 0,
    val activeDaysThisWeek: Int = 0,
    val devotionalsThisWeek: Int = 0,
    val journalEntriesThisWeek: Int = 0,
)

/**
 * ViewModel ligero para la pantalla de Inicio.
 * Combina reactivamente JournalRepository y ProgressRepository sin duplicar almacenamiento.
 */
class HomeViewModel(
    private val journalRepository: JournalRepository,
    private val progressRepository: ProgressRepository,
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val timeZone: TimeZone = TimeZone.getDefault(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    externalScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        initializeRepositories()
    }

    private fun observeData() {
        scope.launch(dispatcher) {
            combine(
                journalRepository.entriesFlow,
                progressRepository.observeSummary(),
                progressRepository.eventsFlow,
            ) { journalEntries, progressSummary, progressEvents ->
                calculateHomeUiState(
                    journalEntries = journalEntries,
                    progressSummary = progressSummary,
                    progressEvents = progressEvents,
                    nowMillis = timeProvider(),
                    timeZone = timeZone,
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun initializeRepositories() {
        scope.launch(dispatcher) {
            journalRepository.refresh()
            progressRepository.getSummary()
        }
    }

    companion object {
        /**
         * Función pura para calcular el estado UI de Inicio a partir de las fuentes de verdad.
         */
        fun calculateHomeUiState(
            journalEntries: List<JournalEntry>,
            progressSummary: ProgressSummary,
            progressEvents: List<ProgressEvent>,
            nowMillis: Long,
            timeZone: TimeZone = TimeZone.getDefault(),
        ): HomeUiState {
            val todayKey = LocalProgressRepository.getCalendarDayKey(nowMillis, timeZone)

            // 1. "Tu momento de hoy": Entradas creadas hoy exclusivamente mediante createdAt
            val entriesToday = journalEntries.count { entry ->
                LocalProgressRepository.getCalendarDayKey(entry.createdAt, timeZone) == todayKey
            }

            // 2. "Esta semana": Reutilizar la ventana de 7 días y los días activos calculados por ProgressRepository
            val weekly = progressSummary.weeklyActivity
            val activeDays = weekly.activeDaysCount

            val weekStart = weekly.days.firstOrNull()?.dateMillis ?: getStartOfDayMillis(
                nowMillis = nowMillis,
                offsetDays = 6,
                timeZone = timeZone,
            )
            val weekEnd = (weekly.days.lastOrNull()?.dateMillis ?: getStartOfDayMillis(
                nowMillis = nowMillis,
                offsetDays = 0,
                timeZone = timeZone,
            )) + 86_400_000L - 1L

            // Devocionales completados dentro de la ventana semanal
            val devotionalsInWeek = progressEvents.count { event ->
                event.timestamp in weekStart..weekEnd &&
                    event.type == ProgressEventType.DEVOTIONAL_COMPLETED
            }

            // Entradas de diario creadas dentro de la ventana semanal
            val journalsInWeek = journalEntries.count { entry ->
                entry.createdAt in weekStart..weekEnd
            }

            return HomeUiState(
                journalEntriesToday = entriesToday,
                activeDaysThisWeek = activeDays,
                devotionalsThisWeek = devotionalsInWeek,
                journalEntriesThisWeek = journalsInWeek,
            )
        }

        private fun getStartOfDayMillis(nowMillis: Long, offsetDays: Int, timeZone: TimeZone): Long {
            val cal = Calendar.getInstance(timeZone).apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, -offsetDays)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun provideFactory(
            journalRepository: JournalRepository,
            progressRepository: ProgressRepository,
            timeProvider: () -> Long = { System.currentTimeMillis() },
            timeZone: TimeZone = TimeZone.getDefault(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    return HomeViewModel(
                        journalRepository = journalRepository,
                        progressRepository = progressRepository,
                        timeProvider = timeProvider,
                        timeZone = timeZone,
                        dispatcher = dispatcher,
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
