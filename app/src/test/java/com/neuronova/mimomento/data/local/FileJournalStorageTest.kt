package com.neuronova.mimomento.data.local

import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.JournalMood
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileJournalStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun readEntries_whenFileDoesNotExist_returnsEmptyList() = runBlocking {
        val nonExistentFile = tempFolder.root.resolve("non_existent.json")
        val storage = FileJournalStorage(nonExistentFile)

        val entries = storage.readEntries()
        assertTrue(entries.isEmpty())
    }

    @Test
    fun writeAndReadEntries_persistsAndRecoversCorrectly() = runBlocking {
        val storageFile = tempFolder.newFile("storage_test.json")
        val storage = FileJournalStorage(storageFile)

        val sampleEntries = listOf(
            JournalEntry(
                id = "id-1",
                text = "Entrada con estado de ánimo",
                mood = JournalMood.GOOD,
                createdAt = 1000L,
                updatedAt = 1000L,
            ),
            JournalEntry(
                id = "id-2",
                text = "Entrada sin emoción seleccionada",
                mood = null,
                createdAt = 2000L,
                updatedAt = 2500L,
            ),
        )

        storage.writeEntries(sampleEntries)

        val recovered = storage.readEntries()
        assertEquals(2, recovered.size)
        assertEquals("id-1", recovered[0].id)
        assertEquals("Entrada con estado de ánimo", recovered[0].text)
        assertEquals(JournalMood.GOOD, recovered[0].mood)
        assertEquals("id-2", recovered[1].id)
        assertEquals(null, recovered[1].mood)
        assertEquals(2500L, recovered[1].updatedAt)
    }

    @Test
    fun readEntries_whenFileIsCorrupted_recoversGracefullyWithEmptyList() = runBlocking {
        val corruptedFile = tempFolder.newFile("corrupt.json")
        corruptedFile.writeText("INVALID NOT A JSON {{{")
        val storage = FileJournalStorage(corruptedFile)

        val entries = storage.readEntries()
        assertTrue(entries.isEmpty())
    }
}
