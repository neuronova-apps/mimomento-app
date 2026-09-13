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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
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

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun MiMomentoTheme(
    theme: MiMomentoThemeDefinition = MiMomentoThemeCatalog.DEFAULT_THEME,
    isDarkTheme: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val effectiveVisual = remember(theme, isDarkTheme, highContrast) {
        theme.resolveVisual(isDarkTheme = isDarkTheme, highContrast = highContrast)
    }
    val effectiveTheme = remember(theme, effectiveVisual) {
        theme.copy(visual = effectiveVisual)
    }

    val colorScheme = if (highContrast) {
        darkColorScheme(
            primary = Color(0xFFFFD600),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF101010),
            onPrimaryContainer = Color(0xFFFFD600),
            secondary = Color.White,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF101010),
            onSecondaryContainer = Color.White,
            tertiary = Color(0xFFFFD600),
            onTertiary = Color.Black,
            tertiaryContainer = Color(0xFF101010),
            onTertiaryContainer = Color(0xFFFFD600),
            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF101010),
            onSurfaceVariant = Color(0xFFF2F2F2),
            surfaceContainer = Color(0xFF101010),
            surfaceContainerLow = Color.Black,
            surfaceContainerHigh = Color(0xFF101010),
            surfaceContainerHighest = Color(0xFF101010),
            outline = Color.White,
            outlineVariant = Color.White,
        )
    } else if (isDarkTheme) {
        darkColorScheme(
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
            onSurfaceVariant = effectiveVisual.onSurface.copy(alpha = 0.72f),
            surfaceContainer = effectiveVisual.cardColor,
            surfaceContainerLow = effectiveVisual.cardColor,
            surfaceContainerHigh = effectiveVisual.surfaceVariant,
            surfaceContainerHighest = effectiveVisual.surfaceVariant,
            outline = effectiveVisual.borderColor,
            outlineVariant = effectiveVisual.borderColor.copy(alpha = 0.4f),
        )
    } else {
        lightColorScheme(
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
            onSurfaceVariant = effectiveVisual.onSurface.copy(alpha = 0.72f),
            surfaceContainer = effectiveVisual.cardColor,
            surfaceContainerLow = effectiveVisual.cardColor,
            surfaceContainerHigh = effectiveVisual.surfaceVariant,
            surfaceContainerHighest = effectiveVisual.surfaceVariant,
            outline = effectiveVisual.borderColor,
            outlineVariant = effectiveVisual.borderColor.copy(alpha = 0.4f),
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity() ?: return@SideEffect
            val window = activity.window
            val insetsController = WindowCompat.getInsetsController(window, view)

            if (highContrast) {
                window.statusBarColor = Color.Black.toArgb()
                insetsController.isAppearanceLightStatusBars = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    window.navigationBarColor = Color.Black.toArgb()
                    insetsController.isAppearanceLightNavigationBars = false
                }
            } else if (isDarkTheme) {
                window.statusBarColor = effectiveVisual.surface.toArgb()
                insetsController.isAppearanceLightStatusBars = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    window.navigationBarColor = effectiveVisual.cardColor.toArgb()
                    insetsController.isAppearanceLightNavigationBars = false
                }
            } else {
                window.statusBarColor = effectiveVisual.surface.toArgb()
                insetsController.isAppearanceLightStatusBars = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    window.navigationBarColor = effectiveVisual.cardColor.toArgb()
                    insetsController.isAppearanceLightNavigationBars = true
                }
            }
        }
    }

    CompositionLocalProvider(
        LocalActiveTheme provides effectiveTheme,
        LocalHighContrast provides highContrast,
        LocalDarkTheme provides isDarkTheme,
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
    alpha: Float = if (LocalHighContrast.current) 1.0f else 0.6f,
) {
    if (LocalHighContrast.current) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(Color(0xFFFFD600)),
        )
        return
    }

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
fun themedCardColors(): CardColors {
    val isHighContrast = LocalHighContrast.current
    return CardDefaults.cardColors(
        containerColor = if (isHighContrast) Color(0xFF101010) else LocalActiveTheme.current.visual.cardColor,
        contentColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun themedCardBorder(isSelected: Boolean = false): BorderStroke {
    val isHighContrast = LocalHighContrast.current
    val strokeWidth = if (isHighContrast) 2.dp else if (isSelected) 2.dp else 1.dp
    val color = if (isHighContrast) {
        if (isSelected) Color(0xFFFFD600) else Color.White
    } else {
        if (isSelected) MaterialTheme.colorScheme.primary else LocalActiveTheme.current.visual.borderColor
    }
    return BorderStroke(width = strokeWidth, color = color)
}

@Composable
fun themedSwitchColors(): SwitchColors {
    val isHighContrast = LocalHighContrast.current
    return if (isHighContrast) {
        SwitchDefaults.colors(
            checkedThumbColor = Color.Black,
            checkedTrackColor = Color(0xFFFFD600),
            checkedBorderColor = Color(0xFFFFD600),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFF101010),
            uncheckedBorderColor = Color.White,
            disabledCheckedThumbColor = Color(0xFF101010),
            disabledCheckedTrackColor = Color(0xFF887700),
            disabledCheckedBorderColor = Color(0xFF887700),
            disabledUncheckedThumbColor = Color(0xFF888888),
            disabledUncheckedTrackColor = Color.Black,
            disabledUncheckedBorderColor = Color(0xFF888888),
        )
    } else {
        SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
fun themedRadioButtonColors(): RadioButtonColors {
    val isHighContrast = LocalHighContrast.current
    return if (isHighContrast) {
        RadioButtonDefaults.colors(
            selectedColor = Color(0xFFFFD600),
            unselectedColor = Color.White,
            disabledSelectedColor = Color(0xFF887700),
            disabledUnselectedColor = Color(0xFF888888),
        )
    } else {
        RadioButtonDefaults.colors()
    }
}

@Composable
fun themedButtonColors(): ButtonColors {
    val isHighContrast = LocalHighContrast.current
    return if (isHighContrast) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD600),
            contentColor = Color.Black,
            disabledContainerColor = Color(0xFF887700),
            disabledContentColor = Color(0xFF222222),
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun themedOutlinedButtonColors(): ButtonColors {
    val isHighContrast = LocalHighContrast.current
    return if (isHighContrast) {
        ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color.Black,
            disabledContentColor = Color(0xFF888888),
        )
    } else {
        ButtonDefaults.outlinedButtonColors()
    }
}

@Composable
fun themedOutlinedButtonBorder(): BorderStroke {
    val isHighContrast = LocalHighContrast.current
    return if (isHighContrast) {
        BorderStroke(2.dp, Color.White)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }
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
