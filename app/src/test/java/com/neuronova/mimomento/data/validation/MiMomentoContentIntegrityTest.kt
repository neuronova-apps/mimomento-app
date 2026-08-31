package com.neuronova.mimomento.data.validation

import com.neuronova.mimomento.data.TestContentFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiMomentoContentIntegrityTest {
    private val content get() = TestContentFixture.loaded.content
    private val result get() = TestContentFixture.validation

    @Test
    fun completeValidationPassesWithoutBrokenRelationships() {
        assertTrue(result.issues.joinToString { issue -> "${issue.code}: ${issue.message}" }, result.isValid)
        assertEquals(0, result.statistics.brokenRelationships)
    }

    @Test
    fun countsMatchTheAuditedContract() {
        with(result.statistics) {
            assertEquals(360, devotionals)
            assertEquals(12, categories)
            assertEquals(48, subthemes)
            assertEquals(12, situations)
            assertEquals(212, tags)
        }
        assertNoIssue("DECLARED_COUNTS")
    }

    @Test
    fun devotionalIdsAreConsecutive() {
        val expected = (1..360).map { index -> "DEV-${index.toString().padStart(4, '0')}" }
        assertEquals(expected, content.devotionals.map { devotional -> devotional.id })
        assertNoIssue("DEVOTIONAL_SEQUENCE")
    }

    @Test
    fun devotionalIdsAreUnique() {
        val ids = content.devotionals.map { devotional -> devotional.id }
        assertEquals(ids.size, ids.toSet().size)
        assertNoIssue("DUPLICATE_DEVOTIONAL_IDS")
    }

    @Test
    fun categoryAndSubthemeRelationshipsAreValid() {
        val categories = content.categories.map { category -> category.id }.toSet()
        val subthemes = content.subthemes.associateBy { subtheme -> subtheme.id }
        content.devotionals.forEach { devotional ->
            assertTrue(devotional.categoryId in categories)
            assertEquals(devotional.categoryId, subthemes[devotional.subthemeId]?.categoryId)
        }
        assertNoIssue("UNKNOWN_CATEGORY")
        assertNoIssue("UNKNOWN_SUBTHEME")
        assertNoIssue("SUBTHEME_CATEGORY_MISMATCH")
    }

    @Test
    fun situationRelationshipsAreValid() {
        val situations = content.situations.map { situation -> situation.id }.toSet()
        assertTrue(content.devotionals.flatMap { it.situationIds }.all(situations::contains))
        assertNoIssue("UNKNOWN_SITUATION")
    }

    @Test
    fun devotionalTagsUseTheCanonicalTagKey() {
        val tags = content.tags.map { tag -> tag.tag }.toSet()
        assertEquals(212, tags.size)
        assertTrue(content.devotionals.flatMap { it.tags }.all(tags::contains))
        assertNoIssue("UNKNOWN_TAG")
    }

    @Test
    fun affinityGuideTagsExist() {
        val tags = content.tags.map { tag -> tag.tag }.toSet()
        assertTrue(content.affinities.flatMap { it.guideTags }.all(tags::contains))
        assertNoIssue("UNKNOWN_GUIDE_TAG")
    }

    @Test
    fun preferenceMappingsPointToExistingPreferences() {
        val preferences = content.preferences.map { preference -> preference.id }.toSet()
        assertTrue(content.preferenceMappings.all { mapping -> mapping.preferenceId in preferences })
        assertNoIssue("UNKNOWN_PREFERENCE")
    }

    @Test
    fun prayerRoutesPointToExistingGuides() {
        val guideIds = content.prayerGuides.map { guide -> guide.id }.toSet()
        assertTrue(content.prayerRoutes.flatMap { it.guideIds }.all(guideIds::contains))
        assertNoIssue("UNKNOWN_PRAYER_GUIDE")
    }

    @Test
    fun continuityPointsToExistingCategories() {
        val categories = content.categories.map { category -> category.id }.toSet()
        assertTrue(content.continuity.all { rule ->
            rule.sourceCategoryId in categories &&
                rule.primaryDestinationCategoryId in categories &&
                rule.alternateDestinationCategoryId in categories
        })
        assertNoIssue("UNKNOWN_CONTINUITY_CATEGORY")
    }

    @Test
    fun sourcePolicyMatchesTheRequiredVersionsAndPrivacyRules() {
        assertEquals("2.0", content.schemaVersion)
        assertEquals("1.1", content.sourcePolicy.outputContractVersion)
        assertFalse(content.sourcePolicy.literalBibleTextIncluded)
        assertFalse(content.sourcePolicy.userPersonalDataIncluded)
        assertFalse(content.sourcePolicy.autoRecommendReviewOnly)
        assertNoIssue("SCHEMA_VERSION")
        assertNoIssue("OUTPUT_CONTRACT_VERSION")
        assertNoIssue("LITERAL_BIBLE_TEXT_POLICY")
        assertNoIssue("USER_PERSONAL_DATA_POLICY")
        assertNoIssue("AUTO_RECOMMEND_REVIEW_ONLY_POLICY")
    }

    @Test
    fun literalBibleTextIsAbsent() {
        assertNoIssue("BIBLE_TEXT_PRESENT")
    }

    @Test
    fun noPlansOrPlanContentAreExported() {
        assertTrue(content.plans.isEmpty())
        assertTrue(content.planContent.isEmpty())
        assertEquals(0, result.statistics.plans)
        assertEquals(0, result.statistics.planContent)
        assertNoIssue("PLANS_NOT_EMPTY")
        assertNoIssue("PLAN_CONTENT_NOT_EMPTY")
    }

    @Test
    fun out11DefinesResultType() {
        assertNotNull(content.outputContract.firstOrNull { it.id == "OUT-11" && it.field == "resultType" })
        assertNoIssue("OUT_11")
    }

    @Test
    fun fallbackFb08Exists() {
        assertTrue(content.fallbackRules.any { rule -> rule.id == "FB-08" })
        assertNoIssue("FB_08")
    }

    @Test
    fun exactly34DevotionalsRequireReview() {
        assertEquals(34, content.devotionals.count { it.autoRecommendation == "Solo con revisión" })
        assertEquals(34, result.statistics.reviewOnlyDevotionals)
        assertNoIssue("REVIEW_ONLY_COUNT")
    }

    private fun assertNoIssue(code: String) {
        assertTrue(
            "Unexpected validation issue $code: ${result.issues.filter { it.code == code }}",
            result.issues.none { issue -> issue.code == code },
        )
    }
}
