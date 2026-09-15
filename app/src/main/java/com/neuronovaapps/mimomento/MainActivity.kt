package com.neuronovaapps.mimomento

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.preferences.preferencesDataStore
import com.neuronovaapps.mimomento.data.local.FileJournalStorage
import com.neuronovaapps.mimomento.data.local.FileProgressStorage
import com.neuronovaapps.mimomento.data.local.MiMomentoContentLoader
import com.neuronovaapps.mimomento.data.repository.AccessibilityPreferencesRepository
import com.neuronovaapps.mimomento.data.repository.LocalJournalRepository
import com.neuronovaapps.mimomento.data.repository.LocalProgressRepository
import com.neuronovaapps.mimomento.data.repository.MiMomentoContentRepository
import com.neuronovaapps.mimomento.data.repository.ThemePreferencesRepository
import com.neuronovaapps.mimomento.ui.app.MiMomentoApp
import java.io.File

private val Context.themeDataStore by preferencesDataStore(name = "theme_preferences")
private val Context.accessibilityDataStore by preferencesDataStore(name = "accessibility_preferences")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentRepository = MiMomentoContentRepository(MiMomentoContentLoader(applicationContext))
        val themeRepository = ThemePreferencesRepository(applicationContext.themeDataStore)
        val accessibilityRepository = AccessibilityPreferencesRepository(applicationContext.accessibilityDataStore)
        val journalStorage = FileJournalStorage(File(applicationContext.filesDir, "journal_entries.json"))
        val journalRepository = LocalJournalRepository(journalStorage)
        val progressStorage = FileProgressStorage(File(applicationContext.filesDir, "progress_events.json"))
        val progressRepository = LocalProgressRepository(
            storage = progressStorage,
            journalRepository = journalRepository,
        )

        setContent {
            MiMomentoApp(
                repository = contentRepository,
                themeRepository = themeRepository,
                accessibilityRepository = accessibilityRepository,
                journalRepository = journalRepository,
                progressRepository = progressRepository,
            )
        }
    }
}
