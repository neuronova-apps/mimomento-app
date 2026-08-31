package com.neuronova.mimomento.data.local

import com.neuronova.mimomento.data.model.MiMomentoContent
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

data class LoadedMiMomentoContent(
    val content: MiMomentoContent,
    val rawJson: JsonElement,
)

class MiMomentoContentParseException(
    message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)

class MiMomentoContentParser(
    private val json: Json = DEFAULT_JSON,
) {
    fun parse(rawJson: String): LoadedMiMomentoContent {
        if (rawJson.isBlank()) {
            throw MiMomentoContentParseException("The Mi Momento content asset is empty.")
        }

        return try {
            val jsonElement = json.parseToJsonElement(rawJson)
            if (jsonElement !is JsonObject) {
                throw MiMomentoContentParseException(
                    "The Mi Momento content asset must contain a JSON object at its root.",
                )
            }
            LoadedMiMomentoContent(
                content = json.decodeFromJsonElement(jsonElement),
                rawJson = jsonElement,
            )
        } catch (error: MiMomentoContentParseException) {
            throw error
        } catch (error: SerializationException) {
            throw MiMomentoContentParseException(
                "The Mi Momento content asset does not match schema version 2.0: ${error.message}",
                error,
            )
        } catch (error: IllegalArgumentException) {
            throw MiMomentoContentParseException(
                "The Mi Momento content asset is not valid JSON: ${error.message}",
                error,
            )
        }
    }

    companion object {
        val DEFAULT_JSON = Json {
            ignoreUnknownKeys = true
            isLenient = false
            coerceInputValues = false
            explicitNulls = true
        }
    }
}
