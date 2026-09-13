package com.neuronova.mimomento.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition

val LocalActiveTheme = staticCompositionLocalOf<MiMomentoThemeDefinition> {
    MiMomentoThemeCatalog.DEFAULT_THEME
}

val LocalHighContrast = compositionLocalOf { false }

@Composable
fun MiMomentoTheme(
    theme: MiMomentoThemeDefinition = MiMomentoThemeCatalog.DEFAULT_THEME,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val effectiveVisual = remember(theme, highContrast) {
        if (highContrast) theme.visual.toHighContrast() else theme.visual
    }
    val effectiveTheme = remember(theme, effectiveVisual, highContrast) {
        if (highContrast) theme.copy(visual = effectiveVisual) else theme
    }

    val colorScheme = lightColorScheme(
        primary = effectiveVisual.primary,
        onPrimary = effectiveVisual.onButtonColor,
        primaryContainer = effectiveVisual.surfaceVariant,
        onPrimaryContainer = effectiveVisual.primary,
        secondary = effectiveVisual.secondary,
        onSecondary = Color.White,
        secondaryContainer = effectiveVisual.surfaceVariant,
        onSecondaryContainer = effectiveVisual.secondary,
        tertiary = effectiveVisual.secondary,
        onTertiary = Color.White,
        tertiaryContainer = effectiveVisual.surfaceVariant.copy(alpha = 0.7f),
        onTertiaryContainer = effectiveVisual.primary,
        background = Color.Transparent,
        onBackground = effectiveVisual.onBackground,
        surface = effectiveVisual.surface,
        onSurface = effectiveVisual.onSurface,
        surfaceVariant = effectiveVisual.surfaceVariant,
        onSurfaceVariant = if (highContrast) effectiveVisual.onSurface.copy(alpha = 0.95f) else effectiveVisual.onSurface.copy(alpha = 0.72f),
        surfaceContainer = effectiveVisual.cardColor,
        surfaceContainerLow = effectiveVisual.cardColor,
        surfaceContainerHigh = effectiveVisual.surfaceVariant,
        surfaceContainerHighest = effectiveVisual.surfaceVariant,
        outline = effectiveVisual.borderColor,
        outlineVariant = if (highContrast) effectiveVisual.borderColor.copy(alpha = 0.75f) else effectiveVisual.borderColor.copy(alpha = 0.4f),
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)

            window.statusBarColor = effectiveVisual.surface.toArgb()
            insetsController.isAppearanceLightStatusBars = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = effectiveVisual.cardColor.toArgb()
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(
        LocalActiveTheme provides effectiveTheme,
        LocalHighContrast provides highContrast,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Línea de acento temática sutil en la parte superior de tarjetas y secciones,
 * adaptada con una identidad visual única para cada tema del catálogo.
 */
@Composable
fun ThemedCardAccentLine(
    modifier: Modifier = Modifier,
    alpha: Float = if (LocalHighContrast.current) 0.95f else 0.6f,
) {
    val theme = LocalActiveTheme.current
    when (theme.accentStyle) {
        "SKY_CELESTIAL_ACCENT" -> {
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

        "DAWN_SUNRISE_ACCENT" -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                theme.visual.primary.copy(alpha = 0.03f),
                                theme.visual.primary.copy(alpha = alpha * 0.75f),
                                theme.visual.secondary.copy(alpha = alpha * 0.95f),
                                theme.visual.primary.copy(alpha = alpha * 0.75f),
                                theme.visual.primary.copy(alpha = 0.03f),
                            ),
                        ),
                    ),
            )
        }

        "NATURE_LEAF_ACCENT" -> {
            Canvas(
                modifier = modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
            ) {
                val strokeWidth = 2.dp.toPx()
                val brush = Brush.horizontalGradient(
                    colors = listOf(
                        theme.visual.primary.copy(alpha = 0.03f),
                        theme.visual.primary.copy(alpha = alpha * 0.75f),
                        theme.visual.secondary.copy(alpha = alpha * 0.90f),
                        theme.visual.primary.copy(alpha = alpha * 0.75f),
                        theme.visual.primary.copy(alpha = 0.03f),
                    ),
                )
                val path = Path().apply {
                    moveTo(0f, size.height * 0.25f)
                    quadraticTo(
                        size.width / 2f,
                        size.height * 0.85f,
                        size.width,
                        size.height * 0.25f,
                    )
                }
                drawPath(
                    path = path,
                    brush = brush,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                    ),
                )
            }
        }

        "SCRIPTURE_PARCHMENT_ACCENT" -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.2.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    theme.visual.primary.copy(alpha = 0.03f),
                                    theme.visual.primary.copy(alpha = alpha * 0.90f),
                                    theme.visual.secondary.copy(alpha = alpha * 0.80f),
                                    theme.visual.primary.copy(alpha = alpha * 0.90f),
                                    theme.visual.primary.copy(alpha = 0.03f),
                                ),
                            ),
                        ),
                )
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.6.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    theme.visual.secondary.copy(alpha = 0.02f),
                                    theme.visual.secondary.copy(alpha = alpha * 0.55f),
                                    theme.visual.primary.copy(alpha = alpha * 0.45f),
                                    theme.visual.secondary.copy(alpha = alpha * 0.55f),
                                    theme.visual.secondary.copy(alpha = 0.02f),
                                ),
                            ),
                        ),
                )
            }
        }

        "SERENE_TWILIGHT_ACCENT" -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                theme.visual.primary.copy(alpha = 0.03f),
                                theme.visual.secondary.copy(alpha = alpha * 0.80f),
                                theme.visual.primary.copy(alpha = alpha * 0.95f),
                                theme.visual.secondary.copy(alpha = alpha * 0.80f),
                                theme.visual.primary.copy(alpha = 0.03f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun themedCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = LocalActiveTheme.current.visual.cardColor,
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun themedCardBorder(isSelected: Boolean = false): BorderStroke {
    val isHighContrast = LocalHighContrast.current
    val strokeWidth = if (isSelected) 2.dp else if (isHighContrast) 1.5.dp else 1.dp
    val color = if (isSelected) MaterialTheme.colorScheme.primary else LocalActiveTheme.current.visual.borderColor
    return BorderStroke(width = strokeWidth, color = color)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun themedTopAppBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = Color.Transparent,
    scrolledContainerColor = LocalActiveTheme.current.visual.surface.copy(alpha = 0.88f),
    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
    titleContentColor = MaterialTheme.colorScheme.onBackground,
    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
)
