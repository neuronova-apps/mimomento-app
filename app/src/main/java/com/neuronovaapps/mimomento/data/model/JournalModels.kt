package com.neuronovaapps.mimomento.data.model

import androidx.annotation.StringRes
import com.neuronovaapps.mimomento.R
import kotlinx.serialization.Serializable

/**
 * Estados emocionales cotidianos y no clínicos para el Diario personal.
 * La selección es siempre opcional y no constituye diagnóstico ni evaluación psicológica.
 */
@Serializable
enum class JournalMood(
    val id: String,
    val displayName: String,
    val emoji: String,
    @param:StringRes val labelRes: Int,
) {
    GOOD(
        id = "good",
        displayName = "Bien",
        emoji = "😊",
        labelRes = R.string.mood_good,
    ),
    CALM(
        id = "calm",
        displayName = "Tranquilo",
        emoji = "😌",
        labelRes = R.string.mood_calm,
    ),
    CHEERFUL(
        id = "cheerful",
        displayName = "Animado",
        emoji = "✨",
        labelRes = R.string.mood_cheerful,
    ),
    NEUTRAL(
        id = "neutral",
        displayName = "Neutral",
        emoji = "😐",
        labelRes = R.string.mood_neutral,
    ),
    TIRED(
        id = "tired",
        displayName = "Cansado",
        emoji = "🥱",
        labelRes = R.string.mood_tired,
    ),
    WORRIED(
        id = "worried",
        displayName = "Preocupado",
        emoji = "😟",
        labelRes = R.string.mood_worried,
    ),
    SAD(
        id = "sad",
        displayName = "Triste",
        emoji = "😔",
        labelRes = R.string.mood_sad,
    );

    companion object {
        fun fromId(id: String?): JournalMood? {
            if (id.isNullOrBlank()) return null
            val normalized = id.trim().lowercase()
            return entries.firstOrNull {
                it.id.equals(normalized, ignoreCase = true) || it.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}

/**
 * Entrada individual del Diario de MiMomento.
 */
@Serializable
data class JournalEntry(
    val id: String,
    val text: String,
    val mood: JournalMood? = null,
    val createdAt: Long,
    val updatedAt: Long = createdAt,
)
