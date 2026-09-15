package com.neuronovaapps.mimomento.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.neuronovaapps.mimomento.data.model.AccessibilitySettings
import com.neuronovaapps.mimomento.data.model.TextScale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccessibilityPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    companion object {
        val KEY_TEXT_SCALE = stringPreferencesKey("text_scale")
        val KEY_HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val KEY_REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }

    val settingsFlow: Flow<AccessibilitySettings> = dataStore.data.map { prefs ->
        val rawTextScale = prefs[KEY_TEXT_SCALE]
        val textScale = TextScale.fromNameSafe(rawTextScale)
        val highContrast = prefs[KEY_HIGH_CONTRAST] ?: false
        val reduceMotion = prefs[KEY_REDUCE_MOTION] ?: false

        AccessibilitySettings(
            textScale = textScale,
            highContrast = highContrast,
            reduceMotion = reduceMotion,
        )
    }

    suspend fun setTextScale(textScale: TextScale) {
        dataStore.edit { prefs ->
            prefs[KEY_TEXT_SCALE] = textScale.name
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HIGH_CONTRAST] = enabled
        }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_REDUCE_MOTION] = enabled
        }
    }
}
