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
        primary = Color(0xFF945729),
        secondary = Color(0xFFA66D3D),
        surface = Color(0xFFFCF8F5),
        surfaceVariant = Color(0xFFF3E8DE),
        onSurface = Color(0xFF2B1F17),
        onBackground = Color(0xFF221711),
        cardColor = Color(0xF2FFFFFF),
        borderColor = Color(0x33945729),
        iconTint = Color(0xFF945729),
        buttonColor = Color(0xFF945729),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.85f,
        scrimColor = Color(0xFFFAF3EC),
        decorativeAlpha = 0.15f,
    )

    val NATURE = ThemeVisualDefinition(
        primary = Color(0xFF336C4A),
        secondary = Color(0xFF4B8261),
        surface = Color(0xFFF5F9F6),
        surfaceVariant = Color(0xFFE1ECE3),
        onSurface = Color(0xFF1A261E),
        onBackground = Color(0xFF131F17),
        cardColor = Color(0xF2FFFFFF),
        borderColor = Color(0x33336C4A),
        iconTint = Color(0xFF336C4A),
        buttonColor = Color(0xFF336C4A),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.85f,
        scrimColor = Color(0xFFF0F6F2),
        decorativeAlpha = 0.15f,
    )

    val SCRIPTURE = ThemeVisualDefinition(
        primary = Color(0xFF7A4E38),
        secondary = Color(0xFF8F624C),
        surface = Color(0xFFFAF6F2),
        surfaceVariant = Color(0xFFEFE5DC),
        onSurface = Color(0xFF291E18),
        onBackground = Color(0xFF201611),
        cardColor = Color(0xF2FFFFFF),
        borderColor = Color(0x337A4E38),
        iconTint = Color(0xFF7A4E38),
        buttonColor = Color(0xFF7A4E38),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.85f,
        scrimColor = Color(0xFFF7F1EB),
        decorativeAlpha = 0.15f,
    )

    val SERENE = ThemeVisualDefinition(
        primary = Color(0xFF575283),
        secondary = Color(0xFF6E6A99),
        surface = Color(0xFFF7F6FA),
        surfaceVariant = Color(0xFFEBE8F2),
        onSurface = Color(0xFF211E2D),
        onBackground = Color(0xFF191624),
        cardColor = Color(0xF2FFFFFF),
        borderColor = Color(0x33575283),
        iconTint = Color(0xFF575283),
        buttonColor = Color(0xFF575283),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.85f,
        scrimColor = Color(0xFFF3F1F8),
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
    )

    val NATURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.NATURE,
        nameRes = R.string.theme_nature,
        descriptionRes = R.string.theme_nature_desc,
        backgroundRes = R.drawable.theme_nature_bg,
        isPremium = true,
        visual = ThemeVisuals.NATURE,
    )

    val SCRIPTURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SCRIPTURE,
        nameRes = R.string.theme_scripture,
        descriptionRes = R.string.theme_scripture_desc,
        backgroundRes = R.drawable.theme_scripture_bg,
        isPremium = true,
        visual = ThemeVisuals.SCRIPTURE,
    )

    val SERENE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SERENE,
        nameRes = R.string.theme_serene,
        descriptionRes = R.string.theme_serene_desc,
        backgroundRes = R.drawable.theme_serene_bg,
        isPremium = true,
        visual = ThemeVisuals.SERENE,
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
