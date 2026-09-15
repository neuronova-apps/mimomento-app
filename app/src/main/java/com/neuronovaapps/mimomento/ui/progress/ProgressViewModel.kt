package com.neuronovaapps.mimomento.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronovaapps.mimomento.data.model.ProgressSummary
import com.neuronovaapps.mimomento.data.repository.ProgressRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados de la pantalla de Progreso.
 */
sealed interface ProgressUiState {
    data object Loading : ProgressUiState
    data class Ready(
        val summary: ProgressSummary,
        val isEmpty: Boolean,
    ) : ProgressUiState
}

/**
 * ViewModel encargado de la lógica de presentación de la pantalla de Progreso.
 */
class ProgressViewModel(
    private val repository: ProgressRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgressUiState>(ProgressUiState.Loading)
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        observeProgress()
    }

    private fun observeProgress() {
        viewModelScope.launch(dispatcher) {
            repository.observeSummary().collect { summary ->
                _uiState.value = ProgressUiState.Ready(
                    summary = summary,
                    isEmpty = summary.isEmpty,
                )
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: ProgressRepository,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                    return ProgressViewModel(repository, dispatcher) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
