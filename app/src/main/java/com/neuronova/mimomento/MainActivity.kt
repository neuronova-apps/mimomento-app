package com.neuronova.mimomento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.neuronova.mimomento.data.local.MiMomentoContentLoader
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.ui.app.MiMomentoApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = MiMomentoContentRepository(MiMomentoContentLoader(applicationContext))
        setContent {
            MiMomentoApp(repository = repository)
        }
    }
}
