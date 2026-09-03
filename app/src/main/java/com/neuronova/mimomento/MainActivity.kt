package com.neuronova.mimomento

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.preferences.preferencesDataStore
import com.neuronova.mimomento.data.local.MiMomentoContentLoader
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.ui.app.MiMomentoApp

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentRepository = MiMomentoContentRepository(MiMomentoContentLoader(applicationContext))
        val themeRepository = ThemePreferencesRepository(applicationContext.themeDataStore)

        setContent {
            MiMomentoApp(
                repository = contentRepository,
                themeRepository = themeRepository,
            )
        }
    }
}
