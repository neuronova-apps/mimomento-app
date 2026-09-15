package com.neuronovaapps.mimomento.data.model

import androidx.compose.ui.graphics.Color
import com.neuronovaapps.mimomento.R

enum class MiMomentoThemeId {
    SKY,
    DAWN,
    NATURE,
    SCRIPTURE,
    SERENE,
}

enum class AppearanceMode {
    DAY,
    NIGHT,
    SYSTEM;

    fun isDark(systemIsDark: Boolean? = null): Boolean {
        return when (this) {
            DAY -> false
            NIGHT -> true
            SYSTEM -> systemIsDark ?: false
        }
    }

    companion object {
        val DEFAULT = SYSTEM

        fun fromNameSafe(name: String?): AppearanceMode {
            if (name.isNullOrBlank()) return DEFAULT
            return try {
                valueOf(name.trim().uppercase())
            } catch (_: IllegalArgumentException) {
                DEFAULT
            }
        }
    }
}

fun resolveIsDarkTheme(
    mode: AppearanceMode,
    systemIsDark: Boolean? = null,
): Boolean = mode.isDark(systemIsDark)

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
) {
    fun toHighContrast(): ThemeVisualDefinition = copy(
        primary = Color(0xFFFFD600),
        secondary = Color(0xFFFFFFFF),
        surface = Color(0xFF000000),
        surfaceVariant = Color(0xFF101010),
        onSurface = Color(0xFFFFFFFF),
        onBackground = Color(0xFFFFFFFF),
        cardColor = Color(0xFF101010),
        borderColor = Color(0xFFFFFFFF),
        iconTint = Color(0xFFFFD600),
        buttonColor = Color(0xFFFFD600),
        onButtonColor = Color(0xFF000000),
        overlayAlpha = 1.0f,
        scrimColor = Color(0xFF000000),
        decorativeAlpha = 0.0f,
    )

    fun toNight(): ThemeVisualDefinition = copy(
        surface = Color(0xFF14181D),
        surfaceVariant = Color(0xFF202730),
        onSurface = Color(0xFFE5ECF2),
        onBackground = Color(0xFFEEF3F7),
        cardColor = Color(0xEE19212A),
        borderColor = primary.copy(alpha = 0.28f),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF0B1015),
    )
}

