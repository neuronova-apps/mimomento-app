package com.neuronova.mimomento.data.model

import androidx.compose.ui.graphics.Color
import com.neuronova.mimomento.R

enum class MiMomentoThemeId {
    SKY,
    DAWN,
    NATURE,
    SCRIPTURE,
    SERENE,
}

enum class ThemeTier {
    FREE,
    PREMIUM,
}

data class ThemeVisualDefinition(
    val primary: Color,
    val secondary: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onBackground: Color,
    val cardColor: Color,
    val borderColor: Color,
    val iconTint: Color,
    val buttonColor: Color,
    val onButtonColor: Color,
    val overlayAlpha: Float = 0.85f,
    val scrimColor: Color,
    val decorativeAlpha: Float = 0.15f,
)

data class MiMomentoThemeDefinition(
    val id: MiMomentoThemeId,
    val nameRes: Int,
    val descriptionRes: Int,
    val backgroundRes: Int,
    val previewRes: Int = backgroundRes,
    val isPremium: Boolean,
    val tier: ThemeTier = if (isPremium) ThemeTier.PREMIUM else ThemeTier.FREE,
    val visual: ThemeVisualDefinition,
    val headerDecorationRes: Int? = null,
    val cardDecorationRes: Int? = null,
    val sectionDecorationRes: Int? = null,
    val accentStyle: String? = null,
)

object ThemeVisuals {
    val SKY = ThemeVisualDefinition(
        primary = Color(0xFF24668D),
        secondary = Color(0xFF4E7F99),
        surface = Color(0xFFF2F7FA),
        surfaceVariant = Color(0xFFE2ECF4),
        onSurface = Color(0xFF15232D),
        onBackground = Color(0xFF121F28),
        cardColor = Color(0xEEF2F7FC),
        borderColor = Color(0x3824668D),
        iconTint = Color(0xFF24668D),
        buttonColor = Color(0xFF24668D),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.62f,
        scrimColor = Color(0xFFEFF5FA),
        decorativeAlpha = 0.15f,
    )

    val DAWN = ThemeVisualDefinition(
        primary = Color(0xFF9E4B24),
        secondary = Color(0xFFC48B47),
        surface = Color(0xFFFDFBF7),
        surfaceVariant = Color(0xFFF4ECE4),
        onSurface = Color(0xFF261A14),
        onBackground = Color(0xFF1E140E),
        cardColor = Color(0xEEFDF8F2),
        borderColor = Color(0x389E4B24),
        iconTint = Color(0xFF9E4B24),
        buttonColor = Color(0xFF9E4B24),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.60f,
        scrimColor = Color(0xFFFAF2EB),
        decorativeAlpha = 0.15f,
    )

    val NATURE = ThemeVisualDefinition(
        primary = Color(0xFF2C6445),
        secondary = Color(0xFF508465),
        surface = Color(0xFFF3F8F5),
        surfaceVariant = Color(0xFFDFECE4),
        onSurface = Color(0xFF14241B),
        onBackground = Color(0xFF101F16),
        cardColor = Color(0xEEF2F8F4),
        borderColor = Color(0x382C6445),
        iconTint = Color(0xFF2C6445),
        buttonColor = Color(0xFF2C6445),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.62f,
        scrimColor = Color(0xFFEFF5F1),
        decorativeAlpha = 0.15f,
    )

    val SCRIPTURE = ThemeVisualDefinition(
        primary = Color(0xFF734934),
        secondary = Color(0xFF91654C),
        surface = Color(0xFFFAF6F0),
        surfaceVariant = Color(0xFFEFE6D9),
        onSurface = Color(0xFF261C16),
        onBackground = Color(0xFF1E150F),
        cardColor = Color(0xEEFAF5ED),
        borderColor = Color(0x38734934),
        iconTint = Color(0xFF734934),
        buttonColor = Color(0xFF734934),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.60f,
        scrimColor = Color(0xFFF8F3EA),
        decorativeAlpha = 0.15f,
    )

    val SERENE = ThemeVisualDefinition(
        primary = Color(0xFF534C7A),
        secondary = Color(0xFF736D96),
        surface = Color(0xFFF7F6FA),
        surfaceVariant = Color(0xFFE9E6F2),
        onSurface = Color(0xFF1E1A2B),
        onBackground = Color(0xFF171422),
        cardColor = Color(0xEEF5F4FA),
        borderColor = Color(0x38534C7A),
        iconTint = Color(0xFF534C7A),
        buttonColor = Color(0xFF534C7A),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.62f,
        scrimColor = Color(0xFFF3F1F7),
        decorativeAlpha = 0.15f,
    )
}

object MiMomentoThemeCatalog {
    val SKY = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SKY,
        nameRes = R.string.theme_sky,
        descriptionRes = R.string.theme_sky_desc,
        backgroundRes = R.drawable.theme_sky_bg,
        isPremium = false,
        visual = ThemeVisuals.SKY,
        accentStyle = "SKY_CELESTIAL_ACCENT",
    )

    val DAWN = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.DAWN,
        nameRes = R.string.theme_dawn,
        descriptionRes = R.string.theme_dawn_desc,
        backgroundRes = R.drawable.theme_dawn_bg,
        isPremium = true,
        visual = ThemeVisuals.DAWN,
        accentStyle = "DAWN_SUNRISE_ACCENT",
    )

    val NATURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.NATURE,
        nameRes = R.string.theme_nature,
        descriptionRes = R.string.theme_nature_desc,
        backgroundRes = R.drawable.theme_nature_bg,
        isPremium = true,
        visual = ThemeVisuals.NATURE,
        accentStyle = "NATURE_LEAF_ACCENT",
    )

    val SCRIPTURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SCRIPTURE,
        nameRes = R.string.theme_scripture,
        descriptionRes = R.string.theme_scripture_desc,
        backgroundRes = R.drawable.theme_scripture_bg,
        isPremium = true,
        visual = ThemeVisuals.SCRIPTURE,
        accentStyle = "SCRIPTURE_PARCHMENT_ACCENT",
    )

    val SERENE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SERENE,
        nameRes = R.string.theme_serene,
        descriptionRes = R.string.theme_serene_desc,
        backgroundRes = R.drawable.theme_serene_bg,
        isPremium = true,
        visual = ThemeVisuals.SERENE,
        accentStyle = "SERENE_TWILIGHT_ACCENT",
    )

    val themes: List<MiMomentoThemeDefinition> = listOf(
        SKY,
        DAWN,
        NATURE,
        SCRIPTURE,
        SERENE,
    )

    val DEFAULT_THEME: MiMomentoThemeDefinition = SKY

    fun fromId(id: String?): MiMomentoThemeDefinition {
        if (id == null) return DEFAULT_THEME
        return try {
            val themeId = MiMomentoThemeId.valueOf(id.trim().uppercase())
            fromThemeId(themeId)
        } catch (_: IllegalArgumentException) {
            DEFAULT_THEME
        }
    }

    fun fromThemeId(themeId: MiMomentoThemeId): MiMomentoThemeDefinition {
        return themes.firstOrNull { it.id == themeId } ?: DEFAULT_THEME
    }
}
