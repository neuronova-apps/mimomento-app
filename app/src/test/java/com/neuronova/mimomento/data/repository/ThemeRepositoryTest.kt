package com.neuronova.mimomento.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.random.Random

class ThemeRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createRepository(
        policy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
    ): ThemePreferencesRepository {
        val testFile = tempFolder.newFile("test_theme_prefs_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        return ThemePreferencesRepository(dataStore, policy)
    }

    // --- PREFERENCIAS ---

    @Test
    fun preferences_defaultSelectedThemeIsSky() = runBlocking {
        val repository = createRepository()
        val state = repository.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.SKY, state.selectedThemeId)
    }

    @Test
    fun preferences_validSelectedThemePersistsAndRecovers() = runBlocking {
        val repository = createRepository()
        repository.setSelectedTheme(MiMomentoThemeId.DAWN)
        val state = repository.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.DAWN, state.selectedThemeId)
    }

    @Test
    fun preferences_corruptSelectedThemeFallsBackToSky() = runBlocking {
        val testFile = tempFolder.newFile("corrupt_test_${System.nanoTime()}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create { testFile }
        dataStore.edit { prefs ->
            prefs[ThemePreferencesRepository.KEY_SELECTED_THEME_ID] = "MALFORMED_CORRUPT_THEME"
        }
        val repository = ThemePreferencesRepository(dataStore)
        val state = repository.preferencesFlow.first()
        assertEquals(MiMomentoThemeId.SKY, state.selectedThemeId)
    }

    @Test
    fun preferences_defaultAutoThemeEnabledIsFalse() = runBlocking {
        val repository = createRepository()
        val state = repository.preferencesFlow.first()
        assertFalse("autoThemeEnabled must default to false", state.autoThemeEnabled)
    }

    @Test
    fun preferences_defaultAutoThemeSelectionIsSky() = runBlocking {
        val repository = createRepository()
        val state = repository.preferencesFlow.first()
        assertEquals(setOf(MiMomentoThemeId.SKY), state.autoThemeSelectedIds)
    }

    @Test
    fun preferences_setAutoThemeEnabledPersists() = runBlocking {
        val repository = createRepository()
        repository.setAutoThemeEnabled(true)
        val state = repository.preferencesFlow.first()
        assertTrue(state.autoThemeEnabled)
    }

    @Test
    fun preferences_setAutoThemeSelectedIdsPersists() = runBlocking {
        val repository = createRepository()
        val customSelection = setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN)
        repository.setAutoThemeSelectedIds(customSelection)
        val state = repository.preferencesFlow.first()
        assertEquals(customSelection, state.autoThemeSelectedIds)
    }

    // --- ROTACIÓN ---

    @Test
    fun rotation_whenAutoThemeDisabled_returnsSelectedTheme() {
        val repository = createRepository()
        val state = ThemePreferencesState(
            selectedThemeId = MiMomentoThemeId.SKY,
            autoThemeEnabled = false,
        )
        val resolved = repository.resolveSessionTheme(state)
        assertEquals(MiMomentoThemeId.SKY, resolved)
    }

    @Test
    fun rotation_whenZeroValidThemes_fallsBackToSky() {
        val repository = createRepository()
        // Policy where no themes are owned
        val emptyPolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = false
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = emptySet()
        }
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = emptySet(),
        )
        val resolved = repository.resolveSessionTheme(state, policy = emptyPolicy)
        assertEquals(MiMomentoThemeId.SKY, resolved)
    }

    @Test
    fun rotation_whenOneValidTheme_returnsThatTheme() {
        val repository = createRepository()
        val singlePolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = themeId == MiMomentoThemeId.DAWN
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = setOf(MiMomentoThemeId.DAWN)
        }
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = setOf(MiMomentoThemeId.DAWN),
        )
        val resolved = repository.resolveSessionTheme(state, policy = singlePolicy)
        assertEquals(MiMomentoThemeId.DAWN, resolved)
    }

    @Test
    fun rotation_whenTwoOrMoreValidThemes_choosesOnlyFromValidCandidates() {
        val repository = createRepository()
        val multiPolicy = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean =
                themeId == MiMomentoThemeId.SKY || themeId == MiMomentoThemeId.DAWN
            override fun getOwnedThemes(): Set<MiMomentoThemeId> =
                setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN)
        }
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN),
        )

        val randomWithSeed0 = Random(0)
        val randomWithSeed1 = Random(1)
        val resolved0 = repository.resolveSessionTheme(state, policy = multiPolicy, randomSource = randomWithSeed0)
        val resolved1 = repository.resolveSessionTheme(state, policy = multiPolicy, randomSource = randomWithSeed1)

        assertTrue(resolved0 == MiMomentoThemeId.SKY || resolved0 == MiMomentoThemeId.DAWN)
        assertTrue(resolved1 == MiMomentoThemeId.SKY || resolved1 == MiMomentoThemeId.DAWN)
    }

    @Test
    fun rotation_unownedThemesAreExcludedFromCandidates() {
        val repository = createRepository()
        // User has selected SKY and DAWN, but DAWN is not owned:
        val policyOnlySky = DefaultThemeAvailabilityPolicy() // only SKY is owned
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN),
        )
        val resolved = repository.resolveSessionTheme(state, policy = policyOnlySky)
        assertEquals("Unowned DAWN must be excluded, resulting in single valid theme SKY", MiMomentoThemeId.SKY, resolved)
    }

    @Test
    fun rotation_unselectedThemesAreExcludedFromCandidates() {
        val repository = createRepository()
        // User owns SKY, DAWN, NATURE, but only selected SKY and DAWN:
        val policyThreeOwned = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean =
                themeId in setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN, MiMomentoThemeId.NATURE)
            override fun getOwnedThemes(): Set<MiMomentoThemeId> =
                setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN, MiMomentoThemeId.NATURE)
        }
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN),
        )

        for (seed in 0..20) {
            val resolved = repository.resolveSessionTheme(state, policy = policyThreeOwned, randomSource = Random(seed))
            assertTrue(
                "NATURE was not selected for rotation so it must never be picked",
                resolved == MiMomentoThemeId.SKY || resolved == MiMomentoThemeId.DAWN,
            )
        }
    }

    @Test
    fun rotation_remainsStableDuringSession() {
        val repository = createRepository()
        val policyMulti = object : ThemeAvailabilityPolicy {
            override fun isThemeOwned(themeId: MiMomentoThemeId): Boolean = true
            override fun getOwnedThemes(): Set<MiMomentoThemeId> = MiMomentoThemeId.values().toSet()
        }
        val state = ThemePreferencesState(
            autoThemeEnabled = true,
            autoThemeSelectedIds = setOf(MiMomentoThemeId.SKY, MiMomentoThemeId.DAWN, MiMomentoThemeId.NATURE),
        )

        // Session start resolves once with a fixed seed:
        val fixedSessionSeed = 42L
        val sessionTheme = repository.resolveSessionTheme(state, policy = policyMulti, randomSource = Random(fixedSessionSeed))

        // Multiple calls during the same session with same initial decision return identical theme
        assertEquals(sessionTheme, repository.resolveSessionTheme(state, policy = policyMulti, randomSource = Random(fixedSessionSeed)))
    }
}
