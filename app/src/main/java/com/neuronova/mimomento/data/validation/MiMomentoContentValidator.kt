package com.neuronova.mimomento.data.validation

import com.neuronova.mimomento.data.model.MiMomentoContent
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class ValidationIssue(
    val code: String,
    val message: String,
)

data class ContentStatistics(
    val devotionals: Int,
    val categories: Int,
    val subthemes: Int,
    val situations: Int,
    val tags: Int,
    val reviewOnlyDevotionals: Int,
    val plans: Int,
    val planContent: Int,
    val brokenRelationships: Int,
)

data class ValidationResult(
    val issues: List<ValidationIssue>,
    val statistics: ContentStatistics,
) {
    val isValid: Boolean = issues.isEmpty()
}

class MiMomentoContentValidator {
    fun validate(
        content: MiMomentoContent,
        rawJson: JsonElement,
    ): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        var brokenRelationships = 0

        fun requireRule(condition: Boolean, code: String, message: String) {
            if (!condition) issues += ValidationIssue(code, message)
        }

        fun requireReferences(code: String, description: String, broken: List<String>) {
            brokenRelationships += broken.size
            if (broken.isNotEmpty()) {
                issues += ValidationIssue(
                    code = code,
                    message = "$description (${broken.size}): ${broken.take(10).joinToString()}",
                )
            }
        }

        requireRule(
            content.schemaVersion == EXPECTED_SCHEMA_VERSION,
            "SCHEMA_VERSION",
            "schemaVersion must be $EXPECTED_SCHEMA_VERSION but was '${content.schemaVersion}'.",
        )
        requireRule(
            content.sourcePolicy.outputContractVersion == EXPECTED_OUTPUT_CONTRACT_VERSION,
            "OUTPUT_CONTRACT_VERSION",
            "sourcePolicy.outputContractVersion must be $EXPECTED_OUTPUT_CONTRACT_VERSION but was " +
                "'${content.sourcePolicy.outputContractVersion}'.",
        )
        requireRule(
            content.devotionals.size == EXPECTED_DEVOTIONALS,
            "DEVOTIONAL_COUNT",
            "Expected $EXPECTED_DEVOTIONALS devotionals but found ${content.devotionals.size}.",
        )

        val expectedDevotionalIds = (1..EXPECTED_DEVOTIONALS).map { index ->
            "DEV-${index.toString().padStart(4, '0')}"
        }
        val actualDevotionalIds = content.devotionals.map { devotional -> devotional.id }
        requireRule(
            actualDevotionalIds == expectedDevotionalIds,
            "DEVOTIONAL_SEQUENCE",
            "Devotional IDs must be ordered consecutively from DEV-0001 through DEV-0360.",
        )

        requireRule(
            content.categories.size == EXPECTED_CATEGORIES,
            "CATEGORY_COUNT",
            "Expected $EXPECTED_CATEGORIES categories but found ${content.categories.size}.",
        )
        requireRule(
            content.subthemes.size == EXPECTED_SUBTHEMES,
            "SUBTHEME_COUNT",
            "Expected $EXPECTED_SUBTHEMES subthemes but found ${content.subthemes.size}.",
        )
        requireRule(
            content.situations.size == EXPECTED_SITUATIONS,
            "SITUATION_COUNT",
            "Expected $EXPECTED_SITUATIONS situations but found ${content.situations.size}.",
        )
        requireRule(
            content.tags.size == EXPECTED_TAGS,
            "TAG_COUNT",
            "Expected $EXPECTED_TAGS tags but found ${content.tags.size}.",
        )

        val duplicateDevotionalIds = actualDevotionalIds
            .groupingBy { id -> id }
            .eachCount()
            .filterValues { occurrences -> occurrences > 1 }
        requireRule(
            duplicateDevotionalIds.isEmpty(),
            "DUPLICATE_DEVOTIONAL_IDS",
            "Duplicate devotional IDs found: ${duplicateDevotionalIds.keys.joinToString()}.",
        )

