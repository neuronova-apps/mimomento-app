package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.local.LoadedMiMomentoContent
import com.neuronova.mimomento.data.local.MiMomentoContentSource
import com.neuronova.mimomento.data.model.Category
import com.neuronova.mimomento.data.model.Devotional
import com.neuronova.mimomento.data.model.Situation
import com.neuronova.mimomento.data.model.Subtheme
import com.neuronova.mimomento.data.model.TagDefinition
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import com.neuronova.mimomento.data.validation.ValidationResult

class MiMomentoContentRepository(
    private val source: MiMomentoContentSource,
) {
    @Volatile
    private var cachedContent: LoadedMiMomentoContent? = null

    fun getAllDevotionals(): List<Devotional> = loadOnce().content.devotionals

    fun getDevotionalById(id: String): Devotional? =
        loadOnce().content.devotionals.firstOrNull { devotional -> devotional.id == id }

    fun getPreviousDevotionalId(id: String): String? {
        val devotionals = getAllDevotionals()
        val index = devotionals.indexOfFirst { it.id == id }
        return if (index > 0) devotionals[index - 1].id else null
    }

    fun getNextDevotionalId(id: String): String? {
        val devotionals = getAllDevotionals()
        val index = devotionals.indexOfFirst { it.id == id }
        return if (index in 0 until devotionals.size - 1) devotionals[index + 1].id else null
    }

    fun getCategories(): List<Category> = loadOnce().content.categories

    fun getSituations(): List<Situation> = loadOnce().content.situations

    fun getSubthemes(): List<Subtheme> = loadOnce().content.subthemes

    fun getTags(): List<TagDefinition> = loadOnce().content.tags

    fun validate(validator: MiMomentoContentValidator): ValidationResult {
        val loaded = loadOnce()
        return validator.validate(loaded.content, loaded.rawJson)
    }

    private fun loadOnce(): LoadedMiMomentoContent {
        cachedContent?.let { return it }
        return synchronized(this) {
            cachedContent ?: source.load().also { loaded -> cachedContent = loaded }
        }
    }
}
