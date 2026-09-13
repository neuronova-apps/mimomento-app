package com.neuronova.mimomento.ui.journal

import com.neuronova.mimomento.data.local.JournalStorage
import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.JournalMood
import com.neuronova.mimomento.data.repository.LocalJournalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalViewModelTest {

    private class FakeJournalStorage(
        initialEntries: List<JournalEntry> = emptyList(),
    ) : JournalStorage {
        var entries: MutableList<JournalEntry> = initialEntries.toMutableList()

        override suspend fun readEntries(): List<JournalEntry> = entries.toList()

        override suspend fun writeEntries(entries: List<JournalEntry>) {
            this.entries = entries.toMutableList()
        }
    }

    private fun createViewModel(
        initialEntries: List<JournalEntry> = emptyList(),
    ): Pair<JournalViewModel, LocalJournalRepository> {
        val storage = FakeJournalStorage(initialEntries)
        val repository = LocalJournalRepository(storage)
        val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val viewModel = JournalViewModel(
            repository = repository,
            externalScope = testScope,
        )
        return Pair(viewModel, repository)
    }

    @Test
    fun initialState_loadsEntriesSuccessfully() = runBlocking {
        val entry = JournalEntry(
            id = "1",
            text = "Entrada inicial",
            mood = JournalMood.GOOD,
            createdAt = 1000L,
        )
        val (viewModel, _) = createViewModel(listOf(entry))

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(1, state.entries.size)
        assertEquals("Entrada inicial", state.entries[0].text)
        assertFalse(state.isEditorOpen)
        assertNull(state.entryPendingDelete)
    }

    @Test
    fun createEntryFlow_opensEditor_updatesTextAndMood_andSaves() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.openNewEntryEditor()
        var state = viewModel.uiState.value
        assertTrue(state.isEditorOpen)
        assertNull(state.editingEntryId)

        viewModel.updateEditorText("Momento de pausa y gratitud al final del día")
        viewModel.selectMood(JournalMood.CALM)

        state = viewModel.uiState.value
        assertEquals("Momento de pausa y gratitud al final del día", state.editorText)
        assertEquals(JournalMood.CALM, state.editorMood)

        viewModel.saveEntry()

        val savedState = viewModel.uiState.first { it.entries.isNotEmpty() }
        assertFalse(savedState.isEditorOpen)
        assertEquals(1, savedState.entries.size)
        assertEquals("Momento de pausa y gratitud al final del día", savedState.entries[0].text)
        assertEquals(JournalMood.CALM, savedState.entries[0].mood)
        assertEquals("Entrada guardada", savedState.userMessage)
    }

    @Test
    fun saveEntry_withEmptyText_showsValidationErrorAndDoesNotSave() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.openNewEntryEditor()
        viewModel.updateEditorText("     ")
        viewModel.saveEntry()

        val state = viewModel.uiState.value
        assertTrue(state.isEditorOpen)
        assertNotNull(state.editorError)
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun selectMood_togglesSelection_allowingDeselection() = runBlocking {
        val (viewModel, _) = createViewModel()

        viewModel.openNewEntryEditor()
        assertNull(viewModel.uiState.value.editorMood)

        // Seleccionar TRANQUILO
        viewModel.selectMood(JournalMood.CALM)
        assertEquals(JournalMood.CALM, viewModel.uiState.value.editorMood)

        // Tocar TRANQUILO de nuevo lo deselecciona
        viewModel.selectMood(JournalMood.CALM)
        assertNull(viewModel.uiState.value.editorMood)

        // Seleccionar ANIMADO
        viewModel.selectMood(JournalMood.CHEERFUL)
        assertEquals(JournalMood.CHEERFUL, viewModel.uiState.value.editorMood)
    }

    @Test
    fun editEntryFlow_populatesFields_andUpdatesEntry() = runBlocking {
        val entry = JournalEntry(
            id = "entry-123",
            text = "Texto antes de editar",
            mood = JournalMood.TIRED,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        val (viewModel, _) = createViewModel(listOf(entry))
        viewModel.uiState.first { !it.isLoading && it.entries.isNotEmpty() }

        viewModel.openEditEntry(entry)

        var state = viewModel.uiState.value
        assertTrue(state.isEditorOpen)
        assertEquals("entry-123", state.editingEntryId)
        assertEquals("Texto antes de editar", state.editorText)
        assertEquals(JournalMood.TIRED, state.editorMood)

        viewModel.updateEditorText("Texto editado con nueva perspectiva")
        viewModel.selectMood(JournalMood.CALM)
        viewModel.saveEntry()

        val updatedState = viewModel.uiState.first {
            it.entries.any { e -> e.text == "Texto editado con nueva perspectiva" }
        }
        assertFalse(updatedState.isEditorOpen)
        assertEquals("Texto editado con nueva perspectiva", updatedState.entries[0].text)
        assertEquals(JournalMood.CALM, updatedState.entries[0].mood)
        assertEquals("Entrada guardada", updatedState.userMessage)
    }

    @Test
    fun deleteConfirmationFlow_cancelAndConfirm() = runBlocking {
        val entry = JournalEntry(
            id = "del-1",
            text = "Entrada a eliminar",
            mood = null,
            createdAt = 1000L,
        )
        val (viewModel, _) = createViewModel(listOf(entry))
        viewModel.uiState.first { !it.isLoading && it.entries.isNotEmpty() }

        // Solicitar eliminación
        viewModel.requestDelete(entry)
        assertEquals(entry, viewModel.uiState.value.entryPendingDelete)

        // Cancelar eliminación
        viewModel.cancelDelete()
        assertNull(viewModel.uiState.value.entryPendingDelete)
        assertEquals(1, viewModel.uiState.value.entries.size)

        // Solicitar y confirmar eliminación
        viewModel.requestDelete(entry)
        viewModel.confirmDelete()

        val emptyState = viewModel.uiState.first { it.entries.isEmpty() }
        assertNull(emptyState.entryPendingDelete)
        assertEquals("Entrada eliminada", emptyState.userMessage)
    }

    @Test
    fun closeEditor_resetsFieldsWithoutSaving() = runBlocking {
        val (viewModel, _) = createViewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.openNewEntryEditor()
        viewModel.updateEditorText("Borrador sin guardar")
        viewModel.selectMood(JournalMood.GOOD)

        viewModel.closeEditor()

        val state = viewModel.uiState.value
        assertFalse(state.isEditorOpen)
        assertEquals("", state.editorText)
        assertNull(state.editorMood)
        assertTrue(state.entries.isEmpty())
    }
}
