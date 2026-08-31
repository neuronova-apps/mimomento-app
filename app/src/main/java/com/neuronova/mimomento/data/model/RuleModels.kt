package com.neuronova.mimomento.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RecommendationRule(
    val id: String,
    val priority: String,
    val rule: String,
    val source: String,
    val effect: String,
    val weightOrder: Int,
    val exception: String,
    val status: String,
    val notes: String,
)

@Serializable
data class SafetyRule(
    val id: String,
    val scope: String,
    val rule: String,
    val riskAvoided: String,
    val actionIfViolated: String,
    val affectsRecommendation: Boolean,
    val level: String,
    val status: String,
    val notes: String,
)

@Serializable
data class Affinity(
    val id: String,
    val contextSource: String,
    val contextId: String,
    val primaryCategoryId: String,
    val secondaryCategoryId: String,
    val guideTags: List<String>,
    val primaryWeight: Int,
    val secondaryWeight: Int,
    val status: String,
    val notes: String,
)

@Serializable
data class ContinuityRule(
    val id: String,
    val sourceCategoryId: String,
    val primaryDestinationCategoryId: String,
    val alternateDestinationCategoryId: String,
    val editorialLogic: String,
    val weight: Int,
    val safetyCondition: String,
    val varietyRule: String,
    val status: String,
    val notes: String,
)

@Serializable
data class OutputContractField(
    val id: String,
    val field: String,
    val type: String,
    val required: Boolean,
    val source: String,
    val rule: String,
    val visibleUi: String,
    val persistence: String,
    val sensitivity: String,
    val notes: String,
)

@Serializable
data class FallbackRule(
    val id: String,
    val level: String,
    val condition: String,
    val action: String,
    val relaxes: String,
    val neverRelaxes: String,
    val result: String,
    val status: String,
    val uiExplanation: String,
    val notes: String,
)
