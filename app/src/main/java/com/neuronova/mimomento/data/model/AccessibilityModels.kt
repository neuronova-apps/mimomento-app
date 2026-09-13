package com.neuronova.mimomento.data.model

enum class TextScale(
    val multiplier: Float,
) {
    NORMAL(1.00f),
    LARGE(1.15f),
    VERY_LARGE(1.30f);

    companion object {
        val DEFAULT = NORMAL

        fun fromMultiplier(multiplier: Float): TextScale {
            val clamped = multiplier.coerceAtLeast(1.00f)
            return entries.minByOrNull { kotlin.math.abs(it.multiplier - clamped) } ?: DEFAULT
        }

        fun fromNameSafe(name: String?): TextScale {
            if (name.isNullOrBlank()) return DEFAULT
            return try {
                valueOf(name.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                DEFAULT
            }
        }
    }
}

data class AccessibilitySettings(
    val textScale: TextScale = TextScale.NORMAL,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
) {
    val effectiveMultiplier: Float
        get() = textScale.multiplier.coerceAtLeast(1.00f)
}
