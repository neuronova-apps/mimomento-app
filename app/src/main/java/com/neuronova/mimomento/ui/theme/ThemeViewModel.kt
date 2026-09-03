package com.neuronova.mimomento.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import com.neuronova.mimomento.data.repository.DebugThemePreviewPolicy
import com.neuronova.mimomento.data.repository.DefaultDebugThemePreviewPolicy
import com.neuronova.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.data.repository.ThemePreferencesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.CoroutineScope

data class ThemeUiState(
    val activeTheme: MiMomentoThemeDefinition = MiMomentoThemeCatalog.DEFAULT_THEME,
    val selectedTheme: MiMomentoThemeDefinition = MiMomentoThemeCatalog.DEFAULT_THEME,
    val autoThemeEnabled: Boolean = false,
    val autoThemeSelectedIds: Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.SKY),
    val themes: List<MiMomentoThemeDefinition> = MiMomentoThemeCatalog.themes,
    val ownedThemeIds: Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.SKY),
    val canEnableAutoTheme: Boolean = false,
    val debugPreviewThemeId: MiMomentoThemeId? = null,
    val isDebugPreviewAllowed: Boolean = false,
) {
    val isDebugPreviewActive: Boolean
        get() = isDebugPreviewAllowed && debugPreviewThemeId != null

    fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = ownedThemeIds.contains(themeId)
}

class ThemeViewModel(
    private val repository: ThemePreferencesRepository,
    private val availabilityPolicy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
    private val previewPolicy: DebugThemePreviewPolicy = DefaultDebugThemePreviewPolicy(),
    externalScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val sessionThemeIdFlow = MutableStateFlow<MiMomentoThemeId?>(null)
    private val debugPreviewThemeIdFlow = MutableStateFlow<MiMomentoThemeId?>(null)

    val uiState: StateFlow<ThemeUiState> = combine(
        repository.preferencesFlow,
        sessionThemeIdFlow,
        debugPreviewThemeIdFlow,
    ) { prefs, sessionThemeId, previewThemeId ->
        val owned = availabilityPolicy.getOwnedThemes()
        val canAuto = owned.size >= 2

        val resolvedSessionThemeId = if (sessionThemeId == null) {
            val initial = repository.resolveSessionTheme(prefs, availabilityPolicy)
            sessionThemeIdFlow.value = initial
            initial
        } else {
            sessionThemeId
        }

        val effectiveThemeId = if (previewPolicy.isAllowed && previewThemeId != null) {
            previewThemeId
        } else {
            resolvedSessionThemeId
        }

        val activeDef = MiMomentoThemeCatalog.fromThemeId(effectiveThemeId)
        val selectedDef = MiMomentoThemeCatalog.fromThemeId(prefs.selectedThemeId)

        ThemeUiState(
            activeTheme = activeDef,
            selectedTheme = selectedDef,
            autoThemeEnabled = prefs.autoThemeEnabled,
            autoThemeSelectedIds = prefs.autoThemeSelectedIds,
            themes = MiMomentoThemeCatalog.themes,
            ownedThemeIds = owned,
            canEnableAutoTheme = canAuto,
            debugPreviewThemeId = if (previewPolicy.isAllowed) previewThemeId else null,
            isDebugPreviewAllowed = previewPolicy.isAllowed,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeUiState(),
    )

    fun selectTheme(themeId: MiMomentoThemeId) {
        if (!availabilityPolicy.isThemeOwned(themeId)) {
            return
        }
        sessionThemeIdFlow.value = themeId
        scope.launch {
            repository.setSelectedTheme(themeId)
        }
    }

    fun setDebugPreview(themeId: MiMomentoThemeId) {
        if (!previewPolicy.isAllowed) return
        debugPreviewThemeIdFlow.value = themeId
    }

    fun exitDebugPreview() {
        debugPreviewThemeIdFlow.value = null
    }

    fun clearDebugPreview() {
        exitDebugPreview()
    }

    fun toggleAutoTheme(enabled: Boolean) {
        val owned = availabilityPolicy.getOwnedThemes()
        if (enabled && owned.size < 2) {
            return
        }
        scope.launch {
            repository.setAutoThemeEnabled(enabled)
        }
    }

    fun toggleThemeForRotation(themeId: MiMomentoThemeId) {
        if (!availabilityPolicy.isThemeOwned(themeId)) {
            return
        }
        val currentSet = uiState.value.autoThemeSelectedIds.toMutableSet()
        if (currentSet.contains(themeId)) {
            if (currentSet.size > 1) {
                currentSet.remove(themeId)
            }
        } else {
            currentSet.add(themeId)
        }
        scope.launch {
            repository.setAutoThemeSelectedIds(currentSet)
        }
    }

    companion object {
        fun provideFactory(
            repository: ThemePreferencesRepository,
            availabilityPolicy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
            previewPolicy: DebugThemePreviewPolicy = DefaultDebugThemePreviewPolicy(),
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
                    return ThemeViewModel(repository, availabilityPolicy, previewPolicy) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
