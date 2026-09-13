package com.neuronova.mimomento

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.preferences.preferencesDataStore
import com.neuronova.mimomento.data.local.FileJournalStorage
import com.neuronova.mimomento.data.local.MiMomentoContentLoader
import com.neuronova.mimomento.data.repository.LocalJournalRepository
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.ui.app.MiMomentoApp
import java.io.File

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentRepository = MiMomentoContentRepository(MiMomentoContentLoader(applicationContext))
        val themeRepository = ThemePreferencesRepository(applicationContext.themeDataStore)
        val journalStorage = FileJournalStorage(File(applicationContext.filesDir, "journal_entries.json"))
        val journalRepository = LocalJournalRepository(journalStorage)

        setContent {
            MiMomentoApp(
                repository = contentRepository,
                themeRepository = themeRepository,
                journalRepository = journalRepository,
            )
        }
    }
}