        val categoryIds = content.categories.mapTo(mutableSetOf()) { category -> category.id }
        val subthemesById = content.subthemes.associateBy { subtheme -> subtheme.id }
        val situationIds = content.situations.mapTo(mutableSetOf()) { situation -> situation.id }
        val canonicalTags = content.tags.mapTo(mutableSetOf()) { tag -> tag.tag }
        val preferenceIds = content.preferences.mapTo(mutableSetOf()) { preference -> preference.id }
        val prayerGuideIds = content.prayerGuides.mapTo(mutableSetOf()) { guide -> guide.id }

        requireReferences(
            code = "UNKNOWN_CATEGORY",
            description = "Devotionals reference unknown categories",
            broken = content.devotionals.mapNotNull { devotional ->
                devotional.categoryId.takeUnless(categoryIds::contains)?.let { id -> "${devotional.id}:$id" }
            },
        )
        requireReferences(
            code = "UNKNOWN_SUBTHEME",
            description = "Devotionals reference unknown subthemes",
            broken = content.devotionals.mapNotNull { devotional ->
                devotional.subthemeId.takeUnless(subthemesById::containsKey)?.let { id -> "${devotional.id}:$id" }
            },
        )
        requireReferences(
            code = "SUBTHEME_CATEGORY_MISMATCH",
            description = "Devotional subthemes belong to a different category",
            broken = content.devotionals.mapNotNull { devotional ->
                subthemesById[devotional.subthemeId]
                    ?.takeUnless { subtheme -> subtheme.categoryId == devotional.categoryId }
                    ?.let { subtheme ->
                        "${devotional.id}:${devotional.categoryId}/${subtheme.id}:${subtheme.categoryId}"
                    }
            },
        )
        requireReferences(
            code = "UNKNOWN_SITUATION",
            description = "Devotionals reference unknown situations",
            broken = content.devotionals.flatMap { devotional ->
                devotional.situationIds.filterNot(situationIds::contains).map { id -> "${devotional.id}:$id" }
            },
        )
        requireReferences(
            code = "UNKNOWN_TAG",
            description = "Devotionals reference tags outside the canonical catalog",
            broken = content.devotionals.flatMap { devotional ->
                devotional.tags.filterNot(canonicalTags::contains).map { tag -> "${devotional.id}:$tag" }
            },
        )
        requireReferences(
            code = "UNKNOWN_GUIDE_TAG",
            description = "Affinities reference guideTags outside the canonical catalog",
            broken = content.affinities.flatMap { affinity ->
                affinity.guideTags.filterNot(canonicalTags::contains).map { tag -> "${affinity.id}:$tag" }
            },
        )
        requireReferences(
            code = "UNKNOWN_PREFERENCE",
            description = "Preference mappings reference unknown preferences",
            broken = content.preferenceMappings.mapNotNull { mapping ->
                mapping.preferenceId.takeUnless(preferenceIds::contains)?.let { id -> "${mapping.id}:$id" }
            },
        )
        requireReferences(
            code = "UNKNOWN_PRAYER_GUIDE",
            description = "Prayer routes reference unknown prayer guides",
            broken = content.prayerRoutes.flatMap { route ->
                route.guideIds.filterNot(prayerGuideIds::contains).map { id -> "${route.id}:$id" }
            },
        )
        requireReferences(
            code = "UNKNOWN_CONTINUITY_CATEGORY",
            description = "Continuity rules reference unknown categories",
            broken = content.continuity.flatMap { rule ->
                listOf(
                    "source" to rule.sourceCategoryId,
                    "primary" to rule.primaryDestinationCategoryId,
                    "alternate" to rule.alternateDestinationCategoryId,
                ).filterNot { (_, id) -> id in categoryIds }
                    .map { (role, id) -> "${rule.id}:$role:$id" }
            },
        )

        requireRule(
            content.plans.isEmpty(),
            "PLANS_NOT_EMPTY",
            "plans must be empty but contained ${content.plans.size} entries.",
        )
        requireRule(
            content.planContent.isEmpty(),
            "PLAN_CONTENT_NOT_EMPTY",
            "planContent must be empty but contained ${content.planContent.size} entries.",
        )

