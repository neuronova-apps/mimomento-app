package com.neuronova.mimomento.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        secondaryContainer = theme.visual.surfaceVariant,
        onSecondaryContainer = theme.visual.secondary,
        tertiary = theme.visual.secondary,
        onTertiary = Color.White,
        tertiaryContainer = theme.visual.surfaceVariant.copy(alpha = 0.7f),
        onTertiaryContainer = theme.visual.primary,
        background = Color.Transparent,
        onBackground = theme.visual.onBackground,
        surface = theme.visual.surface,
        onSurface = theme.visual.onSurface,
        surfaceVariant = theme.visual.surfaceVariant,
        onSurfaceVariant = theme.visual.onSurface.copy(alpha = 0.72f),
        surfaceContainer = theme.visual.cardColor,
        surfaceContainerLow = theme.visual.cardColor,
        surfaceContainerHigh = theme.visual.surfaceVariant,
        surfaceContainerHighest = theme.visual.surfaceVariant,
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

/**
 * Línea de acento celestial sutil en la parte superior de tarjetas y secciones,
 * activa cuando el tema actual define el estilo de acento SKY_CELESTIAL_ACCENT.
 */
@Composable
fun ThemedCardAccentLine(
    modifier: Modifier = Modifier,
    alpha: Float = 0.6f,
) {
    val theme = LocalActiveTheme.current
    if (theme.accentStyle == "SKY_CELESTIAL_ACCENT") {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            theme.visual.primary.copy(alpha = 0.04f),
                            theme.visual.primary.copy(alpha = alpha),
                            theme.visual.secondary.copy(alpha = alpha * 0.85f),
                            theme.visual.primary.copy(alpha = 0.04f),
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun themedCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = LocalActiveTheme.current.visual.cardColor,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun themedCardBorder(isSelected: Boolean = false): BorderStroke = BorderStroke(
    width = if (isSelected) 2.dp else 1.dp,
    color = if (isSelected) MaterialTheme.colorScheme.primary else LocalActiveTheme.current.visual.borderColor,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun themedTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = LocalActiveTheme.current.visual.surface.copy(alpha = 0.88f),
    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
    titleContentColor = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
)
