package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.TestContentFixture
import com.neuronova.mimomento.data.model.Devotional
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DevotionalSuggestionResolverTest {

    private fun createSampleDevotional(
        id: String,
        categoryId: String = "CAT-01",
        situationIds: List<String> = listOf("SIT-01"),
        autoRecommendation: String = "Sí",
        sensitivity: String = "Estándar",
        title: String = "Test Devotional $id",
    ): Devotional = Devotional(
        id = id,
        title = title,
        categoryId = categoryId,
        subthemeId = "SUB-01-01",
        situationIds = situationIds,
        bibleReference = "Salmo 23:1",
        bibleSourceId = "FUENTE-01",
        centralIdea = "Idea central",
        reflection = "Reflexión",
        personalQuestion = "Pregunta",
        prayerGuide = "Oración",
        dailyAction = "Acción",
        timeOfDay = "Cualquier momento",
        estimatedMinutes = 5,
        usageType = "Ambos",
        recommendationPriority = "Alta",
        tags = listOf("fe", "paz"),
        audience = "General",
        depth = "Inicial",
        reflectionWordCount = 200,
        sensitivity = sensitivity,
        autoRecommendation = autoRecommendation,
        lastReviewed = "2026-08-10",
        contentVersion = "1.0",
    )

    @Test
    fun resolve_bySituationId_returnsMatchingDevotional() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"), categoryId = "CAT-01")
        val dev2 = createSampleDevotional("DEV-002", situationIds = listOf("SIT-02"), categoryId = "CAT-02")
        val devotionals = listOf(dev1, dev2)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-02"),
            categoryIds = listOf("CAT-99"),
            devotionals = devotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-002", result?.id)
    }

    @Test
    fun resolve_byCategoryId_returnsMatchingDevotionalWhenNoSituationMatches() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"), categoryId = "CAT-01")
        val dev2 = createSampleDevotional("DEV-002", situationIds = listOf("SIT-02"), categoryId = "CAT-02")
        val devotionals = listOf(dev1, dev2)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-99"),
            categoryIds = listOf("CAT-02"),
            devotionals = devotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-002", result?.id)
    }

    @Test
    fun resolve_prioritizesSituationOverCategory() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"), categoryId = "CAT-02")
        val dev2 = createSampleDevotional("DEV-002", situationIds = listOf("SIT-02"), categoryId = "CAT-01")
        val devotionals = listOf(dev1, dev2)

        // Query has situation SIT-02 (matches dev2) and category CAT-02 (matches dev1)
        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-02"),
            categoryIds = listOf("CAT-02"),
            devotionals = devotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-002", result?.id)
    }

    @Test
    fun resolve_excludesDevotionalsWithManualReviewRequirement() {
        val dev1 = createSampleDevotional(
            id = "DEV-001",
            situationIds = listOf("SIT-01"),
            autoRecommendation = "Solo con revisión",
            sensitivity = "Sensible",
        )
        val dev2 = createSampleDevotional(
            id = "DEV-002",
            situationIds = listOf("SIT-01"),
            autoRecommendation = "Sí",
            sensitivity = "Estándar",
        )
        val devotionals = listOf(dev1, dev2)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-01"),
            categoryIds = emptyList(),
            devotionals = devotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-002", result?.id)
    }

    @Test
    fun resolve_excludesSensitiveDevotionals() {
        val dev1 = createSampleDevotional(
            id = "DEV-001",
            situationIds = listOf("SIT-01"),
            autoRecommendation = "Sí",
            sensitivity = "Sensible",
        )
        val devotionals = listOf(dev1)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-01"),
            categoryIds = emptyList(),
            devotionals = devotionals,
        )

        assertNull(result)
    }

    @Test
    fun resolve_returnsNullWhenNoEligibleCandidateFound() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"), categoryId = "CAT-01")
        val devotionals = listOf(dev1)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-99"),
            categoryIds = listOf("CAT-99"),
            devotionals = devotionals,
        )

        assertNull(result)
    }

    @Test
    fun resolve_returnsNullWhenEmptyCriteria() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"), categoryId = "CAT-01")
        val devotionals = listOf(dev1)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = emptyList(),
            categoryIds = emptyList(),
            devotionals = devotionals,
        )

        assertNull(result)
    }

    @Test
    fun resolve_stableTieBreaking_preservesCatalogOrder() {
        val dev1 = createSampleDevotional("DEV-001", situationIds = listOf("SIT-01"))
        val dev2 = createSampleDevotional("DEV-002", situationIds = listOf("SIT-01"))
        val dev3 = createSampleDevotional("DEV-003", situationIds = listOf("SIT-01"))
        val devotionals = listOf(dev1, dev2, dev3)

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = listOf("SIT-01"),
            categoryIds = emptyList(),
            devotionals = devotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-001", result?.id)
    }

    @Test
    fun resolve_realCatalog_allEligibleResultsBelongToEligible326Devotionals() {
        val realDevotionals = TestContentFixture.loaded.content.devotionals
        assertEquals(360, realDevotionals.size)

        val eligibleCount = realDevotionals.count {
            DevotionalSuggestionResolver.isEligibleForAutoRecommendation(it)
        }
        assertEquals(326, eligibleCount)

        val ineligibleCount = realDevotionals.count {
            !DevotionalSuggestionResolver.isEligibleForAutoRecommendation(it)
        }
        assertEquals(34, ineligibleCount)
    }

    @Test
    fun resolve_realCatalog_ME01_resolvesDEV0002() {
        val realDevotionals = TestContentFixture.loaded.content.devotionals
        val me01 = TestContentFixture.loaded.content.spiritualMoments.first { it.id == "ME-01" }

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = me01.situationIds,
            categoryIds = me01.categoryIds,
            devotionals = realDevotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-0002", result?.id)
        assertEquals("Cuando una decisión pesa", result?.title)
        assertTrue(result!!.situationIds.contains("SIT-03"))
        assertEquals("Sí", result.autoRecommendation)
        assertEquals("Estándar", result.sensitivity)
    }

    @Test
    fun resolve_realCatalog_GO01_resolvesDEV0004() {
        val realDevotionals = TestContentFixture.loaded.content.devotionals
        val go01 = TestContentFixture.loaded.content.prayerGuides.first { it.id == "GO-01" }

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = go01.situationIds,
            categoryIds = go01.categoryIds,
            devotionals = realDevotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-0004", result?.id)
        assertEquals("Cuando no veo el camino", result?.title)
        assertTrue(result!!.situationIds.contains("SIT-09"))
        assertEquals("Sí", result.autoRecommendation)
        assertEquals("Estándar", result.sensitivity)
    }

    @Test
    fun resolve_realCatalog_RO01_resolvesDEV0004() {
        val realDevotionals = TestContentFixture.loaded.content.devotionals
        val ro01 = TestContentFixture.loaded.content.prayerRoutes.first { it.id == "RO-01" }

        val result = DevotionalSuggestionResolver.resolve(
            situationIds = ro01.situationIds,
            categoryIds = ro01.categoryIds,
            devotionals = realDevotionals,
        )

        assertNotNull(result)
        assertEquals("DEV-0004", result?.id)
        assertEquals("Cuando no veo el camino", result?.title)
        assertTrue(result!!.situationIds.contains("SIT-09"))
        assertEquals("Sí", result.autoRecommendation)
        assertEquals("Estándar", result.sensitivity)
    }
}