data class MiMomentoThemeDefinition(
    val id: MiMomentoThemeId,
    val nameRes: Int,
    val descriptionRes: Int,
    val backgroundRes: Int,
    val previewRes: Int = backgroundRes,
    val isPremium: Boolean,
    val tier: ThemeTier = if (isPremium) ThemeTier.PREMIUM else ThemeTier.FREE,
    val visual: ThemeVisualDefinition,
    val nightVisual: ThemeVisualDefinition = visual.toNight(),
    val headerDecorationRes: Int? = null,
    val cardDecorationRes: Int? = null,
    val sectionDecorationRes: Int? = null,
    val accentStyle: String? = null,
) {
    fun resolveVisual(isDarkTheme: Boolean, highContrast: Boolean): ThemeVisualDefinition = when {
        highContrast -> visual.toHighContrast()
        isDarkTheme -> nightVisual
        else -> visual
    }
}

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

    val SKY_NIGHT = ThemeVisualDefinition(
        primary = Color(0xFF68B2DF),
        secondary = Color(0xFF8FB9D4),
        surface = Color(0xFF10171E),
        surfaceVariant = Color(0xFF1B252F),
        onSurface = Color(0xFFE4EDF3),
        onBackground = Color(0xFFEDF4F8),
        cardColor = Color(0xEE162029),
        borderColor = Color(0x3868B2DF),
        iconTint = Color(0xFF68B2DF),
        buttonColor = Color(0xFF2B719B),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF0C1318),
        decorativeAlpha = 0.15f,
    )

    val DAWN_NIGHT = ThemeVisualDefinition(
        primary = Color(0xFFE28256),
        secondary = Color(0xFFDCA96C),
        surface = Color(0xFF1A1310),
        surfaceVariant = Color(0xFF281E19),
        onSurface = Color(0xFFF6ECE5),
        onBackground = Color(0xFFFAF2EC),
        cardColor = Color(0xEE221814),
        borderColor = Color(0x38E28256),
        iconTint = Color(0xFFE28256),
        buttonColor = Color(0xFFA65228),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF130D0A),
        decorativeAlpha = 0.15f,
    )

    val NATURE_NIGHT = ThemeVisualDefinition(
        primary = Color(0xFF65AE84),
        secondary = Color(0xFF8BBDA0),
        surface = Color(0xFF101813),
        surfaceVariant = Color(0xFF1B271F),
        onSurface = Color(0xFFE2EFE7),
        onBackground = Color(0xFFEFF7F2),
        cardColor = Color(0xEE152119),
        borderColor = Color(0x3865AE84),
        iconTint = Color(0xFF65AE84),
        buttonColor = Color(0xFF2F734E),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF0C130E),
        decorativeAlpha = 0.15f,
    )

    val SCRIPTURE_NIGHT = ThemeVisualDefinition(
        primary = Color(0xFFC08E74),
        secondary = Color(0xFFCFAC98),
        surface = Color(0xFF191411),
        surfaceVariant = Color(0xFF27201C),
        onSurface = Color(0xFFF3ECE5),
        onBackground = Color(0xFFFAF5EE),
        cardColor = Color(0xEE211A16),
        borderColor = Color(0x38C08E74),
        iconTint = Color(0xFFC08E74),
        buttonColor = Color(0xFF7F513A),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF120D0A),
        decorativeAlpha = 0.15f,
    )

    val SERENE_NIGHT = ThemeVisualDefinition(
        primary = Color(0xFFA29BCB),
        secondary = Color(0xFFB8B2DC),
        surface = Color(0xFF14121C),
        surfaceVariant = Color(0xFF211E2D),
        onSurface = Color(0xFFECEAF4),
        onBackground = Color(0xFFF4F3F9),
        cardColor = Color(0xEE1A1724),
        borderColor = Color(0x38A29BCB),
        iconTint = Color(0xFFA29BCB),
        buttonColor = Color(0xFF5B5388),
        onButtonColor = Color(0xFFFFFFFF),
        overlayAlpha = 0.90f,
        scrimColor = Color(0xFF0E0C14),
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
        nightVisual = ThemeVisuals.SKY_NIGHT,
        accentStyle = "SKY_CELESTIAL_ACCENT",
    )

    val DAWN = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.DAWN,
        nameRes = R.string.theme_dawn,
        descriptionRes = R.string.theme_dawn_desc,
        backgroundRes = R.drawable.theme_dawn_bg,
        isPremium = true,
        visual = ThemeVisuals.DAWN,
        nightVisual = ThemeVisuals.DAWN_NIGHT,
        accentStyle = "DAWN_SUNRISE_ACCENT",
    )

    val NATURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.NATURE,
        nameRes = R.string.theme_nature,
        descriptionRes = R.string.theme_nature_desc,
        backgroundRes = R.drawable.theme_nature_bg,
        isPremium = true,
        visual = ThemeVisuals.NATURE,
        nightVisual = ThemeVisuals.NATURE_NIGHT,
        accentStyle = "NATURE_LEAF_ACCENT",
    )

    val SCRIPTURE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SCRIPTURE,
        nameRes = R.string.theme_scripture,
        descriptionRes = R.string.theme_scripture_desc,
        backgroundRes = R.drawable.theme_scripture_bg,
        isPremium = true,
        visual = ThemeVisuals.SCRIPTURE,
        nightVisual = ThemeVisuals.SCRIPTURE_NIGHT,
        accentStyle = "SCRIPTURE_PARCHMENT_ACCENT",
    )

    val SERENE = MiMomentoThemeDefinition(
        id = MiMomentoThemeId.SERENE,
        nameRes = R.string.theme_serene,
        descriptionRes = R.string.theme_serene_desc,
        backgroundRes = R.drawable.theme_serene_bg,
        isPremium = true,
        visual = ThemeVisuals.SERENE,
        nightVisual = ThemeVisuals.SERENE_NIGHT,
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
