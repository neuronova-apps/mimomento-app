package com.neuronova.mimomento.data.local

import com.neuronova.mimomento.data.TestContentFixture
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class MiMomentoContentParserTest {
    private val parser = MiMomentoContentParser()

    @Test
    fun parsesTheAuditedAsset() {
        val content = parser.parse(TestContentFixture.rawJson).content

        assertEquals("2.0", content.schemaVersion)
        assertEquals("1.1", content.sourcePolicy.outputContractVersion)
        assertEquals(360, content.devotionals.size)
    }

    @Test
    fun toleratesUnknownFutureFields() {
        val withFutureField = JsonObject(
            TestContentFixture.loaded.rawJson.jsonObject + ("futureField" to JsonPrimitive(true)),
        )

        assertEquals(360, parser.parse(withFutureField.toString()).content.devotionals.size)
    }

    @Test
    fun rejectsMissingRequiredFields() {
        val withoutSchemaVersion = JsonObject(
            TestContentFixture.loaded.rawJson.jsonObject - "schemaVersion",
        )

        assertThrows(MiMomentoContentParseException::class.java) {
            parser.parse(withoutSchemaVersion.toString())
        }
    }

    @Test
    fun rejectsIncorrectRequiredTypes() {
        val wrongSchemaType = JsonObject(
            TestContentFixture.loaded.rawJson.jsonObject + ("schemaVersion" to JsonPrimitive(2.0)),
        )

        assertThrows(MiMomentoContentParseException::class.java) {
            parser.parse(wrongSchemaType.toString())
        }
    }

    @Test
    fun loaderReadsAndParsesTheAsset() {
        val loader = MiMomentoContentLoader(
            openAsset = { TestContentFixture.assetFile.inputStream() },
        )

        assertEquals(360, loader.load().content.devotionals.size)
    }

    @Test
    fun loaderReportsMissingAssetsClearly() {
        val loader = MiMomentoContentLoader(
            openAsset = { throw FileNotFoundException("missing test asset") },
        )

        assertThrows(MiMomentoContentLoadException::class.java) { loader.load() }
    }

    @Test
    fun loaderReportsInvalidJsonClearly() {
        val loader = MiMomentoContentLoader(
            openAsset = { ByteArrayInputStream("{invalid".toByteArray()) },
        )

        assertThrows(MiMomentoContentParseException::class.java) { loader.load() }
    }
}
