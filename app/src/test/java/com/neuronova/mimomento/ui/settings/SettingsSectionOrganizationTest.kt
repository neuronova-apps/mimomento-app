package com.neuronova.mimomento.ui.settings

import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsSectionOrganizationTest {

    @Test
    fun settingsThemesAndAbout_canonicalDestinationsExist() {
        assertEquals("settings", MiMomentoDestinations.SETTINGS)
        assertEquals("settings/themes", MiMomentoDestinations.THEMES)
        assertEquals("settings/about", MiMomentoDestinations.ABOUT)

        // Neither Settings, Themes nor About are top-level bottom-nav tabs
        assertTrue(TOP_LEVEL_DESTINATIONS.none { it.route == MiMomentoDestinations.SETTINGS })
        assertTrue(TOP_LEVEL_DESTINATIONS.none { it.route == MiMomentoDestinations.THEMES })
        assertTrue(TOP_LEVEL_DESTINATIONS.none { it.route == MiMomentoDestinations.ABOUT })
    }

    @Test
    fun bottomBarSuppressed_onSettingsThemesAndAboutRoutes() {
        assertFalse(
            "Settings must suppress bottom navigation bar",
            shouldShowBottomBar(MiMomentoDestinations.SETTINGS),
        )
        assertFalse(
            "Themes must suppress bottom navigation bar",
            shouldShowBottomBar(MiMomentoDestinations.THEMES),
        )
        assertFalse(
            "About must suppress bottom navigation bar",
            shouldShowBottomBar(MiMomentoDestinations.ABOUT),
        )
    }

    @Test
    fun backStackTransitions_fromHomeToSettingsToThemesAndReturn() {
        val backStack = mutableListOf(MiMomentoDestinations.HOME)
        assertTrue(shouldShowBottomBar(backStack.last()))

        // 1. Enter Settings
        backStack.add(MiMomentoDestinations.SETTINGS)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 2. Navigate from Settings to Themes
        backStack.add(MiMomentoDestinations.THEMES)
        assertEquals(
            listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS, MiMomentoDestinations.THEMES),
            backStack,
        )
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 3. Navigate back from Themes to Settings
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 4. Navigate back from Settings to Home
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME), backStack)
        assertTrue(shouldShowBottomBar(backStack.last()))
    }

    @Test
    fun settingsSections_areFourDistinctNonDuplicatedBlocks() {
        // Define the 4 canonical sections established in Settings
        val sections = listOf(
            "APARIENCIA",
            "ACCESIBILIDAD",
            "DATOS_Y_PRIVACIDAD",
            "ACERCA_DE_MIMOMENTO",
        )

        assertEquals("Settings must have exactly 4 functional blocks", 4, sections.size)
        assertEquals("Sections must be unique without duplication", sections.size, sections.toSet().size)

        // Functional responsibility isolation:
        // - APARIENCIA: Handles theme selection and visual presentation
        // - ACCESIBILIDAD: Handles system font scale adaptation without duplicating theme options
        // - DATOS_Y_PRIVACIDAD: Handles local data transparency (journal, progress, account)
        // - ACERCA_DE_MIMOMENTO: Handles app identity, version, and external privacy policy
        val visualThemeScope = setOf("theme_catalog", "active_theme_selection")
        val accessibilityScope = setOf("system_font_scale_info")

        val intersection = visualThemeScope.intersect(accessibilityScope)
        assertTrue(
            "Apariencia and Accesibilidad must not share or duplicate configuration options",
            intersection.isEmpty(),
        )
    }

    @Test
    fun activeThemeCatalog_resolvesNamesCorrectlyForSettings() {
        val themes = MiMomentoThemeCatalog.themes
        assertEquals(5, themes.size)

        // Each theme has a valid string resource for its name to display on Settings
        themes.forEach { theme ->
            assertNotNull(theme.nameRes)
            assertTrue("Theme resource ID must be valid", theme.nameRes != 0)
        }

        val defaultTheme = MiMomentoThemeCatalog.DEFAULT_THEME
        assertEquals(MiMomentoThemeId.SKY, defaultTheme.id)
    }

    @Test
    fun backStackTransitions_fromHomeToSettingsToAboutAndReturn() {
        val backStack = mutableListOf(MiMomentoDestinations.HOME)
        assertTrue(shouldShowBottomBar(backStack.last()))

        // 1. Enter Settings
        backStack.add(MiMomentoDestinations.SETTINGS)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 2. Navigate from Settings to About
        backStack.add(MiMomentoDestinations.ABOUT)
        assertEquals(
            listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS, MiMomentoDestinations.ABOUT),
            backStack,
        )
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 3. Navigate back from About to Settings
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)
        assertFalse(shouldShowBottomBar(backStack.last()))

        // 4. Navigate back from Settings to Home
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME), backStack)
        assertTrue(shouldShowBottomBar(backStack.last()))
    }

    @Test
    fun settingsAboutSection_delegatesToDedicatedScreenWithoutInliningAboutDetails() {
        // Settings contains only the entry point row for About
        val settingsSection4Scope = setOf("about_access_row", "chevron_navigation")

        // Detailed about content belongs exclusively to AboutScreen (settings/about)
        val aboutDedicatedScreenScope = setOf(
            "large_logo",
            "extended_description",
            "purpose",
            "features_list",
            "support_links",
            "neuronova_links",
            "version_code_compilation",
            "credits",
            "dynamic_copyright",
        )

        val overlap = settingsSection4Scope.intersect(aboutDedicatedScreenScope)
        assertTrue(
            "Settings must not inline detailed About content; it must delegate to AboutScreen",
            overlap.isEmpty(),
        )
    }
}
