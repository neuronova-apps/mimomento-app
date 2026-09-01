package com.neuronova.mimomento.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AppContentUiState {
    data object Loading : AppContentUiState
    data class Ready(
        val devotionalCount: Int,
        val categoryCount: Int,
    ) : AppContentUiState
    data class Error(val message: String? = null) : AppContentUiState
}

class AppContentViewModel(
    private val repository: MiMomentoContentRepository,
    private val validator: MiMomentoContentValidator = MiMomentoContentValidator(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppContentUiState>(AppContentUiState.Loading)
    val uiState: StateFlow<AppContentUiState> = _uiState.asStateFlow()

    init {
        loadAndValidate()
    }

    fun loadAndValidate() {
        _uiState.value = AppContentUiState.Loading
        viewModelScope.launch(dispatcher) {
            try {
                val validation = repository.validate(validator)
                if (validation.isValid) {
                    _uiState.value = AppContentUiState.Ready(
                        devotionalCount = validation.statistics.devotionals,
                        categoryCount = validation.statistics.categories,
                    )
                } else {
                    _uiState.value = AppContentUiState.Error(
                        message = "Validación fallida (${validation.issues.size} problemas)",
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AppContentUiState.Error(
                    message = e.localizedMessage ?: "Error al cargar contenido",
                )
            }
        }
    }

    fun retry() {
        loadAndValidate()
    }

    companion object {
        fun provideFactory(
            repository: MiMomentoContentRepository,
            validator: MiMomentoContentValidator = MiMomentoContentValidator(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppContentViewModel::class.java)) {
                    return AppContentViewModel(repository, validator, dispatcher) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
