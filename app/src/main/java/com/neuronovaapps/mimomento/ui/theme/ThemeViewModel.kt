package com.neuronovaapps.mimomento.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronovaapps.mimomento.data.model.AppearanceMode
import com.neuronovaapps.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronovaapps.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronovaapps.mimomento.data.model.MiMomentoThemeId
import com.neuronovaapps.mimomento.data.repository.DebugThemePreviewPolicy
import com.neuronovaapps.mimomento.data.repository.DefaultDebugThemePreviewPolicy
import com.neuronovaapps.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronovaapps.mimomento.data.repository.ThemeAvailabilityPolicy
import com.neuronovaapps.mimomento.data.repository.ThemePreferencesRepository
import com.neuronovaapps.mimomento.data.repository.ThemePreferencesState
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
    val appearanceMode: AppearanceMode = AppearanceMode.SYSTEM,
    val autoThemeEnabled: Boolean = false,
    val autoThemeSelectedIds: Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.SKY),
    val themes: List<MiMomentoThemeDefinition> = MiMomentoThemeCatalog.themes,
    val ownedThemeIds: Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.SKY),
    val canEnableAutoTheme: Boolean = false,
    val debugPreviewThemeId: MiMomentoThemeId? = null,
    val isDebugPreviewAllowed: Boolean = false,
    val previewTheme: MiMomentoThemeDefinition? = null,
    val effectiveTheme: MiMomentoThemeDefinition = activeTheme,
) {
    val isPreviewActive: Boolean
        get() = previewTheme != null

    val isDebugPreviewActive: Boolean
        get() = isPreviewActive

    fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = ownedThemeIds.contains(themeId)
    fun isThemeUnlocked(themeId: MiMomentoThemeId): Boolean = isThemeOwned(themeId)
    fun canUseTheme(themeId: MiMomentoThemeId): Boolean = isThemeUnlocked(themeId)
}

class ThemeViewModel(
    private val repository: ThemePreferencesRepository,
    private val availabilityPolicy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
    private val previewPolicy: DebugThemePreviewPolicy = DefaultDebugThemePreviewPolicy(),
    externalScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope
    private val sessionThemeIdFlow = MutableStateFlow<MiMomentoThemeId?>(null)
    private val previewThemeIdFlow = MutableStateFlow<MiMomentoThemeId?>(null)

    val uiState: StateFlow<ThemeUiState> = combine(
        repository.preferencesFlow,
        sessionThemeIdFlow,
        previewThemeIdFlow,
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

        val effectiveThemeId = previewThemeId ?: resolvedSessionThemeId
        val effectiveDef = MiMomentoThemeCatalog.fromThemeId(effectiveThemeId)
        val selectedDef = MiMomentoThemeCatalog.fromThemeId(prefs.selectedThemeId)
        val previewDef = previewThemeId?.let { MiMomentoThemeCatalog.fromThemeId(it) }

        ThemeUiState(
            activeTheme = effectiveDef,
            selectedTheme = selectedDef,
            appearanceMode = prefs.appearanceMode,
            autoThemeEnabled = prefs.autoThemeEnabled,
            autoThemeSelectedIds = prefs.autoThemeSelectedIds,
            themes = MiMomentoThemeCatalog.themes,
            ownedThemeIds = owned,
            canEnableAutoTheme = canAuto,
            debugPreviewThemeId = previewThemeId,
            isDebugPreviewAllowed = previewPolicy.isAllowed,
            previewTheme = previewDef,
            effectiveTheme = effectiveDef,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeUiState(),
    )

    fun selectTheme(themeId: MiMomentoThemeId) {
        if (!availabilityPolicy.canUseTheme(themeId)) {
            return
        }
        previewThemeIdFlow.value = null
        sessionThemeIdFlow.value = themeId
        scope.launch {
            repository.setSelectedTheme(themeId)
        }
    }

    fun previewTheme(themeId: MiMomentoThemeId) {
        previewThemeIdFlow.value = themeId
    }

    fun clearThemePreview() {
        previewThemeIdFlow.value = null
    }

    fun confirmPreviewTheme(themeId: MiMomentoThemeId? = null): Boolean {
        val targetId = themeId ?: previewThemeIdFlow.value ?: return false
        if (!availabilityPolicy.canUseTheme(targetId)) {
            return false
        }
        sessionThemeIdFlow.value = targetId
        previewThemeIdFlow.value = null
        scope.launch {
            repository.setSelectedTheme(targetId)
        }
        return true
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        scope.launch {
            repository.setAppearanceMode(mode)
        }
    }

    fun setDebugPreview(themeId: MiMomentoThemeId) {
        if (!previewPolicy.isAllowed) return
        previewTheme(themeId)
    }

    fun exitDebugPreview() {
        clearThemePreview()
    }

    fun clearDebugPreview() {
        clearThemePreview()
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
