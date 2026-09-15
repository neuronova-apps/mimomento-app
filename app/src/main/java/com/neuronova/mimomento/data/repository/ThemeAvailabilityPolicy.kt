package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronova.mimomento.data.model.MiMomentoThemeId

interface ThemeAvailabilityPolicy {
    fun isThemeOwned(themeId: MiMomentoThemeId): Boolean
    fun getOwnedThemes(): Set<MiMomentoThemeId>

    fun isThemeUnlocked(themeId: MiMomentoThemeId): Boolean = isThemeOwned(themeId)
    fun canUseTheme(themeId: MiMomentoThemeId): Boolean = isThemeUnlocked(themeId)
    fun canUseTheme(theme: MiMomentoThemeDefinition): Boolean = canUseTheme(theme.id)
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

