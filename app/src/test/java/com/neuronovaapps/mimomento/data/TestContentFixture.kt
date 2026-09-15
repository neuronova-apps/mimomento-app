package com.neuronovaapps.mimomento.data

import com.neuronovaapps.mimomento.data.local.LoadedMiMomentoContent
import com.neuronovaapps.mimomento.data.local.MiMomentoContentLoader
import com.neuronovaapps.mimomento.data.local.MiMomentoContentParser
import com.neuronovaapps.mimomento.data.validation.MiMomentoContentValidator
import com.neuronovaapps.mimomento.data.validation.ValidationResult
import java.io.File

internal object TestContentFixture {
    val assetFile: File by lazy {
        listOf(
            File("src/main/assets/${MiMomentoContentLoader.ASSET_NAME}"),
            File("app/src/main/assets/${MiMomentoContentLoader.ASSET_NAME}"),
        ).firstOrNull(File::isFile)
            ?: error("Required test asset '${MiMomentoContentLoader.ASSET_NAME}' was not found.")
    }

    val rawJson: String by lazy { assetFile.readText(Charsets.UTF_8) }

    val loaded: LoadedMiMomentoContent by lazy {
        MiMomentoContentParser().parse(rawJson)
    }

    val validation: ValidationResult by lazy {
        MiMomentoContentValidator().validate(loaded.content, loaded.rawJson)
    }
}
