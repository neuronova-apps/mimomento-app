package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.local.FileJournalStorage
import com.neuronova.mimomento.data.model.JournalMood
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class JournalRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(
        storageFileName: String = "test_journal_${System.nanoTime()}.json",
        timeProvider: () -> Long = { System.currentTimeMillis() },
    ): Pair<LocalJournalRepository, FileJournalStorage> {
        val testFile = tempFolder.newFile(storageFileName)
        val storage = FileJournalStorage(testFile)
        val repository = LocalJournalRepository(
            storage = storage,
            timeProvider = timeProvider,
        )
        return Pair(repository, storage)
    }

    @Test
    fun createEntry_withValidTextAndNoMood_persistsSuccessfully() = runBlocking {
        val (repository, _) = createRepository()

        val result = repository.createEntry("Hoy fue un día de profunda paz y gratitud.", null)
        assertTrue(result.isSuccess)

        val entry = result.getOrThrow()
        assertEquals("Hoy fue un día de profunda paz y gratitud.", entry.text)
        assertNull(entry.mood)
        assertNotNull(entry.id)
        assertTrue(entry.createdAt > 0)
        assertEquals(entry.createdAt, entry.updatedAt)

        val list = repository.entriesFlow.first()
        assertEquals(1, list.size)
        assertEquals(entry.id, list[0].id)
    }

    @Test
    fun createEntry_withSelectedMood_persistsCorrectly() = runBlocking {
        val (repository, _) = createRepository()

        val result = repository.createEntry("Pude descansar y meditar con tranquilidad.", JournalMood.CALM)
        assertTrue(result.isSuccess)

        val entry = result.getOrThrow()
        assertEquals("Pude descansar y meditar con tranquilidad.", entry.text)
        assertEquals(JournalMood.CALM, entry.mood)

        val list = repository.entriesFlow.first()
        assertEquals(JournalMood.CALM, list[0].mood)
    }

    @Test
    fun createEntry_withEmptyOrBlankText_failsValidation() = runBlocking {
        val (repository, _) = createRepository()

        val resultEmpty = repository.createEntry("", JournalMood.GOOD)
        assertTrue(resultEmpty.isFailure)

        val resultBlank = repository.createEntry("   \n\t  ", JournalMood.TIRED)
        assertTrue(resultBlank.isFailure)

        val list = repository.entriesFlow.first()
        assertTrue(list.isEmpty())
    }

    @Test
    fun entries_areOrderedChronologicallyDescending() = runBlocking {
        var currentTime = 1000L
        val (repository, _) = createRepository(timeProvider = { currentTime })

        // Entrada 1 a t=1000
        currentTime = 1000L
        repository.createEntry("Primera entrada", null)

        // Entrada 2 a t=2000
        currentTime = 2000L
        repository.createEntry("Segunda entrada", JournalMood.GOOD)

        // Entrada 3 a t=3000
        currentTime = 3000L
        repository.createEntry("Tercera entrada más reciente", JournalMood.CHEERFUL)

        val entries = repository.entriesFlow.first()
        assertEquals(3, entries.size)
        assertEquals("Tercera entrada más reciente", entries[0].text)
        assertEquals("Segunda entrada", entries[1].text)
        assertEquals("Primera entrada", entries[2].text)
        assertTrue(entries[0].createdAt > entries[1].createdAt)
        assertTrue(entries[1].createdAt > entries[2].createdAt)
    }

    @Test
    fun updateEntry_modifiesTextAndMood_andUpdatesTimestamp() = runBlocking {
        var currentTime = 1000L
        val (repository, _) = createRepository(timeProvider = { currentTime })

        val created = repository.createEntry("Texto original", JournalMood.TIRED).getOrThrow()

        currentTime = 5000L
        val updateResult = repository.updateEntry(
            id = created.id,
            text = "Texto actualizado y corregido",
            mood = JournalMood.ANIMADO_OR_CHEERFUL(),
        )

        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("Texto actualizado y corregido", updated.text)
        assertEquals(JournalMood.CHEERFUL, updated.mood)
        assertEquals(1000L, updated.createdAt)
        assertEquals(5000L, updated.updatedAt)

        val currentInFlow = repository.entriesFlow.first()[0]
        assertEquals("Texto actualizado y corregido", currentInFlow.text)
        assertEquals(JournalMood.CHEERFUL, currentInFlow.mood)
        assertEquals(5000L, currentInFlow.updatedAt)
    }

    @Test
    fun updateEntry_withBlankText_fails() = runBlocking {
        val (repository, _) = createRepository()
        val created = repository.createEntry("Texto válido", null).getOrThrow()

        val updateResult = repository.updateEntry(created.id, "   ", null)
        assertTrue(updateResult.isFailure)

        val entry = repository.getEntryById(created.id)
        assertEquals("Texto válido", entry?.text)
    }

    @Test
    fun deleteEntry_removesItemPermanently() = runBlocking {
        val (repository, _) = createRepository()
        val entry1 = repository.createEntry("Entrada 1", null).getOrThrow()
        val entry2 = repository.createEntry("Entrada 2", JournalMood.GOOD).getOrThrow()

        assertEquals(2, repository.entriesFlow.first().size)

        val deleteResult = repository.deleteEntry(entry1.id)
        assertTrue(deleteResult.isSuccess)

        val remaining = repository.entriesFlow.first()
        assertEquals(1, remaining.size)
        assertEquals(entry2.id, remaining[0].id)
        assertNull(repository.getEntryById(entry1.id))
    }

    @Test
    fun deleteEntry_nonExistentId_returnsFailure() = runBlocking {
        val (repository, _) = createRepository()
        val deleteResult = repository.deleteEntry("non_existent_id")
        assertTrue(deleteResult.isFailure)
    }

    @Test
    fun persistence_reloadingFromFile_retainsAllData() = runBlocking {
        val testFileName = "persist_test_${System.nanoTime()}.json"
        val testFile = tempFolder.newFile(testFileName)
        val storage1 = FileJournalStorage(testFile)
        val repository1 = LocalJournalRepository(storage1)

        repository1.createEntry("Reflexión del amanecer", JournalMood.GOOD)
        repository1.createEntry("Agradecimiento nocturno", JournalMood.CALM)

        // Instanciar un nuevo repositorio leyendo del mismo archivo (simula reinicio de app)
        val storage2 = FileJournalStorage(testFile)
        val repository2 = LocalJournalRepository(storage2)

        val recovered = repository2.getEntries()
        assertEquals(2, recovered.size)
        assertEquals("Agradecimiento nocturno", recovered[0].text)
        assertEquals(JournalMood.CALM, recovered[0].mood)
        assertEquals("Reflexión del amanecer", recovered[1].text)
        assertEquals(JournalMood.GOOD, recovered[1].mood)
    }

    private fun JournalMood.Companion.ANIMADO_OR_CHEERFUL(): JournalMood = JournalMood.CHEERFUL
}
