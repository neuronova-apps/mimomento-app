package com.neuronova.mimomento.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

data class ThemePreferencesState(
    val selectedThemeId: MiMomentoThemeId = MiMomentoThemeId.SKY,
    val autoThemeEnabled: Boolean = false,
    val autoThemeSelectedIds: Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.SKY),
    val autoThemeMode: String = AUTO_MODE_SESSION_START,
) {
    companion object {
        const val AUTO_MODE_SESSION_START = "SESSION_START"
    }
}

class ThemePreferencesRepository(
    private val dataStore: DataStore<Preferences>,
    private val availabilityPolicy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
) {
    companion object {
        val KEY_SELECTED_THEME_ID = stringPreferencesKey("selected_theme_id")
        val KEY_AUTO_THEME_ENABLED = booleanPreferencesKey("auto_theme_enabled")
        val KEY_AUTO_THEME_SELECTED_IDS = stringSetPreferencesKey("auto_theme_selected_ids")
        val KEY_AUTO_THEME_MODE = stringPreferencesKey("auto_theme_mode")
    }

    val preferencesFlow: Flow<ThemePreferencesState> = dataStore.data.map { prefs ->
        val rawSelectedId = prefs[KEY_SELECTED_THEME_ID]
        val selectedThemeId = parseThemeIdSafe(rawSelectedId)

        val autoEnabled = prefs[KEY_AUTO_THEME_ENABLED] ?: false

        val rawSelectedSet = prefs[KEY_AUTO_THEME_SELECTED_IDS]
        val autoSelectedIds = if (rawSelectedSet.isNullOrEmpty()) {
            setOf(MiMomentoThemeId.SKY)
        } else {
            val parsedSet = rawSelectedSet.mapNotNull { str ->
                try {
                    MiMomentoThemeId.valueOf(str.trim().uppercase())
                } catch (_: IllegalArgumentException) {
                    null
                }
            }.toSet()
            if (parsedSet.isEmpty()) setOf(MiMomentoThemeId.SKY) else parsedSet
        }

        val mode = prefs[KEY_AUTO_THEME_MODE] ?: ThemePreferencesState.AUTO_MODE_SESSION_START

        ThemePreferencesState(
            selectedThemeId = selectedThemeId,
            autoThemeEnabled = autoEnabled,
            autoThemeSelectedIds = autoSelectedIds,
            autoThemeMode = mode,
        )
    }

    suspend fun setSelectedTheme(themeId: MiMomentoThemeId) {
        dataStore.edit { prefs ->
            prefs[KEY_SELECTED_THEME_ID] = themeId.name
        }
    }

    suspend fun setAutoThemeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTO_THEME_ENABLED] = enabled
        }
    }

    suspend fun setAutoThemeSelectedIds(themeIds: Set<MiMomentoThemeId>) {
        dataStore.edit { prefs ->
            prefs[KEY_AUTO_THEME_SELECTED_IDS] = themeIds.map { it.name }.toSet()
        }
    }

    fun resolveSessionTheme(
        state: ThemePreferencesState,
        policy: ThemeAvailabilityPolicy = availabilityPolicy,
        randomSource: Random? = null,
    ): MiMomentoThemeId {
        if (!state.autoThemeEnabled) {
            return if (policy.isThemeOwned(state.selectedThemeId)) {
                state.selectedThemeId
            } else {
                MiMomentoThemeId.SKY
            }
        }

        val validThemes = state.autoThemeSelectedIds.filter { policy.isThemeOwned(it) }

        return when (validThemes.size) {
            0 -> MiMomentoThemeId.SKY
            1 -> validThemes.first()
            else -> {
                val rand = randomSource ?: Random.Default
                validThemes[rand.nextInt(validThemes.size)]
            }
        }
    }

    private fun parseThemeIdSafe(raw: String?): MiMomentoThemeId {
        if (raw.isNullOrBlank()) return MiMomentoThemeId.SKY
        return try {
            MiMomentoThemeId.valueOf(raw.trim().uppercase())
        } catch (_: IllegalArgumentException) {
            MiMomentoThemeId.SKY
        }
    }
}