        val bibleTextOccurrences = countExactBibleTextOccurrences(rawJson)
        requireRule(
            bibleTextOccurrences == 0,
            "BIBLE_TEXT_PRESENT",
            "The raw JSON contains $bibleTextOccurrences exact bibleText field or value occurrences.",
        )
        requireRule(
            !content.sourcePolicy.literalBibleTextIncluded,
            "LITERAL_BIBLE_TEXT_POLICY",
            "sourcePolicy.literalBibleTextIncluded must be false.",
        )
        requireRule(
            !content.sourcePolicy.userPersonalDataIncluded,
            "USER_PERSONAL_DATA_POLICY",
            "sourcePolicy.userPersonalDataIncluded must be false.",
        )
        requireRule(
            !content.sourcePolicy.autoRecommendReviewOnly,
            "AUTO_RECOMMEND_REVIEW_ONLY_POLICY",
            "sourcePolicy.autoRecommendReviewOnly must be false.",
        )

        requireRule(
            content.outputContract.any { field -> field.id == "OUT-11" && field.field == "resultType" },
            "OUT_11",
            "OUT-11 with field=resultType is required.",
        )
        requireRule(
            content.fallbackRules.any { rule -> rule.id == "FB-08" },
            "FB_08",
            "FB-08 is required.",
        )

        val reviewOnlyCount = content.devotionals.count { devotional ->
            devotional.autoRecommendation == REVIEW_ONLY_VALUE
        }
        requireRule(
            reviewOnlyCount == EXPECTED_REVIEW_ONLY,
            "REVIEW_ONLY_COUNT",
            "Expected $EXPECTED_REVIEW_ONLY review-only devotionals but found $reviewOnlyCount.",
        )

        requireRule(
            declaredCountsMatch(content),
            "DECLARED_COUNTS",
            "The counts object does not match one or more top-level collection sizes.",
        )

        return ValidationResult(
            issues = issues.toList(),
            statistics = ContentStatistics(
                devotionals = content.devotionals.size,
                categories = content.categories.size,
                subthemes = content.subthemes.size,
                situations = content.situations.size,
                tags = content.tags.size,
                reviewOnlyDevotionals = reviewOnlyCount,
                plans = content.plans.size,
                planContent = content.planContent.size,
                brokenRelationships = brokenRelationships,
            ),
        )
    }

    private fun declaredCountsMatch(content: MiMomentoContent): Boolean =
        with(content) {
            counts.categories == categories.size &&
                counts.subthemes == subthemes.size &&
                counts.situations == situations.size &&
                counts.tags == tags.size &&
                counts.preferences == preferences.size &&
                counts.preferenceMappings == preferenceMappings.size &&
                counts.planTypes == planTypes.size &&
                counts.recommendationRules == recommendationRules.size &&
                counts.safetyRules == safetyRules.size &&
                counts.collections == collections.size &&
                counts.spiritualMoments == spiritualMoments.size &&
                counts.prayerRoutes == prayerRoutes.size &&
                counts.prayerGuides == prayerGuides.size &&
                counts.specialContexts == specialContexts.size &&
                counts.affinities == affinities.size &&
                counts.continuity == continuity.size &&
                counts.outputContract == outputContract.size &&
                counts.fallbackRules == fallbackRules.size &&
                counts.plans == plans.size &&
                counts.planContent == planContent.size &&
                counts.devotionals == devotionals.size
        }

    private fun countExactBibleTextOccurrences(element: JsonElement): Int = when (element) {
        is JsonObject -> element.entries.sumOf { (key, value) ->
            (if (key == "bibleText") 1 else 0) + countExactBibleTextOccurrences(value)
        }
        is JsonArray -> element.sumOf(::countExactBibleTextOccurrences)
        is JsonPrimitive -> if (element.isString && element.content == "bibleText") 1 else 0
    }

    companion object {
        private const val EXPECTED_SCHEMA_VERSION = "2.0"
        private const val EXPECTED_OUTPUT_CONTRACT_VERSION = "1.1"
        private const val EXPECTED_DEVOTIONALS = 360
        private const val EXPECTED_CATEGORIES = 12
        private const val EXPECTED_SUBTHEMES = 48
        private const val EXPECTED_SITUATIONS = 12
        private const val EXPECTED_TAGS = 212
        private const val EXPECTED_REVIEW_ONLY = 34
        private const val REVIEW_ONLY_VALUE = "Solo con revisión"
    }
}
