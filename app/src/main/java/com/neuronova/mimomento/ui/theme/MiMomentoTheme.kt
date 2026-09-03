package com.neuronova.mimomento.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition

val LocalActiveTheme = staticCompositionLocalOf<MiMomentoThemeDefinition> {
    MiMomentoThemeCatalog.DEFAULT_THEME
}

@Composable
fun MiMomentoTheme(
    theme: MiMomentoThemeDefinition = MiMomentoThemeCatalog.DEFAULT_THEME,
    content: @Composable () -> Unit,
) {
    val colorScheme = lightColorScheme(
        primary = theme.visual.primary,
        onPrimary = theme.visual.onButtonColor,
        primaryContainer = theme.visual.surfaceVariant,
        onPrimaryContainer = theme.visual.primary,
        secondary = theme.visual.secondary,
        onSecondary = Color.White,
        background = Color.Transparent,
        onBackground = theme.visual.onBackground,
        surface = theme.visual.surface,
        onSurface = theme.visual.onSurface,
        surfaceVariant = theme.visual.surfaceVariant,
        onSurfaceVariant = theme.visual.onSurface.copy(alpha = 0.72f),
        outline = theme.visual.borderColor,
        outlineVariant = theme.visual.borderColor.copy(alpha = 0.4f),
    )

    CompositionLocalProvider(LocalActiveTheme provides theme) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
