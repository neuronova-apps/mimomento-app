package com.neuronovaapps.mimomento.data.local

import com.neuronovaapps.mimomento.data.model.ProgressEvent
import com.neuronovaapps.mimomento.data.model.ProgressEventType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileProgressStorageTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun readEvents_whenFileDoesNotExist_returnsEmptyList() = runBlocking {
        val nonExistentFile = tempFolder.root.resolve("non_existent_progress.json")
        val storage = FileProgressStorage(nonExistentFile)

        val events = storage.readEvents()
        assertTrue(events.isEmpty())
    }

    @Test
    fun writeAndReadEvents_persistsAndRecoversCorrectly() = runBlocking {
        val storageFile = tempFolder.newFile("progress_storage_test.json")
        val storage = FileProgressStorage(storageFile)

        val sampleEvents = listOf(
            ProgressEvent(
                id = "evt-1",
                type = ProgressEventType.DEVOTIONAL_OPENED,
                timestamp = 1000L,
                referenceId = "dev-101",
            ),
            ProgressEvent(
                id = "evt-2",
                type = ProgressEventType.DEVOTIONAL_COMPLETED,
                timestamp = 2000L,
                referenceId = "dev-101",
            ),
            ProgressEvent(
                id = "evt-3",
                type = ProgressEventType.JOURNAL_CREATED,
                timestamp = 3000L,
                referenceId = "journal-1",
            ),
        )

        storage.writeEvents(sampleEvents)

        val recovered = storage.readEvents()
        assertEquals(3, recovered.size)
        assertEquals("evt-1", recovered[0].id)
        assertEquals(ProgressEventType.DEVOTIONAL_OPENED, recovered[0].type)
        assertEquals("dev-101", recovered[0].referenceId)
        assertEquals("evt-2", recovered[1].id)
        assertEquals(ProgressEventType.DEVOTIONAL_COMPLETED, recovered[1].type)
        assertEquals("evt-3", recovered[2].id)
        assertEquals(ProgressEventType.JOURNAL_CREATED, recovered[2].type)
    }

    @Test
    fun readEvents_whenFileIsCorrupted_recoversGracefullyWithEmptyList() = runBlocking {
        val corruptedFile = tempFolder.newFile("corrupt_progress.json")
        corruptedFile.writeText("INVALID JSON CONTENT NOT A LIST {[[")
        val storage = FileProgressStorage(corruptedFile)

        val events = storage.readEvents()
        assertTrue(events.isEmpty())
    }

    @Test
    fun writeEvents_overwritesAtomically() = runBlocking {
        val storageFile = tempFolder.newFile("overwrite_test.json")
        val storage = FileProgressStorage(storageFile)

        storage.writeEvents(
            listOf(
                ProgressEvent(
                    id = "init-1",
                    type = ProgressEventType.PRAYER_OPENED,
                    timestamp = 500L,
                    referenceId = "guide-1",
                ),
            ),
        )

        assertEquals(1, storage.readEvents().size)

        val updated = listOf(
            ProgressEvent(
                id = "init-1",
                type = ProgressEventType.PRAYER_OPENED,
                timestamp = 500L,
                referenceId = "guide-1",
            ),
            ProgressEvent(
                id = "init-2",
                type = ProgressEventType.DEVOTIONAL_OPENED,
                timestamp = 1500L,
                referenceId = "dev-1",
            ),
        )
        storage.writeEvents(updated)

        val finalRecovered = storage.readEvents()
        assertEquals(2, finalRecovered.size)
        assertEquals("init-2", finalRecovered[1].id)
    }
}
