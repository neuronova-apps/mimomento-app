package com.neuronova.mimomento.data.model

import com.neuronova.mimomento.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeCatalogTest {

    @Test
    fun catalog_containsExactlyFiveThemes() {
        assertEquals("Theme catalog must contain exactly 5 themes", 5, MiMomentoThemeCatalog.themes.size)
    }

    @Test
    fun skyTheme_isConfiguredAsDefault() {
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.DEFAULT_THEME)
        assertEquals(MiMomentoThemeId.SKY, MiMomentoThemeCatalog.DEFAULT_THEME.id)
    }

    @Test
    fun skyTheme_isFreeAndAlwaysAvailable() {
        val sky = MiMomentoThemeCatalog.SKY
        assertFalse("SKY must NOT be premium", sky.isPremium)
        assertEquals(ThemeTier.FREE, sky.tier)
        assertEquals(R.string.theme_sky, sky.nameRes)
        assertEquals(R.drawable.theme_sky_bg, sky.backgroundRes)
    }

    @Test
    fun dawnTheme_isConfiguredAsPremium() {
        val dawn = MiMomentoThemeCatalog.DAWN
        assertTrue("DAWN must be premium", dawn.isPremium)
        assertEquals(ThemeTier.PREMIUM, dawn.tier)
        assertEquals(R.string.theme_dawn, dawn.nameRes)
        assertEquals(R.drawable.theme_dawn_bg, dawn.backgroundRes)
    }

    @Test
    fun natureTheme_isConfiguredAsPremium() {
        val nature = MiMomentoThemeCatalog.NATURE
        assertTrue("NATURE must be premium", nature.isPremium)
        assertEquals(ThemeTier.PREMIUM, nature.tier)
        assertEquals(R.string.theme_nature, nature.nameRes)
        assertEquals(R.drawable.theme_nature_bg, nature.backgroundRes)
    }

    @Test
    fun scriptureTheme_isConfiguredAsPremium() {
        val scripture = MiMomentoThemeCatalog.SCRIPTURE
        assertTrue("SCRIPTURE must be premium", scripture.isPremium)
        assertEquals(ThemeTier.PREMIUM, scripture.tier)
        assertEquals(R.string.theme_scripture, scripture.nameRes)
        assertEquals(R.drawable.theme_scripture_bg, scripture.backgroundRes)
    }

    @Test
    fun sereneTheme_isConfiguredAsPremium() {
        val serene = MiMomentoThemeCatalog.SERENE
        assertTrue("SERENE must be premium", serene.isPremium)
        assertEquals(ThemeTier.PREMIUM, serene.tier)
        assertEquals(R.string.theme_serene, serene.nameRes)
        assertEquals(R.drawable.theme_serene_bg, serene.backgroundRes)
    }

    @Test
    fun themeLookup_returnsCorrectThemeForValidIds() {
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId("SKY"))
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId("sky"))
        assertEquals(MiMomentoThemeCatalog.DAWN, MiMomentoThemeCatalog.fromId("DAWN"))
        assertEquals(MiMomentoThemeCatalog.NATURE, MiMomentoThemeCatalog.fromId("NATURE"))
        assertEquals(MiMomentoThemeCatalog.SCRIPTURE, MiMomentoThemeCatalog.fromId("SCRIPTURE"))
        assertEquals(MiMomentoThemeCatalog.SERENE, MiMomentoThemeCatalog.fromId("SERENE"))
    }

    @Test
    fun themeLookup_fallsBackToSkyForCorruptOrUnknownId() {
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId("CORRUPTED_ID"))
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId(null))
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId(""))
        assertEquals(MiMomentoThemeCatalog.SKY, MiMomentoThemeCatalog.fromId("UNKNOWN"))
    }

    @Test
    fun allThemes_haveNonNullVisualDefinitions() {
        MiMomentoThemeCatalog.themes.forEach { theme ->
            assertNotNull("Visual definition must not be null for ${theme.id}", theme.visual)
            assertTrue("Overlay alpha must be between 0 and 1", theme.visual.overlayAlpha in 0f..1f)
            assertTrue("Decorative alpha must be between 0 and 1", theme.visual.decorativeAlpha in 0f..1f)
        }
    }

    @Test
    fun allThemes_haveUniqueAccentStylesConfigured() {
        assertEquals("SKY_CELESTIAL_ACCENT", MiMomentoThemeCatalog.SKY.accentStyle)
        assertEquals("DAWN_SUNRISE_ACCENT", MiMomentoThemeCatalog.DAWN.accentStyle)
        assertEquals("NATURE_LEAF_ACCENT", MiMomentoThemeCatalog.NATURE.accentStyle)
        assertEquals("SCRIPTURE_PARCHMENT_ACCENT", MiMomentoThemeCatalog.SCRIPTURE.accentStyle)
        assertEquals("SERENE_TWILIGHT_ACCENT", MiMomentoThemeCatalog.SERENE.accentStyle)
    }

    @Test
    fun allThemes_haveDistinctCardColors() {
        val cardColors = MiMomentoThemeCatalog.themes.map { it.visual.cardColor }.toSet()
        assertEquals("Each theme must have its own distinct cardColor", 5, cardColors.size)
    }
}
