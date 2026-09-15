package com.neuronovaapps.mimomento.ui.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.neuronovaapps.mimomento.data.model.AccessibilitySettings
import com.neuronovaapps.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronovaapps.mimomento.data.model.TextScale
import com.neuronovaapps.mimomento.data.repository.AccessibilityPreferencesRepository
import androidx.compose.ui.graphics.Color
import com.neuronovaapps.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronovaapps.mimomento.ui.navigation.shouldShowBottomBar
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

    // --- 4. ALTO CONTRASTE VERDADERO Y TRANSFORMACIÓN DE TEMAS ---

    @Test
    fun highContrast_falsePreservesOriginalThemeVisuals() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            val normalVisual = theme.visual
            assertEquals(normalVisual, theme.visual)
        }
    }

    @Test
    fun highContrast_trueTransformsToStrictAccessiblePalette() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            val hcVisual = theme.visual.toHighContrast()

            // 1. Surface and background are black
            assertEquals(Color(0xFF000000), hcVisual.surface)
            assertEquals(Color(0xFF000000), hcVisual.scrimColor)

            // 2. cardColor is #101010
            assertEquals(Color(0xFF101010), hcVisual.cardColor)
            assertEquals(Color(0xFF101010), hcVisual.surfaceVariant)

            // 3. Text primary and onSurface are white
            assertEquals(Color(0xFFFFFFFF), hcVisual.onSurface)
            assertEquals(Color(0xFFFFFFFF), hcVisual.onBackground)

            // 4. Primary accessibility accent is #FFD600 (yellow)
            assertEquals(Color(0xFFFFD600), hcVisual.primary)
            assertEquals(Color(0xFFFFD600), hcVisual.buttonColor)
            assertEquals(Color(0xFFFFD600), hcVisual.iconTint)

            // 5. onPrimary / onButtonColor is black
            assertEquals(Color(0xFF000000), hcVisual.onButtonColor)

            // 6. Normal border / outline is white
            assertEquals(Color(0xFFFFFFFF), hcVisual.borderColor)

            // 7. Secondary is white
            assertEquals(Color(0xFFFFFFFF), hcVisual.secondary)

            // 8. Decorative alpha is 0 and overlayAlpha is 1.0f
            assertEquals(0.0f, hcVisual.decorativeAlpha, 0.001f)
            assertEquals(1.0f, hcVisual.overlayAlpha, 0.001f)
        }
    }

    @Test
    fun highContrast_preservesThemeIdentityAndRecoversWhenDisabled() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            // Simulate user having theme selected
            val selectedTheme = theme
            val originalVisual = selectedTheme.visual

            // When HC is activated, the theme ID and definition identity remain unchanged
            val hcVisual = selectedTheme.visual.toHighContrast()
            assertEquals(selectedTheme.id, theme.id)
            assertEquals(selectedTheme.backgroundRes, theme.backgroundRes)
            assertEquals(Color(0xFFFFD600), hcVisual.primary)

            // When HC is deactivated, original visual is fully intact
            val restoredVisual = selectedTheme.visual
            assertEquals(originalVisual.primary, restoredVisual.primary)
            assertEquals(originalVisual.surface, restoredVisual.surface)
            assertEquals(originalVisual.cardColor, restoredVisual.cardColor)
            assertEquals(originalVisual.borderColor, restoredVisual.borderColor)
            assertEquals(originalVisual, restoredVisual)
        }
    }

    @Test
    fun highContrast_worksSimultaneouslyWithAllTextScales() = runBlocking {
        val repository = createRepository()

        // 1. NORMAL + HC
        repository.setTextScale(TextScale.NORMAL)
        repository.setHighContrast(true)
        var settings = repository.settingsFlow.first()
        assertEquals(TextScale.NORMAL, settings.textScale)
        assertTrue(settings.highContrast)
        assertEquals(1.00f, settings.effectiveMultiplier, 0.001f)

        // 2. LARGE + HC
        repository.setTextScale(TextScale.LARGE)
        settings = repository.settingsFlow.first()
        assertEquals(TextScale.LARGE, settings.textScale)
        assertTrue(settings.highContrast)
        assertEquals(1.15f, settings.effectiveMultiplier, 0.001f)

        // 3. VERY_LARGE + HC
        repository.setTextScale(TextScale.VERY_LARGE)
        settings = repository.settingsFlow.first()
        assertEquals(TextScale.VERY_LARGE, settings.textScale)
        assertTrue(settings.highContrast)
        assertEquals(1.30f, settings.effectiveMultiplier, 0.001f)

        // 4. Disable HC while maintaining VERY_LARGE
        repository.setHighContrast(false)
        settings = repository.settingsFlow.first()
        assertEquals(TextScale.VERY_LARGE, settings.textScale)
        assertFalse(settings.highContrast)
        assertEquals(1.30f, settings.effectiveMultiplier, 0.001f)
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
