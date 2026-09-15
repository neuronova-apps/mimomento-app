package com.neuronova.mimomento.data.local

import com.neuronova.mimomento.data.model.ProgressEvent

/**
 * Contrato de almacenamiento de bajo nivel para los eventos de progreso.
 * Encapsula la lectura y persistencia física para desacoplar el repositorio.
 */
interface ProgressStorage {
    suspend fun readEvents(): List<ProgressEvent>
    suspend fun writeEvents(events: List<ProgressEvent>)
}
