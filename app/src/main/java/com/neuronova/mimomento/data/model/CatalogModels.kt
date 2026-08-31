package com.neuronova.mimomento.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val definition: String,
    val includedTopics: List<String>,
    val initialPriority: String,
    val status: String,
    val targetCount: Int,
    val producedCount: Int,
    val remainingCount: Int,
)

@Serializable
data class Subtheme(
    val id: String,
    val categoryId: String,
    val categoryName: String,
    val name: String,
    val editorialScope: String,
    val targetCount: Int,
    val producedCount: Int,
    val remainingCount: Int,
    val suggestedTags: List<String>,
)

@Serializable
data class Situation(
    val id: String,
    val label: String,
    val relatedCategories: List<String>,
    val searchTags: List<String>,
    val priority: String,
    val suggestedUiText: String,
    val status: String,
    val targetCoverage: Int,
    val currentCoverage: Int,
    val remaining: Int,
)

@Serializable
data class TagDefinition(
    val tag: String,
    val family: String,
    val recommendedUse: String,
    val avoidConfusingWith: String,
    val status: String,
)

@Serializable
data class Preference(
    val id: String,
    val dimension: String,
    val label: String,
    val value: String,
    val selectionType: String,
    val required: Boolean,
    val order: Int,
    val affects: String,
    val status: String,
    val privacyNote: String,
)

@Serializable
data class PreferenceMapping(
    val id: String,
    val preferenceId: String,
    val target: String,
    val targetField: String,
    val value: String,
    val weight: Int,
    val effectType: String,
    val status: String,
    val notes: String,
)

@Serializable
data class PlanType(
    val id: String,
    val name: String,
    val definition: String,
    val typicalDurations: String,
    val recommendedUse: String,
    val allowsSensitiveContent: String,
    val status: String,
    val notes: String,
)
