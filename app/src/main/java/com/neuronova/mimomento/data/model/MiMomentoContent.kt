package com.neuronova.mimomento.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MiMomentoContent(
    val schemaVersion: String,
    val contentBank: String,
    val generatedOn: String,
    val targetPlatform: String,
    val encoding: String,
    val sourcePolicy: SourcePolicy,
    val counts: ContentCounts,
    val categories: List<Category>,
    val subthemes: List<Subtheme>,
    val situations: List<Situation>,
    val tags: List<TagDefinition>,
    val preferences: List<Preference>,
    val preferenceMappings: List<PreferenceMapping>,
    val planTypes: List<PlanType>,
    val recommendationRules: List<RecommendationRule>,
    val safetyRules: List<SafetyRule>,
    val collections: List<ContentCollection>,
    val spiritualMoments: List<SpiritualMoment>,
    val prayerRoutes: List<PrayerRoute>,
    val prayerGuides: List<PrayerGuide>,
    val specialContexts: List<SpecialContext>,
    val affinities: List<Affinity>,
    val continuity: List<ContinuityRule>,
    val outputContract: List<OutputContractField>,
    val fallbackRules: List<FallbackRule>,
    val plans: List<JsonObject>,
    val planContent: List<JsonObject>,
    val devotionals: List<Devotional>,
)

@Serializable
data class SourcePolicy(
    val approvedDevotionalsOnly: Boolean,
    val literalBibleTextIncluded: Boolean,
    val publishablePlansOnly: Boolean,
    val includeNonPublishablePlans: Boolean,
    val includePlanContentOnlyWhenPlanPublishable: Boolean,
    val includeReviewOnlyDevotionals: Boolean,
    val autoRecommendReviewOnly: Boolean,
    val userPersonalDataIncluded: Boolean,
    val outputContractVersion: String,
    val idsStable: Boolean,
    val unknownFieldsPolicy: String,
)

@Serializable
data class ContentCounts(
    val categories: Int,
    val subthemes: Int,
    val situations: Int,
    val tags: Int,
    val preferences: Int,
    val preferenceMappings: Int,
    val planTypes: Int,
    val recommendationRules: Int,
    val safetyRules: Int,
    val collections: Int,
    val spiritualMoments: Int,
    val prayerRoutes: Int,
    val prayerGuides: Int,
    val specialContexts: Int,
    val affinities: Int,
    val continuity: Int,
    val outputContract: Int,
    val fallbackRules: Int,
    val plans: Int,
    val planContent: Int,
    val devotionals: Int,
)
