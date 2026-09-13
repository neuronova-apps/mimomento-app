package com.neuronova.mimomento.ui.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.neuronova.mimomento.data.model.TextScale
import com.neuronova.mimomento.data.repository.AccessibilityPreferencesRepository
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

class AccessibilityViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createViewModel(
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ): Pair<AccessibilityViewModel, AccessibilityPreferencesRepository> {
        val testFile = tempFolder.newFile("vm_a11y_test_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ) { testFile }
        val repository = AccessibilityPreferencesRepository(dataStore)
        val viewModel = AccessibilityViewModel(repository, externalScope = scope)
        return Pair(viewModel, repository)
    }

    @Test
    fun initialState_hasSafeDefaults() = runBlocking {
        val (viewModel, _) = createViewModel()
        val state = viewModel.uiState.first()

        assertEquals(TextScale.NORMAL, state.textScale)
        assertFalse(state.highContrast)
        assertFalse(state.reduceMotion)
        assertEquals(1.00f, state.effectiveMultiplier, 0.001f)
    }

    @Test
    fun setTextScale_updatesStateReactively() = runBlocking {
        val (viewModel, _) = createViewModel()

        viewModel.setTextScale(TextScale.LARGE)
        var state = viewModel.uiState.first { it.textScale == TextScale.LARGE }
        assertEquals(TextScale.LARGE, state.textScale)
        assertEquals(1.15f, state.effectiveMultiplier, 0.001f)

        viewModel.setTextScale(TextScale.VERY_LARGE)
        state = viewModel.uiState.first { it.textScale == TextScale.VERY_LARGE }
        assertEquals(TextScale.VERY_LARGE, state.textScale)
        assertEquals(1.30f, state.effectiveMultiplier, 0.001f)

        viewModel.setTextScale(TextScale.NORMAL)
        state = viewModel.uiState.first { it.textScale == TextScale.NORMAL }
        assertEquals(TextScale.NORMAL, state.textScale)
        assertEquals(1.00f, state.effectiveMultiplier, 0.001f)
    }

    @Test
    fun toggleHighContrast_updatesStateReactively() = runBlocking {
        val (viewModel, _) = createViewModel()

        assertFalse(viewModel.uiState.first().highContrast)

        viewModel.toggleHighContrast(true)
        var state = viewModel.uiState.first { it.highContrast }
        assertTrue(state.highContrast)

        viewModel.toggleHighContrast(false)
        state = viewModel.uiState.first { !it.highContrast }
        assertFalse(state.highContrast)
    }

    @Test
    fun toggleReduceMotion_updatesStateReactively() = runBlocking {
        val (viewModel, _) = createViewModel()

        assertFalse(viewModel.uiState.first().reduceMotion)

        viewModel.toggleReduceMotion(true)
        var state = viewModel.uiState.first { it.reduceMotion }
        assertTrue(state.reduceMotion)

        viewModel.toggleReduceMotion(false)
        state = viewModel.uiState.first { !it.reduceMotion }
        assertFalse(state.reduceMotion)
    }

    @Test
    fun uiState_toSettingsMaintainsValues() {
        val uiState = AccessibilityUiState(
            textScale = TextScale.VERY_LARGE,
            highContrast = true,
            reduceMotion = true,
        )
        val settings = uiState.toSettings()

        assertEquals(TextScale.VERY_LARGE, settings.textScale)
        assertTrue(settings.highContrast)
        assertTrue(settings.reduceMotion)
        assertEquals(1.30f, settings.effectiveMultiplier, 0.001f)
    }
}
