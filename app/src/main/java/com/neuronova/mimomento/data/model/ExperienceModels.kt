package com.neuronova.mimomento.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentCollection(
    val id: String,
    val name: String,
    val editorialGoal: String,
    val situationIds: List<String>,
    val categoryIds: List<String>,
    val tags: List<String>,
    val timeOfDay: String,
    val selectionCriteria: String,
    val publishable: Boolean,
    val status: String,
)

@Serializable
data class SpiritualMoment(
    val id: String,
    val label: String,
    val description: String,
    val situationIds: List<String>,
    val categoryIds: List<String>,
    val timeOfDay: String,
    val suggestedInteraction: String,
    val contextPriority: String,
    val status: String,
)

@Serializable
data class PrayerRoute(
    val id: String,
    val name: String,
    val goal: String,
    val guideIds: List<String>,
    val situationIds: List<String>,
    val categoryIds: List<String>,
    val estimatedDuration: String,
    val storesPersonalContent: Boolean,
    val publishable: Boolean,
    val status: String,
)

@Serializable
data class PrayerGuide(
    val id: String,
    val name: String,
    val purpose: String,
    val guidance: String,
    val situationIds: List<String>,
    val categoryIds: List<String>,
    val mode: String,
    val sensitivity: String,
    val publishable: Boolean,
    val status: String,
)

@Serializable
data class SpecialContext(
    val id: String,
    val label: String,
    val purpose: String,
    val situationIds: List<String>,
    val categoryIds: List<String>,
    val allowedContent: String,
    val cautions: String,
    val sensitivity: String,
    val publishable: Boolean,
    val status: String,
)

@Serializable
data class Devotional(
    val id: String,
    val title: String,
    val categoryId: String,
    val subthemeId: String,
    val situationIds: List<String>,
    val bibleReference: String,
    val bibleSourceId: String,
    val centralIdea: String,
    val reflection: String,
    val personalQuestion: String,
    val prayerGuide: String,
    val dailyAction: String,
    val timeOfDay: String,
    val estimatedMinutes: Int,
    val usageType: String,
    val recommendationPriority: String,
    val tags: List<String>,
    val audience: String,
    val depth: String,
    val reflectionWordCount: Int,
    val sensitivity: String,
    val autoRecommendation: String,
    val lastReviewed: String,
    val contentVersion: String,
)
