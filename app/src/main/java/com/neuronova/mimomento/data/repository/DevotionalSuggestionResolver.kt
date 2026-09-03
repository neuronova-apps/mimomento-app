package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.model.Devotional

object DevotionalSuggestionResolver {

    /**
     * Resuelve de forma determinística y transparente un devocional sugerido
     * a partir de los identificadores de situación y categoría del elemento de oración.
     *
     * Reglas:
     * 1. Solo devocionales aptos para recomendación automática (autoRecommendation == "Sí" y sensitivity == "Estándar").
     * 2. Prioridad de coincidencia por situación (situationIds).
     * 3. Si no hay coincidencia por situación, coincidencia por categoría (categoryId).
     * 4. Desempate estable según el orden original del catálogo.
     * 5. Retorna null si no existe candidato seguro.
     */
    fun resolve(
        situationIds: List<String>,
        categoryIds: List<String>,
        devotionals: List<Devotional>,
    ): Devotional? {
        val eligibleDevotionals = devotionals.filter { devotional ->
            isEligibleForAutoRecommendation(devotional)
        }

        if (situationIds.isNotEmpty()) {
            val bySituation = eligibleDevotionals.firstOrNull { devotional ->
                devotional.situationIds.any { sitId -> situationIds.contains(sitId) }
            }
            if (bySituation != null) {
                return bySituation
            }
        }

        if (categoryIds.isNotEmpty()) {
            val byCategory = eligibleDevotionals.firstOrNull { devotional ->
                categoryIds.contains(devotional.categoryId)
            }
            if (byCategory != null) {
                return byCategory
            }
        }

        return null
    }

    /**
     * Verifica si un devocional es apto para recomendación automática sin revisión manual.
     */
    fun isEligibleForAutoRecommendation(devotional: Devotional): Boolean {
        return devotional.autoRecommendation.equals("Sí", ignoreCase = true) &&
            devotional.sensitivity.equals("Estándar", ignoreCase = true)
    }
}
