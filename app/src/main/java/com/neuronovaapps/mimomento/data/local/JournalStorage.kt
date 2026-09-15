package com.neuronovaapps.mimomento.data.local

import com.neuronovaapps.mimomento.data.model.JournalEntry

/**
 * Contrato de almacenamiento de bajo nivel para las entradas de diario.
 * Encapsula la lectura y persistencia física para desacoplar el repositorio.
 */
interface JournalStorage {
    suspend fun readEntries(): List<JournalEntry>
    suspend fun writeEntries(entries: List<JournalEntry>)
}
