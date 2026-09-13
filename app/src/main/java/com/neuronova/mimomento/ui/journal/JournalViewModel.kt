package com.neuronova.mimomento.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.JournalMood
import com.neuronova.mimomento.data.repository.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isEditorOpen: Boolean = false,
    val editingEntryId: String? = null,
    val editorText: String = "",
    val editorMood: JournalMood? = null,
    val editorError: String? = null,
    val entryPendingDelete: JournalEntry? = null,
    val userMessage: String? = null,
)

class JournalViewModel(
    private val repository: JournalRepository,
    externalScope: kotlinx.coroutines.CoroutineScope? = null,
) : ViewModel() {

    private val scope: kotlinx.coroutines.CoroutineScope = externalScope ?: viewModelScope

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            repository.entriesFlow.collect { entries ->
                _uiState.update { currentState ->
                    currentState.copy(
                        entries = entries,
                        isLoading = false,
                    )
                }
            }
        }
        scope.launch {
            repository.refresh()
        }
    }

    fun openNewEntryEditor() {
        _uiState.update {
            it.copy(
                isEditorOpen = true,
                editingEntryId = null,
                editorText = "",
                editorMood = null,
                editorError = null,
            )
        }
    }

    fun openEditEntry(entry: JournalEntry) {
        _uiState.update {
            it.copy(
                isEditorOpen = true,
                editingEntryId = entry.id,
                editorText = entry.text,
                editorMood = entry.mood,
                editorError = null,
            )
        }
    }

    fun updateEditorText(text: String) {
        _uiState.update {
            it.copy(
                editorText = text,
                editorError = if (text.isNotBlank()) null else it.editorError,
            )
        }
    }

    fun selectMood(mood: JournalMood?) {
        _uiState.update {
            val newMood = if (it.editorMood == mood) null else mood
            it.copy(editorMood = newMood)
        }
    }

    fun closeEditor() {
        _uiState.update {
            it.copy(
                isEditorOpen = false,
                editingEntryId = null,
                editorText = "",
                editorMood = null,
                editorError = null,
            )
        }
    }

    fun saveEntry() {
        val currentState = _uiState.value
        val textToSave = currentState.editorText.trim()

        if (textToSave.isEmpty()) {
            _uiState.update { it.copy(editorError = "Escribe algo antes de guardar tu entrada.") }
            return
        }

        scope.launch {
            val result = if (currentState.editingEntryId != null) {
                repository.updateEntry(
                    id = currentState.editingEntryId,
                    text = textToSave,
                    mood = currentState.editorMood,
                )
            } else {
                repository.createEntry(
                    text = textToSave,
                    mood = currentState.editorMood,
                )
            }

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isEditorOpen = false,
                        editingEntryId = null,
                        editorText = "",
                        editorMood = null,
                        editorError = null,
                        userMessage = "Entrada guardada",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(editorError = error.message ?: "No se pudo guardar la entrada")
                }
            }
        }
    }

    fun requestDelete(entry: JournalEntry) {
        _uiState.update { it.copy(entryPendingDelete = entry) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(entryPendingDelete = null) }
    }

    fun confirmDelete() {
        val entry = _uiState.value.entryPendingDelete ?: return
        scope.launch {
            val result = repository.deleteEntry(entry.id)
            result.onSuccess {
                _uiState.update {
                    val closeEditor = it.editingEntryId == entry.id
                    it.copy(
                        entryPendingDelete = null,
                        isEditorOpen = if (closeEditor) false else it.isEditorOpen,
                        editingEntryId = if (closeEditor) null else it.editingEntryId,
                        userMessage = "Entrada eliminada",
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        entryPendingDelete = null,
                        userMessage = "No se pudo eliminar la entrada",
                    )
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun provideFactory(
            repository: JournalRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return JournalViewModel(repository) as T
            }
        }
    }
}
