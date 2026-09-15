package com.neuronovaapps.mimomento.data.local

import androidx.core.util.AtomicFile
import com.neuronovaapps.mimomento.data.model.ProgressEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Implementación de almacenamiento local de eventos de progreso en archivo JSON utilizando AtomicFile.
 * Garantiza escrituras atómicas y previene corrupción de datos ante cierres inesperados.
 */
class FileProgressStorage(
    private val storageFile: File,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        encodeDefaults = true
    },
) : ProgressStorage {

    private val atomicFile = AtomicFile(storageFile)

    override suspend fun readEvents(): List<ProgressEvent> = withContext(dispatcher) {
        if (!storageFile.exists()) {
            return@withContext emptyList()
        }
        try {
            val bytes = atomicFile.readFully()
            val text = bytes.decodeToString()
            if (text.isBlank()) {
                emptyList()
            } else {
                json.decodeFromString<List<ProgressEvent>>(text)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun writeEvents(events: List<ProgressEvent>): Unit = withContext(dispatcher) {
        val serialized = json.encodeToString(events)
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
