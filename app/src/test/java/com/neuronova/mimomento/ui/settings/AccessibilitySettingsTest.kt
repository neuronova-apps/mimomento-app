package com.neuronova.mimomento.ui.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.neuronova.mimomento.data.model.AccessibilitySettings
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.TextScale
import com.neuronova.mimomento.data.repository.AccessibilityPreferencesRepository
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AccessibilitySettingsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(): AccessibilityPreferencesRepository {
        val testFile = tempFolder.newFile("test_a11y_prefs_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        return AccessibilityPreferencesRepository(dataStore)
    }

    // --- 1. MODELO Y VALORES PREDETERMINADOS ---

    @Test
    fun defaultSettings_haveExpectedValues() {
        val defaultSettings = AccessibilitySettings()
        assertEquals(TextScale.NORMAL, defaultSettings.textScale)
        assertFalse(defaultSettings.highContrast)
        assertFalse(defaultSettings.reduceMotion)
        assertEquals(1.00f, defaultSettings.effectiveMultiplier, 0.001f)
    }

    @Test
    fun textScaleMultipliers_areExact() {
        assertEquals(1.00f, TextScale.NORMAL.multiplier, 0.0001f)
        assertEquals(1.15f, TextScale.LARGE.multiplier, 0.0001f)
        assertEquals(1.30f, TextScale.VERY_LARGE.multiplier, 0.0001f)
    }

    @Test
    fun textScale_doesNotAllowValuesLessThanNormal() {
        val clampedScale = TextScale.fromMultiplier(0.5f)
        assertEquals(TextScale.NORMAL, clampedScale)
        assertTrue(clampedScale.multiplier >= 1.00f)

        val negativeScale = TextScale.fromMultiplier(-1.0f)
        assertEquals(TextScale.NORMAL, negativeScale)
        assertTrue(negativeScale.multiplier >= 1.00f)
    }

    @Test
    fun fromNameSafe_handlesValidAndInvalidStrings() {
        assertEquals(TextScale.NORMAL, TextScale.fromNameSafe("NORMAL"))
        assertEquals(TextScale.LARGE, TextScale.fromNameSafe("LARGE"))
        assertEquals(TextScale.VERY_LARGE, TextScale.fromNameSafe("VERY_LARGE"))
        assertEquals(TextScale.NORMAL, TextScale.fromNameSafe(null))
        assertEquals(TextScale.NORMAL, TextScale.fromNameSafe(""))
        assertEquals(TextScale.NORMAL, TextScale.fromNameSafe("INVALID_SCALE"))
    }

    // --- 2. COMBINACIÓN CON FONTSCALE DE ANDROID ---

    @Test
    fun systemFontScale_multipliesCorrectlyWithMiMomentoScale() {
        val systemScales = listOf(1.0f, 1.15f, 1.25f, 1.5f)

        for (systemFontScale in systemScales) {
            val effectiveNormal = systemFontScale * TextScale.NORMAL.multiplier
            val effectiveLarge = systemFontScale * TextScale.LARGE.multiplier
            val effectiveVeryLarge = systemFontScale * TextScale.VERY_LARGE.multiplier

            assertEquals(systemFontScale * 1.00f, effectiveNormal, 0.0001f)
            assertEquals(systemFontScale * 1.15f, effectiveLarge, 0.0001f)
            assertEquals(systemFontScale * 1.30f, effectiveVeryLarge, 0.0001f)

            assertTrue(effectiveLarge > effectiveNormal)
            assertTrue(effectiveVeryLarge > effectiveLarge)
        }
    }

    // --- 3. PERSISTENCIA EN ACCESSIBILITY PREFERENCES REPOSITORY ---

    @Test
    fun repository_initialStateIsDefault() = runBlocking {
        val repository = createRepository()
        val settings = repository.settingsFlow.first()

        assertEquals(TextScale.NORMAL, settings.textScale)
        assertFalse(settings.highContrast)
        assertFalse(settings.reduceMotion)
    }

    @Test
    fun repository_textScaleChangePersistsAndRecovers() = runBlocking {
        val repository = createRepository()

        repository.setTextScale(TextScale.LARGE)
        var settings = repository.settingsFlow.first()
        assertEquals(TextScale.LARGE, settings.textScale)

        repository.setTextScale(TextScale.VERY_LARGE)
        settings = repository.settingsFlow.first()
        assertEquals(TextScale.VERY_LARGE, settings.textScale)

        repository.setTextScale(TextScale.NORMAL)
        settings = repository.settingsFlow.first()
        assertEquals(TextScale.NORMAL, settings.textScale)
    }

    @Test
    fun repository_highContrastChangePersistsAndRecovers() = runBlocking {
        val repository = createRepository()

        assertFalse(repository.settingsFlow.first().highContrast)

        repository.setHighContrast(true)
        assertTrue(repository.settingsFlow.first().highContrast)

        repository.setHighContrast(false)
        assertFalse(repository.settingsFlow.first().highContrast)
    }

    @Test
    fun repository_reduceMotionChangePersistsAndRecovers() = runBlocking {
        val repository = createRepository()

        assertFalse(repository.settingsFlow.first().reduceMotion)

        repository.setReduceMotion(true)
        assertTrue(repository.settingsFlow.first().reduceMotion)

        repository.setReduceMotion(false)
        assertFalse(repository.settingsFlow.first().reduceMotion)
    }

    @Test
    fun repository_corruptedPreferencesFallBackToSafeDefaults() = runBlocking {
        val testFile = tempFolder.newFile("corrupt_a11y_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        dataStore.edit { prefs ->
            prefs[AccessibilityPreferencesRepository.KEY_TEXT_SCALE] = "NON_EXISTENT_SCALE_VAL"
        }

        val repository = AccessibilityPreferencesRepository(dataStore)
        val settings = repository.settingsFlow.first()
        assertEquals(TextScale.NORMAL, settings.textScale)
        assertFalse(settings.highContrast)
        assertFalse(settings.reduceMotion)
    }

    // --- 4. ALTO CONTRASTE PRESERVA IDENTIDAD DE TEMA ---

    @Test
    fun highContrast_preservesThemeIdentityAndIncreasesContrast() {
        val themes = MiMomentoThemeCatalog.themes
        assertEquals(5, themes.size)

        themes.forEach { theme ->
            val normalVisual = theme.visual
            val highContrastVisual = theme.visual.toHighContrast()

            // Preserves theme brand colors
            assertEquals(normalVisual.primary, highContrastVisual.primary)
            assertEquals(normalVisual.secondary, highContrastVisual.secondary)

            // High contrast enhances readability
            assertEquals(1.0f, highContrastVisual.cardColor.alpha, 0.01f)
            assertTrue(highContrastVisual.overlayAlpha >= normalVisual.overlayAlpha)
            assertTrue(highContrastVisual.decorativeAlpha >= normalVisual.decorativeAlpha)
        }
    }

    // --- 5. NAVEGACIÓN Y BACKSTACK ---

    @Test
    fun accessibilityDestination_isCanonicalAndSuppressesBottomBar() {
        assertEquals("settings/accessibility", MiMomentoDestinations.ACCESSIBILITY)
        assertFalse(
            "Accessibility route must suppress bottom navigation bar",
            shouldShowBottomBar(MiMomentoDestinations.ACCESSIBILITY),
        )
    }

    @Test
    fun backStackTransitions_fromHomeToSettingsToAccessibilityAndReturn() {
        val backStack = mutableListOf(MiMomentoDestinations.HOME)
        assertTrue(shouldShowBottomBar(backStack.last()))

        // 1. Enter Settings
        backStack.add(MiMomentoDestinations.SETTINGS)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 2. Navigate to Accessibility
        backStack.add(MiMomentoDestinations.ACCESSIBILITY)
        assertEquals(
            listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS, MiMomentoDestinations.ACCESSIBILITY),
            backStack,
        )
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 3. Return to Settings
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 4. Return to Home
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME), backStack)
        assertTrue(shouldShowBottomBar(backStack.last()))
    }
}
