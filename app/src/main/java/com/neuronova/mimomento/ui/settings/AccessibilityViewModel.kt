package com.neuronova.mimomento.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.model.AccessibilitySettings
import com.neuronova.mimomento.data.model.TextScale
import com.neuronova.mimomento.data.repository.AccessibilityPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccessibilityUiState(
    val textScale: TextScale = TextScale.NORMAL,
    val highContrast: Boolean = false,
    val reduceMotion: Boolean = false,
) {
    val effectiveMultiplier: Float
        get() = textScale.multiplier.coerceAtLeast(1.00f)

    fun toSettings(): AccessibilitySettings = AccessibilitySettings(
        textScale = textScale,
        highContrast = highContrast,
        reduceMotion = reduceMotion,
    )
}

class AccessibilityViewModel(
    private val repository: AccessibilityPreferencesRepository,
    externalScope: CoroutineScope? = null,
) : ViewModel() {

    private val scope: CoroutineScope = externalScope ?: viewModelScope

    val uiState: StateFlow<AccessibilityUiState> = repository.settingsFlow
        .map { settings ->
            AccessibilityUiState(
                textScale = settings.textScale,
                highContrast = settings.highContrast,
                reduceMotion = settings.reduceMotion,
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AccessibilityUiState(),
        )

    fun setTextScale(textScale: TextScale) {
        scope.launch {
            repository.setTextScale(textScale)
        }
    }

    fun setHighContrast(enabled: Boolean) {
        scope.launch {
            repository.setHighContrast(enabled)
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        setHighContrast(enabled)
    }

    fun setReduceMotion(enabled: Boolean) {
        scope.launch {
            repository.setReduceMotion(enabled)
        }
    }

    fun toggleReduceMotion(enabled: Boolean) {
        setReduceMotion(enabled)
    }

    companion object {
        fun provideFactory(
            repository: AccessibilityPreferencesRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AccessibilityViewModel::class.java)) {
                    return AccessibilityViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
