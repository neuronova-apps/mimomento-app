package com.neuronova.mimomento.data.local

import android.content.Context
import java.io.IOException
import java.io.InputStream

fun interface MiMomentoContentSource {
    fun load(): LoadedMiMomentoContent
}

class MiMomentoContentLoadException(
    message: String,
    cause: Throwable? = null,
) : IllegalStateException(message, cause)

class MiMomentoContentLoader(
    private val openAsset: (String) -> InputStream,
    private val parser: MiMomentoContentParser = MiMomentoContentParser(),
) : MiMomentoContentSource {
    constructor(
        context: Context,
        parser: MiMomentoContentParser = MiMomentoContentParser(),
    ) : this(
        openAsset = { assetName -> context.applicationContext.assets.open(assetName) },
        parser = parser,
    )

    override fun load(): LoadedMiMomentoContent {
        val rawJson = try {
            openAsset(ASSET_NAME)
                .bufferedReader(Charsets.UTF_8)
                .use { reader -> reader.readText() }
        } catch (error: IOException) {
            throw MiMomentoContentLoadException(
                "Unable to read '$ASSET_NAME' from Android assets.",
                error,
            )
        }

        return parser.parse(rawJson)
    }

    companion object {
        const val ASSET_NAME = "mi_momento_content_v2.json"
    }
}
