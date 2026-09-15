package com.neuronovaapps.mimomento.data.repository

import com.neuronovaapps.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronovaapps.mimomento.data.model.MiMomentoThemeId

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
    override val isAllowed: Boolean = com.neuronovaapps.mimomento.BuildConfig.DEBUG,
) : DebugThemePreviewPolicy

