package com.neuronova.mimomento.ui.app

import com.neuronova.mimomento.data.TestContentFixture
import com.neuronova.mimomento.data.local.LoadedMiMomentoContent
import com.neuronova.mimomento.data.local.MiMomentoContentSource
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppContentViewModelTest {

    @Test
    fun initializesWithReadyStateWhenRepositoryIsValid() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val viewModel = AppContentViewModel(
            repository = repository,
            validator = MiMomentoContentValidator(),
            dispatcher = Dispatchers.Unconfined,
        )

        val state = viewModel.uiState.value
        assertTrue("Expected Ready state, but got $state", state is AppContentUiState.Ready)
        val ready = state as AppContentUiState.Ready
        assertEquals(360, ready.devotionalCount)
        assertEquals(12, ready.categoryCount)
    }

    @Test
    fun transitionsToErrorStateWhenValidationFails() {
        val invalidContent = TestContentFixture.loaded.content.copy(
            schemaVersion = "1.0", // Invalid schema version triggers validation failure
        )
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource {
                LoadedMiMomentoContent(
                    content = invalidContent,
                    rawJson = TestContentFixture.loaded.rawJson,
                )
            },
        )

        val viewModel = AppContentViewModel(
            repository = repository,
            validator = MiMomentoContentValidator(),
            dispatcher = Dispatchers.Unconfined,
        )

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but got $state", state is AppContentUiState.Error)
    }

    @Test
    fun transitionsToErrorStateWhenRepositoryFails() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource {
                throw IllegalStateException("Simulated IO failure loading content")
            },
        )

        val viewModel = AppContentViewModel(
            repository = repository,
            validator = MiMomentoContentValidator(),
            dispatcher = Dispatchers.Unconfined,
        )

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but got $state", state is AppContentUiState.Error)
    }

    @Test
    fun retryRecoversFromErrorWhenRepositoryBecomesAvailable() {
        val shouldFail = AtomicBoolean(true)
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource {
                if (shouldFail.get()) {
                    throw IllegalStateException("Temporary error")
                } else {
                    TestContentFixture.loaded
                }
            },
        )

        val viewModel = AppContentViewModel(
            repository = repository,
            validator = MiMomentoContentValidator(),
            dispatcher = Dispatchers.Unconfined,
        )

        assertTrue(viewModel.uiState.value is AppContentUiState.Error)

        shouldFail.set(false)
        viewModel.retry()

        val state = viewModel.uiState.value
        assertTrue("Expected Ready state after retry, but got $state", state is AppContentUiState.Ready)
        assertEquals(360, (state as AppContentUiState.Ready).devotionalCount)
    }
}
