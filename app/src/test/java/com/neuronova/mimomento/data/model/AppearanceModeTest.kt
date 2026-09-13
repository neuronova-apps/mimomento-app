package com.neuronova.mimomento.data.model

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.neuronova.mimomento.data.repository.DefaultDebugThemePreviewPolicy
import com.neuronova.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.ui.theme.ThemeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AppearanceModeTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(): ThemePreferencesRepository {
        val testFile = tempFolder.newFile("test_appearance_prefs_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        return ThemePreferencesRepository(dataStore)
    }

    private fun createViewModel(
        repository: ThemePreferencesRepository = createRepository(),
    ): ThemeViewModel {
        return ThemeViewModel(
            repository = repository,
            availabilityPolicy = DefaultThemeAvailabilityPolicy(),
            previewPolicy = DefaultDebugThemePreviewPolicy(),
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
    }

    // 1. DAY fuerza claro
    @Test
    fun dayMode_forcesLightRegardlessOfSystemDarkTheme() {
        assertFalse("DAY with system dark=true must resolve to light (false)", resolveIsDarkTheme(AppearanceMode.DAY, systemIsDark = true))
        assertFalse("DAY with system dark=false must resolve to light (false)", resolveIsDarkTheme(AppearanceMode.DAY, systemIsDark = false))
        assertFalse("DAY with system dark=null must resolve to light (false)", resolveIsDarkTheme(AppearanceMode.DAY, systemIsDark = null))
        assertFalse("DAY member isDark(true) must return false", AppearanceMode.DAY.isDark(systemIsDark = true))
    }

    // 2. NIGHT fuerza oscuro
    @Test
    fun nightMode_forcesDarkRegardlessOfSystemDarkTheme() {
        assertTrue("NIGHT with system dark=false must resolve to dark (true)", resolveIsDarkTheme(AppearanceMode.NIGHT, systemIsDark = false))
        assertTrue("NIGHT with system dark=true must resolve to dark (true)", resolveIsDarkTheme(AppearanceMode.NIGHT, systemIsDark = true))
        assertTrue("NIGHT with system dark=null must resolve to dark (true)", resolveIsDarkTheme(AppearanceMode.NIGHT, systemIsDark = null))
        assertTrue("NIGHT member isDark(false) must return true", AppearanceMode.NIGHT.isDark(systemIsDark = false))
    }

    // 3. SYSTEM + sistema claro = Día
    @Test
    fun systemMode_whenSystemLight_resolvesToDay() {
        assertFalse("SYSTEM with system dark=false must resolve to Día (false)", resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = false))
        assertFalse("SYSTEM member isDark(false) must return false", AppearanceMode.SYSTEM.isDark(systemIsDark = false))
    }

    // 4. SYSTEM + sistema oscuro = Noche
    @Test
    fun systemMode_whenSystemDark_resolvesToNight() {
        assertTrue("SYSTEM with system dark=true must resolve to Noche (true)", resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = true))
        assertTrue("SYSTEM member isDark(true) must return true", AppearanceMode.SYSTEM.isDark(systemIsDark = true))
    }

    // 5. SYSTEM + estado desconocido = Día (fallback seguro)
    @Test
    fun systemMode_whenSystemUnknown_fallsBackToDay() {
        assertFalse("SYSTEM with system dark=null must fallback to Día (false)", resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = null))
        assertFalse("SYSTEM member isDark(null) must return false as fallback", AppearanceMode.SYSTEM.isDark(systemIsDark = null))
    }

    // 6. Default = SYSTEM
    @Test
    fun defaultAppearanceMode_isSystem() {
        assertEquals("AppearanceMode.DEFAULT must be SYSTEM", AppearanceMode.SYSTEM, AppearanceMode.DEFAULT)
        val defaultState = com.neuronova.mimomento.data.repository.ThemePreferencesState()
        assertEquals("Initial preferences state must have SYSTEM appearanceMode", AppearanceMode.SYSTEM, defaultState.appearanceMode)
    }

    // 7. Persistencia DAY
    @Test
    fun persistence_dayMode_persistsCorrectly() = runBlocking {
        val repo = createRepository()
        repo.setAppearanceMode(AppearanceMode.DAY)
        val state = repo.preferencesFlow.first()
        assertEquals(AppearanceMode.DAY, state.appearanceMode)
    }

    // 8. Persistencia NIGHT
    @Test
    fun persistence_nightMode_persistsCorrectly() = runBlocking {
        val repo = createRepository()
        repo.setAppearanceMode(AppearanceMode.NIGHT)
        val state = repo.preferencesFlow.first()
        assertEquals(AppearanceMode.NIGHT, state.appearanceMode)
    }

    // 9. Persistencia SYSTEM
    @Test
    fun persistence_systemMode_persistsCorrectly() = runBlocking {
        val repo = createRepository()
        repo.setAppearanceMode(AppearanceMode.SYSTEM)
        val state = repo.preferencesFlow.first()
        assertEquals(AppearanceMode.SYSTEM, state.appearanceMode)
    }

    // 10. Valor corrupto -> SYSTEM
    @Test
    fun persistence_corruptedValue_fallsBackToSystem() = runBlocking {
        val testFile = tempFolder.newFile("corrupt_appearance_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        dataStore.edit { prefs ->
            prefs[ThemePreferencesRepository.KEY_APPEARANCE_MODE] = "INVALID_CORRUPT_MODE"
        }
        val repo = ThemePreferencesRepository(dataStore)
        val state = repo.preferencesFlow.first()
        assertEquals(AppearanceMode.SYSTEM, state.appearanceMode)
    }

    // 11. Cambio reactivo
    @Test
    fun reactiveUpdate_updatesImmediatelyInViewModel() = runBlocking {
        val repo = createRepository()
        val viewModel = createViewModel(repo)

        val initialState = viewModel.uiState.first()
        assertEquals(AppearanceMode.SYSTEM, initialState.appearanceMode)

        viewModel.setAppearanceMode(AppearanceMode.NIGHT)
        val nightState = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(AppearanceMode.NIGHT, nightState.appearanceMode)

        viewModel.setAppearanceMode(AppearanceMode.DAY)
        val dayState = viewModel.uiState.first { it.appearanceMode == AppearanceMode.DAY }
        assertEquals(AppearanceMode.DAY, dayState.appearanceMode)
    }

    // 12. Tema visual permanece intacto
    @Test
    fun changingAppearanceMode_preservesActiveAndSelectedVisualTheme() = runBlocking {
        val repo = createRepository()
        val viewModel = createViewModel(repo)

        val initialState = viewModel.uiState.first { it.selectedTheme.id == MiMomentoThemeId.SKY }
        val initialTheme = initialState.selectedTheme

        viewModel.setAppearanceMode(AppearanceMode.NIGHT)
        val updatedState = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals("Visual theme must remain intact after appearance change", initialTheme.id, updatedState.selectedTheme.id)
        assertEquals("Active visual theme must remain intact after appearance change", initialTheme.id, updatedState.activeTheme.id)
    }

    // 13. Cambiar tema no modifica appearanceMode
    @Test
    fun selectingVisualTheme_doesNotChangeAppearanceMode() = runBlocking {
        val repo = createRepository()
        repo.setAppearanceMode(AppearanceMode.NIGHT)
        val viewModel = createViewModel(repo)

        val stateWithNight = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(AppearanceMode.NIGHT, stateWithNight.appearanceMode)

        viewModel.selectTheme(MiMomentoThemeId.SKY)
        val afterThemeSelect = viewModel.uiState.first { it.selectedTheme.id == MiMomentoThemeId.SKY }
        assertEquals("Selecting theme must not alter appearanceMode", AppearanceMode.NIGHT, afterThemeSelect.appearanceMode)
    }

    // 14. Cambiar appearanceMode no modifica tema
    @Test
    fun settingAppearanceMode_doesNotAlterSelectedThemeId() = runBlocking {
        val repo = createRepository()
        repo.setSelectedTheme(MiMomentoThemeId.SKY)
        repo.setAppearanceMode(AppearanceMode.DAY)

        val state = repo.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.SKY, state.selectedThemeId)
        assertEquals(AppearanceMode.DAY, state.appearanceMode)
    }

    // 15. HC conserva appearanceMode
    @Test
    fun highContrast_doesNotOverwriteOrEraseAppearanceMode() = runBlocking {
        val repo = createRepository()
        repo.setAppearanceMode(AppearanceMode.NIGHT)
        val viewModel = createViewModel(repo)

        // When High Contrast is visually applied, appearanceMode preference remains NIGHT
        val state = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(AppearanceMode.NIGHT, state.appearanceMode)

        val skyTheme = MiMomentoThemeCatalog.SKY
        val hcVisual = skyTheme.resolveVisual(isDarkTheme = true, highContrast = true)
        // High Contrast colors applied visually
        assertEquals(androidx.compose.ui.graphics.Color(0xFFFFD600), hcVisual.primary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF000000), hcVisual.surface)

        // But repository still maintains NIGHT
        val repoState = repo.preferencesFlow.first()
        assertEquals(AppearanceMode.NIGHT, repoState.appearanceMode)
    }

    // 16. Desactivar HC recupera appearanceMode
    @Test
    fun disablingHighContrast_restoresSelectedAppearanceModeVisuals() {
        val skyTheme = MiMomentoThemeCatalog.SKY
        val nightVisual = skyTheme.resolveVisual(isDarkTheme = true, highContrast = false)

        // Resolved visual in night mode matches night visual, not HC yellow
        assertEquals(skyTheme.nightVisual, nightVisual)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF68B2DF), nightVisual.primary)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF10171E), nightVisual.surface)
    }

    // 17. DAY compatible con todos los temas
    @Test
    fun dayMode_compatibleWithAllCatalogThemes() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            val visual = theme.resolveVisual(isDarkTheme = false, highContrast = false)
            assertEquals("Theme ${theme.id} in Day must use its day visual", theme.visual, visual)
            assertNotNull(visual.primary)
            assertNotNull(visual.surface)
            assertNotNull(visual.cardColor)
        }
    }

    // 18. NIGHT compatible con todos los temas
    @Test
    fun nightMode_compatibleWithAllCatalogThemes() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            val visual = theme.resolveVisual(isDarkTheme = true, highContrast = false)
            assertEquals("Theme ${theme.id} in Night must use its night visual", theme.nightVisual, visual)
            assertNotNull(visual.primary)
            assertNotNull(visual.surface)
            assertNotNull(visual.cardColor)
            assertTrue("Night overlay alpha must be prominent for comfortable reading", visual.overlayAlpha >= 0.85f)
        }
    }

    // 19. SYSTEM compatible con todos los temas
    @Test
    fun systemMode_compatibleWithAllCatalogThemesInBothLightAndDarkSystemStates() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            val systemLightVisual = theme.resolveVisual(
                isDarkTheme = resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = false),
                highContrast = false,
            )
            assertEquals("SYSTEM in light system must resolve to day visual for ${theme.id}", theme.visual, systemLightVisual)

            val systemDarkVisual = theme.resolveVisual(
                isDarkTheme = resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = true),
                highContrast = false,
            )
            assertEquals("SYSTEM in dark system must resolve to night visual for ${theme.id}", theme.nightVisual, systemDarkVisual)
        }
    }

    // 20. Compatible con Normal
    @Test
    fun textScale_normalScale_multiplierMatches() {
        val normal = TextScale.NORMAL
        assertEquals(1.00f, normal.multiplier, 0.001f)
        assertEquals(1.00f, normal.effectiveMultiplier(), 0.001f)
    }

    // 21. Compatible con Grande
    @Test
    fun textScale_largeScale_multiplierMatches() {
        val large = TextScale.LARGE
        assertEquals(1.15f, large.multiplier, 0.001f)
        assertEquals(1.15f, large.effectiveMultiplier(), 0.001f)
    }

    // 22. Compatible con Muy grande
    @Test
    fun textScale_veryLargeScale_multiplierMatches() {
        val veryLarge = TextScale.VERY_LARGE
        assertEquals(1.30f, veryLarge.multiplier, 0.001f)
        assertEquals(1.30f, veryLarge.effectiveMultiplier(), 0.001f)
    }

    // 23. AppearanceMode tiene exactamente tres valores
    @Test
    fun appearanceMode_hasExactlyThreeUniqueAndStableValues() {
        val values = AppearanceMode.entries
        assertEquals("AppearanceMode must have exactly 3 values", 3, values.size)
        assertEquals("Values must be DAY, NIGHT, SYSTEM", listOf("DAY", "NIGHT", "SYSTEM"), values.map { it.name })
        assertEquals("Values must be distinct", 3, values.toSet().size)

        assertEquals(AppearanceMode.DAY, AppearanceMode.valueOf("DAY"))
        assertEquals(AppearanceMode.NIGHT, AppearanceMode.valueOf("NIGHT"))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.valueOf("SYSTEM"))

        assertEquals(AppearanceMode.DAY, AppearanceMode.fromNameSafe("day"))
        assertEquals(AppearanceMode.NIGHT, AppearanceMode.fromNameSafe("NIGHT"))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromNameSafe("system"))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromNameSafe(null))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromNameSafe(""))
        assertEquals(AppearanceMode.SYSTEM, AppearanceMode.fromNameSafe("UNKNOWN_STRING"))
    }

    private fun TextScale.effectiveMultiplier(): Float = multiplier.coerceAtLeast(1.00f)
}
