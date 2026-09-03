package com.neuronova.mimomento.ui.theme

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import com.neuronova.mimomento.data.repository.DebugThemePreviewPolicy
import com.neuronova.mimomento.data.repository.DefaultDebugThemePreviewPolicy
import com.neuronova.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ThemeViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createViewModel(
        policy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
        previewPolicy: DebugThemePreviewPolicy = DefaultDebugThemePreviewPolicy(isAllowed = true),
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ): Pair<ThemeViewModel, ThemePreferencesRepository> {
        val testFile = tempFolder.newFile("vm_test_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ) { testFile }
        val repository = ThemePreferencesRepository(dataStore, policy)
        val viewModel = ThemeViewModel(repository, policy, previewPolicy, externalScope = scope)
        return Pair(viewModel, repository)
    }

    @Test
    fun initialState_hasSkyAsActiveAndSelected() = runBlocking {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.first { it.activeTheme == MiMomentoThemeCatalog.SKY }
        assertEquals(MiMomentoThemeCatalog.SKY, state.activeTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertFalse(state.autoThemeEnabled)
        assertEquals(setOf(MiMomentoThemeId.SKY), state.autoThemeSelectedIds)
        assertFalse("With only SKY owned, cannot enable auto theme", state.canEnableAutoTheme)
    }

    @Test
    fun selectTheme_whenLocked_doesNotChangeSelection() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.uiState.first()

        viewModel.selectTheme(MiMomentoThemeId.DAWN) // DAWN is locked

        val state = viewModel.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state.activeTheme)
    }

    @Test
    fun selectTheme_whenOwned_updatesSelectedAndActiveTheme() = runBlocking {
        val allOwnedPolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = true
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = MiMomentoThemeId.values().toSet()
        }
        val (viewModel, _) = createViewModel(policy = allOwnedPolicy)
        viewModel.uiState.first()

        viewModel.selectTheme(MiMomentoThemeId.DAWN)

        val state = viewModel.uiState.first { it.selectedTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, state.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.DAWN, state.activeTheme)
    }

    @Test
    fun toggleAutoTheme_preventedWhenUnderTwoOwnedThemes() = runBlocking {
        val (viewModel, _) = createViewModel() // only SKY owned
        viewModel.uiState.first()

        viewModel.toggleAutoTheme(true)

        val state = viewModel.uiState.first()
        assertFalse(state.autoThemeEnabled)
    }

    @Test
    fun toggleAutoTheme_allowedWhenMultipleThemesOwned() = runBlocking {
        val multiPolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean =
                themeId == MiMomentoThemeId.SKY || themeId == MiMomentoThemeId.DAWN
            override fun getOwnedThemes(): Set<MiMomentoThemeId> =
                setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN)
        }
        val (viewModel, _) = createViewModel(policy = multiPolicy)

        val initialState = viewModel.uiState.first { it.canEnableAutoTheme }
        assertTrue(initialState.canEnableAutoTheme)

        viewModel.toggleAutoTheme(true)

        val updatedState = viewModel.uiState.first { it.autoThemeEnabled }
        assertTrue(updatedState.autoThemeEnabled)
    }

    // --- PRUEBAS DE PREVISUALIZACIÓN DEBUG (12 pruebas obligatorias) ---

    // 1. preview null -> activeTheme normal
    @Test
    fun preview_whenNull_activeThemeMatchesNormalSessionTheme() = runBlocking {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, state.activeTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertFalse(state.isDebugPreviewActive)
        assertEquals(null, state.debugPreviewThemeId)
    }

    // 2. debug preview SKY -> SKY
    @Test
    fun debugPreview_sky_updatesActiveThemeToSky() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.SKY)
        val state = viewModel.uiState.first { it.isDebugPreviewActive }
        assertEquals(MiMomentoThemeCatalog.SKY, state.activeTheme)
        assertEquals(MiMomentoThemeId.SKY, state.debugPreviewThemeId)
    }

    // 3. debug preview DAWN -> DAWN
    @Test
    fun debugPreview_dawn_updatesActiveThemeToDawn() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.DAWN)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, state.activeTheme)
        assertEquals(MiMomentoThemeId.DAWN, state.debugPreviewThemeId)
        assertTrue(state.isDebugPreviewActive)
    }

    // 4. debug preview NATURE -> NATURE
    @Test
    fun debugPreview_nature_updatesActiveThemeToNature() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.NATURE)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.NATURE }
        assertEquals(MiMomentoThemeCatalog.NATURE, state.activeTheme)
        assertEquals(MiMomentoThemeId.NATURE, state.debugPreviewThemeId)
        assertTrue(state.isDebugPreviewActive)
    }

    // 5. debug preview SCRIPTURE -> SCRIPTURE
    @Test
    fun debugPreview_scripture_updatesActiveThemeToScripture() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.SCRIPTURE)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.SCRIPTURE }
        assertEquals(MiMomentoThemeCatalog.SCRIPTURE, state.activeTheme)
        assertEquals(MiMomentoThemeId.SCRIPTURE, state.debugPreviewThemeId)
        assertTrue(state.isDebugPreviewActive)
    }

    // 6. debug preview SERENE -> SERENE
    @Test
    fun debugPreview_serene_updatesActiveThemeToSerene() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.SERENE)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.SERENE }
        assertEquals(MiMomentoThemeCatalog.SERENE, state.activeTheme)
        assertEquals(MiMomentoThemeId.SERENE, state.debugPreviewThemeId)
        assertTrue(state.isDebugPreviewActive)
    }

    // 7. preview no cambia selectedThemeId
    @Test
    fun debugPreview_doesNotChangeSelectedThemeId() = runBlocking {
        val (viewModel, repository) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.DAWN)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        val persisted = repository.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.SKY, persisted.selectedThemeId)
    }

    // 8. preview no cambia ownedThemes
    @Test
    fun debugPreview_doesNotChangeOwnedThemes() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.DAWN)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(setOf(MiMomentoThemeId.SKY), state.ownedThemeIds)
        assertFalse("DAWN must NOT become owned during preview", state.isThemeOwned(MiMomentoThemeId.DAWN))
    }

    // 9. preview no cambia rotation selection
    @Test
    fun debugPreview_doesNotChangeRotationSelection() = runBlocking {
        val (viewModel, repository) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.NATURE)
        val state = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.NATURE }
        assertEquals(setOf(MiMomentoThemeId.SKY), state.autoThemeSelectedIds)
        val persisted = repository.preferencesFlow.first()
        assertEquals(setOf(MiMomentoThemeId.SKY), persisted.autoThemeSelectedIds)
    }

    // 10. salir preview -> tema real
    @Test
    fun exitDebugPreview_returnsToRealSelectedTheme() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setDebugPreview(MiMomentoThemeId.SERENE)
        val previewState = viewModel.uiState.first { it.activeTheme.id == MiMomentoThemeId.SERENE }
        assertEquals(MiMomentoThemeCatalog.SERENE, previewState.activeTheme)

        viewModel.exitDebugPreview()
        val restoredState = viewModel.uiState.first { !it.isDebugPreviewActive }
        assertEquals(MiMomentoThemeCatalog.SKY, restoredState.activeTheme)
        assertEquals(null, restoredState.debugPreviewThemeId)
    }

    // 11. debugPreviewAllowed false -> premium no puede previsualizarse
    @Test
    fun debugPreview_whenDisallowed_doesNotActivatePreview() = runBlocking {
        val disallowedPolicy = object : DebugThemePreviewPolicy {
            override val isAllowed: Boolean = false
        }
        val (viewModel, _) = createViewModel(previewPolicy = disallowedPolicy)
        viewModel.setDebugPreview(MiMomentoThemeId.DAWN)
        val state = viewModel.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, state.activeTheme)
        assertFalse(state.isDebugPreviewActive)
        assertEquals(null, state.debugPreviewThemeId)
    }

    // 12. release policy conserva bloqueo premium
    @Test
    fun releasePolicy_keepsPremiumLockedAndPreviewDisabled() = runBlocking {
        val releasePolicy = object : DebugThemePreviewPolicy {
            override val isAllowed: Boolean = false
        }
        val (viewModel, _) = createViewModel(previewPolicy = releasePolicy)
        val state = viewModel.uiState.first()
        assertFalse(state.isDebugPreviewAllowed)
        assertFalse(state.isDebugPreviewActive)
        assertFalse(state.isThemeOwned(MiMomentoThemeId.DAWN))
        assertFalse(state.isThemeOwned(MiMomentoThemeId.NATURE))
        assertFalse(state.isThemeOwned(MiMomentoThemeId.SCRIPTURE))
        assertFalse(state.isThemeOwned(MiMomentoThemeId.SERENE))
        assertTrue(state.isThemeOwned(MiMomentoThemeId.SKY))
    }
}
