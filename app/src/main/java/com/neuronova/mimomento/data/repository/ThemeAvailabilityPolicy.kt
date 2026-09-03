package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronova.mimomento.data.model.MiMomentoThemeId

interface ThemeAvailabilityPolicy {
    fun isThemeOwned(themeId: MiMomentoThemeId): Boolean
    fun getOwnedThemes(): Set<MiMomentoThemeId>
}

class DefaultThemeAvailabilityPolicy : ThemeAvailabilityPolicy {
    override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean {
        return themeId == MiMomentoThemeId.SKY
    }

    override fun getOwnedThemes(): Set<MiMomentoThemeId> {
        return setOf(MiMomentoThemeId.SKY)
    }
}

interface DebugThemePreviewPolicy {
    val isAllowed: Boolean
}

class DefaultDebugThemePreviewPolicy(
    override val isAllowed: Boolean = com.neuronova.mimomento.BuildConfig.DEBUG,
) : DebugThemePreviewPolicy

