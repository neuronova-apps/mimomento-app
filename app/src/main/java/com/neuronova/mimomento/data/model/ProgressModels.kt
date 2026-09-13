package com.neuronova.mimomento.data.model

import kotlinx.serialization.Serializable

/**
 * Tipos de eventos cuantitativos de actividad en MiMomento.
 * No almacenan texto, oraciones personales ni estados emocionales para preservar la privacidad.
 */
@Serializable
enum class ProgressEventType {
    JOURNAL_CREATED,
    JOURNAL_EDITED,
    DEVOTIONAL_OPENED,
    DEVOTIONAL_COMPLETED,
    PRAYER_OPENED,
}

/**
 * Evento de actividad individual persistido en almacenamiento local.
 *
 * @param id Identificador único del evento.
 * @param type Tipo de actividad realizada.
 * @param timestamp Marca temporal (epoch millis) en que ocurrió la actividad.
 * @param referenceId Identificador anónimo de la entidad relacionada (e.g. ID del devocional o guía de oración).
 */
@Serializable
data class ProgressEvent(
    val id: String,
    val type: ProgressEventType,
    val timestamp: Long,
    val referenceId: String? = null,
)

/**
 * Representa el resumen de actividad de un día específico (iniciado a medianoche local).
 */
data class DayActivity(
    val dateMillis: Long,
    val eventCount: Int,
    val hasJournal: Boolean = false,
    val hasDevotional: Boolean = false,
    val hasPrayer: Boolean = false,
) {
    val hasActivity: Boolean get() = eventCount > 0
}

/**
 * Resumen de actividad de los últimos 7 días.
 */
data class WeeklyActivitySummary(
    val days: List<DayActivity> = emptyList(),
    val activeDaysCount: Int = 0,
)

/**
 * Resumen de actividad del mes calendario actual.
 */
data class MonthlyActivitySummary(
    val activeDaysCount: Int = 0,
    val totalEventsCount: Int = 0,
)

/**
 * Resumen agregado consolidado de Progreso para la interfaz de usuario.
 */
data class ProgressSummary(
    val totalActiveDays: Int = 0,
    val journalEntriesCount: Int = 0,
    val devotionalsCompletedCount: Int = 0,
    val devotionalsOpenedCount: Int = 0,
    val prayersOpenedCount: Int = 0,
    val weeklyActivity: WeeklyActivitySummary = WeeklyActivitySummary(),
    val monthlyActivity: MonthlyActivitySummary = MonthlyActivitySummary(),
    val currentStreakDays: Int = 0,
) {
    val isEmpty: Boolean
        get() = totalActiveDays == 0 &&
                journalEntriesCount == 0 &&
                devotionalsOpenedCount == 0 &&
                devotionalsCompletedCount == 0 &&
                prayersOpenedCount == 0
}
