package com.neuronova.mimomento.data.local

import androidx.core.util.AtomicFile
import com.neuronova.mimomento.data.model.JournalEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Implementación de almacenamiento local de entradas en archivo JSON utilizando AtomicFile.
 * Garantiza escrituras atómicas y previene corrupción de datos en caso de apagado o fallo inesperado.
 */
class FileJournalStorage(
    private val storageFile: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    },
) : JournalStorage {

    private val atomicFile = AtomicFile(storageFile)

    override suspend fun readEntries(): List<JournalEntry> = withContext(dispatcher) {
        if (!storageFile.exists()) {
            return@withContext emptyList()
        }
        try {
            val bytes = atomicFile.readFully()
            val text = bytes.decodeToString()
            if (text.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<JournalEntry>>(text)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun writeEntries(entries: List<JournalEntry>): Unit = withContext(dispatcher) {
        val serialized = json.encodeToString(entries)
        val bytes = serialized.encodeToByteArray()

        storageFile.parentFile?.mkdirs()

        var stream: FileOutputStream? = null
        try {
            stream = atomicFile.startWrite()
            stream.write(bytes)
            atomicFile.finishWrite(stream)
        } catch (e: IOException) {
            if (stream != null) {
                atomicFile.failWrite(stream)
            }
            throw e
        }
    }
}
