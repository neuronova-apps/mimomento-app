package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.local.JournalStorage
import com.neuronova.mimomento.data.model.JournalEntry
import com.neuronova.mimomento.data.model.JournalMood
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Repositorio de dominio para gestionar las entradas de Diario.
 * Desacoplado del mecanismo de persistencia para permitir futuras migraciones (por ejemplo, a Room).
 */
interface JournalRepository {
    val entriesFlow: Flow<List<JournalEntry>>

    suspend fun getEntries(): List<JournalEntry>
    suspend fun getEntryById(id: String): JournalEntry?
    suspend fun createEntry(text: String, mood: JournalMood?): Result<JournalEntry>
    suspend fun updateEntry(id: String, text: String, mood: JournalMood?): Result<JournalEntry>
    suspend fun deleteEntry(id: String): Result<Unit>
    suspend fun refresh(): Unit
}

/**
 * Implementación local del repositorio respaldada por JournalStorage.
 */
class LocalJournalRepository(
    private val storage: JournalStorage,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
) : JournalRepository {

    private val mutex = Mutex()
    private val _entriesFlow = MutableStateFlow<List<JournalEntry>>(emptyList())
    override val entriesFlow: Flow<List<JournalEntry>> = _entriesFlow.asStateFlow()

    private var isInitialized = false

    private suspend fun ensureLoaded(): List<JournalEntry> {
        if (!isInitialized) {
            val loaded = storage.readEntries()
            val sorted = loaded.sortedByDescending { it.createdAt }
            _entriesFlow.value = sorted
            isInitialized = true
        }
        return _entriesFlow.value
    }

    override suspend fun refresh() {
        mutex.withLock {
            val loaded = storage.readEntries()
            _entriesFlow.value = loaded.sortedByDescending { it.createdAt }
            isInitialized = true
        }
    }

    override suspend fun getEntries(): List<JournalEntry> = mutex.withLock {
        ensureLoaded()
    }

    override suspend fun getEntryById(id: String): JournalEntry? = mutex.withLock {
        ensureLoaded().firstOrNull { it.id == id }
    }

    override suspend fun createEntry(text: String, mood: JournalMood?): Result<JournalEntry> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("El texto de la entrada no puede estar vacío."))
        }

        return mutex.withLock {
            ensureLoaded()
            val timestamp = timeProvider()
            val newEntry = JournalEntry(
                id = idGenerator(),
                text = trimmed,
                mood = mood,
                createdAt = timestamp,
                updatedAt = timestamp,
            )

            val updatedList = (listOf(newEntry) + _entriesFlow.value).sortedByDescending { it.createdAt }
            try {
                storage.writeEntries(updatedList)
                _entriesFlow.value = updatedList
                Result.success(newEntry)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateEntry(
        id: String,
        text: String,
        mood: JournalMood?,
    ): Result<JournalEntry> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("El texto de la entrada no puede estar vacío."))
        }

        return mutex.withLock {
            ensureLoaded()
            val currentList = _entriesFlow.value
            val existingIndex = currentList.indexOfFirst { it.id == id }
            if (existingIndex == -1) {
                return@withLock Result.failure(NoSuchElementException("No se encontró la entrada con ID: $id"))
            }

            val existing = currentList[existingIndex]
            val updatedEntry = existing.copy(
                text = trimmed,
                mood = mood,
                updatedAt = timeProvider(),
            )

            val mutable = currentList.toMutableList()
            mutable[existingIndex] = updatedEntry
            val sorted = mutable.sortedByDescending { it.createdAt }

            try {
                storage.writeEntries(sorted)
                _entriesFlow.value = sorted
                Result.success(updatedEntry)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteEntry(id: String): Result<Unit> {
        return mutex.withLock {
            ensureLoaded()
            val currentList = _entriesFlow.value
            val filtered = currentList.filterNot { it.id == id }
            if (filtered.size == currentList.size) {
                return@withLock Result.failure(NoSuchElementException("No se encontró la entrada con ID: $id"))
            }

            try {
                storage.writeEntries(filtered)
                _entriesFlow.value = filtered
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
