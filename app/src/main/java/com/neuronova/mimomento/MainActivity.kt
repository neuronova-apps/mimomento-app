package com.neuronova.mimomento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.neuronova.mimomento.data.local.MiMomentoContentLoader
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = MiMomentoContentRepository(MiMomentoContentLoader(applicationContext))
        setContent {
            MiMomentoApp(repository)
        }
    }
}

@Composable
private fun MiMomentoApp(repository: MiMomentoContentRepository) {
    val state by produceState<ContentUiState>(
        initialValue = ContentUiState.Loading,
        key1 = repository,
    ) {
        value = withContext(Dispatchers.IO) {
            try {
                val validation = repository.validate(MiMomentoContentValidator())
                if (validation.isValid) {
                    ContentUiState.Ready(validation.statistics.devotionals)
                } else {
                    ContentUiState.Error
                }
            } catch (_: RuntimeException) {
                ContentUiState.Error
            }
        }
    }

    MiMomentoScreen(state)
}

@Composable
private fun MiMomentoScreen(state: ContentUiState) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = when (state) {
                        ContentUiState.Loading -> stringResource(R.string.content_loading)
                        ContentUiState.Error -> stringResource(R.string.content_error)
                        is ContentUiState.Ready -> stringResource(
                            R.string.content_available,
                            state.devotionalCount,
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MiMomentoAppPreview() {
    MiMomentoScreen(ContentUiState.Loading)
}

private sealed interface ContentUiState {
    data object Loading : ContentUiState
    data object Error : ContentUiState
    data class Ready(val devotionalCount: Int) : ContentUiState
}
