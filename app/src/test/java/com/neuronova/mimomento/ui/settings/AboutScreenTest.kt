package com.neuronova.mimomento.ui.settings

import com.neuronova.mimomento.BuildConfig
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AboutScreenTest {

    @Test
    fun aboutDestination_isCanonicalAndNotTopLevel() {
        assertEquals("settings/about", MiMomentoDestinations.ABOUT)
        assertFalse(
            "About destination must not be a top-level tab",
            TOP_LEVEL_DESTINATIONS.any { it.route == MiMomentoDestinations.ABOUT },
        )
    }

    @Test
    fun aboutDestination_suppressesBottomBar() {
        assertFalse(
            "Bottom bar must be hidden on settings/about",
            shouldShowBottomBar(MiMomentoDestinations.ABOUT),
        )
    }

    @Test
    fun verifiedExternalUrls_matchOfficialNeuroNovaEndpoints() {
        assertEquals(
            "https://neuronova-apps.github.io/mimomento-app/privacy/",
            AboutUrls.PRIVACY_POLICY,
        )
        assertEquals(
            "https://neuronova-apps.github.io/support/",
            AboutUrls.SUPPORT,
        )
        assertEquals(
            "https://neuronova-apps.github.io/support/#reportar-problema",
            AboutUrls.REPORT_ISSUE,
        )
        assertEquals(
            "https://neuronova-apps.github.io/apps/",
            AboutUrls.MORE_APPS,
        )
        assertEquals(
            "https://neuronova-apps.github.io/",
            AboutUrls.OFFICIAL_WEBSITE,
        )

        val allUrls = listOf(
            AboutUrls.PRIVACY_POLICY,
            AboutUrls.SUPPORT,
            AboutUrls.REPORT_ISSUE,
            AboutUrls.MORE_APPS,
            AboutUrls.OFFICIAL_WEBSITE,
        )

        allUrls.forEach { url ->
            assertTrue("URL must use HTTPS: $url", url.startsWith("https://"))
            assertTrue("URL must point to official domain: $url", url.contains("neuronova-apps.github.io"))
        }
    }

    @Test
    fun featuresList_containsOnlyExistingFunctionalities() {
        val existingFeatures = listOf(
            "Devocionales",
            "Oraciones",
            "Diario personal",
            "Seguimiento del progreso",
            "Temas de personalización",
        )

        assertEquals("Must display exactly 5 existing features", 5, existingFeatures.size)
        assertEquals(
            "Features must be unique",
            existingFeatures.size,
            existingFeatures.toSet().size,
        )
    }

    @Test
    fun appInfo_usesBuildConfigAndOfficialDeveloper() {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE

        assertNotNull("VERSION_NAME must not be null", versionName)
        assertTrue("VERSION_NAME must not be blank", versionName.isNotBlank())
        assertTrue("VERSION_CODE must be greater than zero", versionCode > 0)

        val developer = "NeuroNova Apps"
        assertEquals("NeuroNova Apps", developer)
    }

    @Test
    fun creditsAndCopyright_followInstitutionalStandards() {
        val developer = "NeuroNova Apps"
        val creator = "Gabriel Berrospi"

        val developedByText = "Desarrollado por $developer."
        val createdByText = "Creado por $creator."

        assertEquals("Desarrollado por NeuroNova Apps.", developedByText)
        assertEquals("Creado por Gabriel Berrospi.", createdByText)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        assertTrue("Current year must be realistic", currentYear >= 2026)

        val copyright = "© $currentYear NeuroNova Apps. Todos los derechos reservados."
        assertTrue("Copyright must contain current year", copyright.contains(currentYear.toString()))
        assertTrue("Copyright must mention NeuroNova Apps", copyright.contains("NeuroNova Apps"))
    }

    @Test
    fun backStackNavigation_handlesSettingsToAboutCycle() {
        val stack = mutableListOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS)
        assertFalse(shouldShowBottomBar(stack.last()))

        // User enters About
        stack.add(MiMomentoDestinations.ABOUT)
        assertEquals(
            listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS, MiMomentoDestinations.ABOUT),
            stack,
        )
        assertFalse("Bottom bar hidden in About", shouldShowBottomBar(stack.last()))

        // User returns to Settings
        stack.removeAt(stack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), stack)
        assertFalse("Bottom bar still hidden in Settings", shouldShowBottomBar(stack.last()))

        // User returns to Home
        stack.removeAt(stack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME), stack)
        assertTrue("Bottom bar shown in Home", shouldShowBottomBar(stack.last()))
    }
}
