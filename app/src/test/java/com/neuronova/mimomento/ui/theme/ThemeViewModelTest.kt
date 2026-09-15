package com.neuronova.mimomento.ui.theme

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.neuronova.mimomento.data.model.AccessibilitySettings
import com.neuronova.mimomento.data.model.AppearanceMode
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import com.neuronova.mimomento.data.model.TextScale
import com.neuronova.mimomento.data.model.resolveIsDarkTheme
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    // --- PRUEBAS DE MODO DE APARIENCIA ---

    @Test
    fun initialState_hasSystemAppearanceMode() = runBlocking {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.first()
        assertEquals(AppearanceMode.SYSTEM, state.appearanceMode)
    }

    @Test
    fun setAppearanceMode_updatesUiStateImmediately() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.setAppearanceMode(AppearanceMode.NIGHT)
        val stateNight = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(AppearanceMode.NIGHT, stateNight.appearanceMode)

        viewModel.setAppearanceMode(AppearanceMode.DAY)
        val stateDay = viewModel.uiState.first { it.appearanceMode == AppearanceMode.DAY }
        assertEquals(AppearanceMode.DAY, stateDay.appearanceMode)
    }

    @Test
    fun setAppearanceMode_doesNotAlterSelectedOrActiveTheme() = runBlocking {
        val (viewModel, _) = createViewModel()
        val initial = viewModel.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, initial.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, initial.activeTheme)

        viewModel.setAppearanceMode(AppearanceMode.NIGHT)
        val updated = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(MiMomentoThemeCatalog.SKY, updated.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, updated.activeTheme)
    }

    @Test
    fun selectTheme_doesNotAlterAppearanceMode() = runBlocking {
        val allOwnedPolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = true
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = MiMomentoThemeId.values().toSet()
        }
        val (viewModel, _) = createViewModel(policy = allOwnedPolicy)
        viewModel.setAppearanceMode(AppearanceMode.NIGHT)
        val stateNight = viewModel.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }
        assertEquals(AppearanceMode.NIGHT, stateNight.appearanceMode)

        viewModel.selectTheme(MiMomentoThemeId.DAWN)
        val stateDawn = viewModel.uiState.first { it.selectedTheme.id == MiMomentoThemeId.DAWN }
        assertEquals("AppearanceMode must remain NIGHT after selecting DAWN", AppearanceMode.NIGHT, stateDawn.appearanceMode)
        assertEquals(MiMomentoThemeCatalog.DAWN, stateDawn.selectedTheme)
    }

    // =========================================================================
    // --- 23 PRUEBAS ESPECÍFICAS DE SEPARACIÓN PREVIEW / PERSISTENCIA ---
    // =========================================================================

    // 1. selectedTheme se recupera de DataStore.
    @Test
    fun test01_selectedTheme_isRecoveredFromDataStore() = runBlocking {
        val allOwned = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = true
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = MiMomentoThemeId.values().toSet()
        }
        val (vm1, repo) = createViewModel(policy = allOwned)
        vm1.selectTheme(MiMomentoThemeId.DAWN)
        vm1.uiState.first { it.selectedTheme.id == MiMomentoThemeId.DAWN }

        val vm2 = ThemeViewModel(
            repository = repo,
            availabilityPolicy = allOwned,
            previewPolicy = DefaultDebugThemePreviewPolicy(isAllowed = true),
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        )
        val state = vm2.uiState.first { it.selectedTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, state.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.DAWN, state.effectiveTheme)
    }

    // 2. previewTheme empieza null.
    @Test
    fun test02_previewTheme_startsAsNull() = runBlocking {
        val (vm, _) = createViewModel()
        val state = vm.uiState.first()
        assertNull("previewTheme must initially be null", state.previewTheme)
        assertFalse("isPreviewActive must initially be false", state.isPreviewActive)
    }

    // 3. previewTheme modifica effectiveTheme.
    @Test
    fun test03_previewTheme_modifiesEffectiveTheme() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.DAWN)

        val state = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, state.previewTheme)
        assertEquals(MiMomentoThemeCatalog.DAWN, state.effectiveTheme)
        assertTrue(state.isPreviewActive)
    }

    // 4. previewTheme no modifica selectedTheme.
    @Test
    fun test04_previewTheme_doesNotModifySelectedTheme() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.NATURE)

        val state = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.NATURE }
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.NATURE, state.effectiveTheme)
    }

    // 5. previewTheme no se guarda en DataStore.
    @Test
    fun test05_previewTheme_isNotSavedToDataStore() = runBlocking {
        val (vm, repo) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.SCRIPTURE)

        vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.SCRIPTURE }
        val persisted = repo.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.SKY, persisted.selectedThemeId)
    }

    // 6. clearPreview restaura selectedTheme.
    @Test
    fun test06_clearThemePreview_restoresSelectedTheme() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.SERENE)

        val previewState = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.SERENE }
        assertEquals(MiMomentoThemeCatalog.SERENE, previewState.effectiveTheme)

        vm.clearThemePreview()
        val restoredState = vm.uiState.first { it.previewTheme == null }
        assertEquals(MiMomentoThemeCatalog.SKY, restoredState.effectiveTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, restoredState.selectedTheme)
        assertNull(restoredState.previewTheme)
        assertFalse(restoredState.isPreviewActive)
    }

    // 7. process recreation elimina previewTheme.
    @Test
    fun test07_processRecreation_eliminatesPreviewTheme() = runBlocking {
        val testFile = tempFolder.newFile("vm_recreation_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ) { testFile }
        val repo = ThemePreferencesRepository(dataStore)
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val vm1 = ThemeViewModel(
            repository = repo,
            externalScope = testScope,
        )
        vm1.previewTheme(MiMomentoThemeId.DAWN)
        vm1.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }

        // Simula nueva instancia de ViewModel tras process death
        val vm2 = ThemeViewModel(
            repository = repo,
            externalScope = testScope,
        )
        val state2 = vm2.uiState.first()
        assertNull("After recreation, previewTheme must be null", state2.previewTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state2.effectiveTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state2.selectedTheme)
    }

    // 8. tema gratuito puede persistirse normalmente.
    @Test
    fun test08_freeTheme_persistsNormally() = runBlocking {
        val (vm, repo) = createViewModel()
        vm.selectTheme(MiMomentoThemeId.SKY)

        val state = vm.uiState.first { it.selectedTheme.id == MiMomentoThemeId.SKY }
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertEquals(MiMomentoThemeId.SKY, repo.preferencesFlow.first().selectedThemeId)
    }

    // 9. tema premium bloqueado puede previsualizarse.
    @Test
    fun test09_lockedPremiumTheme_canBePreviewed() = runBlocking {
        val (vm, _) = createViewModel() // Default policy: SKY owned, DAWN locked
        val initialState = vm.uiState.first()
        assertFalse(initialState.canUseTheme(MiMomentoThemeId.DAWN))

        vm.previewTheme(MiMomentoThemeId.DAWN)
        val state = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, state.effectiveTheme)
        assertTrue(state.isPreviewActive)
    }

    // 10. tema premium bloqueado no puede persistirse.
    @Test
    fun test10_lockedPremiumTheme_cannotBePersisted() = runBlocking {
        val (vm, repo) = createViewModel()

        // Intento 1: selectTheme directo en tema bloqueado
        vm.selectTheme(MiMomentoThemeId.DAWN)
        val stateAfterSelect = vm.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, stateAfterSelect.selectedTheme)
        assertEquals(MiMomentoThemeId.SKY, repo.preferencesFlow.first().selectedThemeId)

        // Intento 2: confirmPreviewTheme en tema bloqueado
        vm.previewTheme(MiMomentoThemeId.DAWN)
        vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }
        val confirmed = vm.confirmPreviewTheme()
        assertFalse("confirmPreviewTheme must return false for locked theme", confirmed)
        val stateAfterConfirm = vm.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, stateAfterConfirm.selectedTheme)
        assertEquals(MiMomentoThemeId.SKY, repo.preferencesFlow.first().selectedThemeId)
        assertEquals(MiMomentoThemeCatalog.DAWN, stateAfterConfirm.previewTheme)
    }

    // 11. confirmPreview con entitlement válido persiste tema.
    @Test
    fun test11_confirmPreview_withValidEntitlement_persistsTheme() = runBlocking {
        val policyWithDawn = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean =
                themeId == MiMomentoThemeId.SKY || themeId == MiMomentoThemeId.DAWN
            override fun getOwnedThemes(): Set<MiMomentoThemeId> =
                setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN)
        }
        val (vm, repo) = createViewModel(policy = policyWithDawn)

        vm.previewTheme(MiMomentoThemeId.DAWN)
        vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }

        val confirmed = vm.confirmPreviewTheme()
        assertTrue("confirmPreviewTheme must return true for owned theme", confirmed)

        val updatedState = vm.uiState.first { it.selectedTheme.id == MiMomentoThemeId.DAWN }
        assertEquals(MiMomentoThemeCatalog.DAWN, updatedState.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.DAWN, updatedState.effectiveTheme)
        assertNull(updatedState.previewTheme)
        assertEquals(MiMomentoThemeId.DAWN, repo.preferencesFlow.first().selectedThemeId)
    }

    // 12. confirmPreview sin entitlement mantiene selectedTheme.
    @Test
    fun test12_confirmPreview_withoutEntitlement_maintainsSelectedTheme() = runBlocking {
        val (vm, repo) = createViewModel() // only SKY owned
        vm.previewTheme(MiMomentoThemeId.NATURE)
        vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.NATURE }

        val confirmed = vm.confirmPreviewTheme()
        assertFalse("confirmPreviewTheme must fail without entitlement", confirmed)

        val state = vm.uiState.first()
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
        assertEquals(MiMomentoThemeCatalog.NATURE, state.previewTheme)
        assertEquals(MiMomentoThemeCatalog.NATURE, state.effectiveTheme)
        assertEquals(MiMomentoThemeId.SKY, repo.preferencesFlow.first().selectedThemeId)
    }

    // 13. selectedTheme y previewTheme son estados diferentes.
    @Test
    fun test13_selectedTheme_and_previewTheme_areIndependentStates() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.SERENE)

        val state = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.SERENE }
        assertNotNull(state.selectedTheme)
        assertNotNull(state.previewTheme)
        assertNotEquals(state.selectedTheme.id, state.previewTheme?.id)
        assertEquals(MiMomentoThemeId.SKY, state.selectedTheme.id)
        assertEquals(MiMomentoThemeId.SERENE, state.previewTheme?.id)
    }

    // 14. appearanceMode se conserva durante preview.
    @Test
    fun test14_appearanceMode_isPreservedDuringPreview() = runBlocking {
        val (vm, _) = createViewModel()
        vm.setAppearanceMode(AppearanceMode.NIGHT)
        vm.uiState.first { it.appearanceMode == AppearanceMode.NIGHT }

        vm.previewTheme(MiMomentoThemeId.DAWN)
        val previewState = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.DAWN }
        assertEquals(AppearanceMode.NIGHT, previewState.appearanceMode)

        vm.clearThemePreview()
        val clearedState = vm.uiState.first { it.previewTheme == null }
        assertEquals(AppearanceMode.NIGHT, clearedState.appearanceMode)
    }

    // 15. HC se conserva durante preview.
    @Test
    fun test15_highContrast_isPreservedDuringPreview() {
        val settings = AccessibilitySettings(highContrast = true)
        assertTrue(settings.highContrast)

        val theme = MiMomentoThemeCatalog.NATURE
        val visualHC = theme.resolveVisual(isDarkTheme = false, highContrast = true)
        assertEquals("HC surface must be pure black", androidx.compose.ui.graphics.Color(0xFF000000), visualHC.surface)
        assertTrue(settings.highContrast)
    }

    // 16. salir de HC recupera preview cuando corresponda.
    @Test
    fun test16_exitingHighContrast_retainsPreviewTheme() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.NATURE)

        val state = vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.NATURE }
        assertEquals(MiMomentoThemeCatalog.NATURE, state.previewTheme)

        val visualNormal = state.effectiveTheme.resolveVisual(isDarkTheme = false, highContrast = false)
        val visualHC = state.effectiveTheme.resolveVisual(isDarkTheme = false, highContrast = true)

        assertNotEquals(visualNormal.surface, visualHC.surface)
        assertEquals(MiMomentoThemeCatalog.NATURE, state.previewTheme)
    }

    // 17. salir de preview recupera selectedTheme.
    @Test
    fun test17_exitingPreview_recoversSelectedTheme() = runBlocking {
        val (vm, _) = createViewModel()
        vm.previewTheme(MiMomentoThemeId.SCRIPTURE)

        vm.uiState.first { it.previewTheme?.id == MiMomentoThemeId.SCRIPTURE }
        vm.clearThemePreview()

        val state = vm.uiState.first { it.previewTheme == null }
        assertEquals(MiMomentoThemeCatalog.SKY, state.effectiveTheme)
        assertEquals(MiMomentoThemeCatalog.SKY, state.selectedTheme)
    }

    // 18. compatible con DAY.
    @Test
    fun test18_compatibleWithDayMode() {
        val isDark = resolveIsDarkTheme(AppearanceMode.DAY, systemIsDark = false)
        assertFalse("DAY mode must resolve to light", isDark)
        val visual = MiMomentoThemeCatalog.NATURE.resolveVisual(isDarkTheme = isDark, highContrast = false)
        assertNotNull(visual)
    }

    // 19. compatible con NIGHT.
    @Test
    fun test19_compatibleWithNightMode() {
        val isDark = resolveIsDarkTheme(AppearanceMode.NIGHT, systemIsDark = false)
        assertTrue("NIGHT mode must resolve to dark", isDark)
        val visual = MiMomentoThemeCatalog.NATURE.resolveVisual(isDarkTheme = isDark, highContrast = false)
        assertNotNull(visual)
    }

    // 20. compatible con SYSTEM.
    @Test
    fun test20_compatibleWithSystemMode() {
        val darkFromSystem = resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = true)
        val lightFromSystem = resolveIsDarkTheme(AppearanceMode.SYSTEM, systemIsDark = false)
        assertTrue(darkFromSystem)
        assertFalse(lightFromSystem)
    }

    // 21. compatible con Normal.
    @Test
    fun test21_compatibleWithNormalTextScale() {
        val normal = TextScale.NORMAL
        assertEquals(1.00f, normal.multiplier, 0.001f)
        val settings = AccessibilitySettings(textScale = normal)
        assertEquals(1.00f, settings.effectiveMultiplier, 0.001f)
    }

    // 22. compatible con Grande.
    @Test
    fun test22_compatibleWithLargeTextScale() {
        val large = TextScale.LARGE
        assertEquals(1.15f, large.multiplier, 0.001f)
        val settings = AccessibilitySettings(textScale = large)
        assertEquals(1.15f, settings.effectiveMultiplier, 0.001f)
    }

    // 23. compatible con Muy grande.
    @Test
    fun test23_compatibleWithVeryLargeTextScale() {
        val veryLarge = TextScale.VERY_LARGE
        assertEquals(1.30f, veryLarge.multiplier, 0.001f)
        val settings = AccessibilitySettings(textScale = veryLarge)
        assertEquals(1.30f, settings.effectiveMultiplier, 0.001f)
    }
}
