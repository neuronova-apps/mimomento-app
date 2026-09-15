package com.neuronova.mimomento.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.neuronova.mimomento.R
import com.neuronova.mimomento.data.model.AppearanceMode
import com.neuronova.mimomento.data.model.MiMomentoThemeDefinition
import com.neuronova.mimomento.data.model.MiMomentoThemeId
import com.neuronova.mimomento.ui.theme.LocalHighContrast
import com.neuronova.mimomento.ui.theme.ThemeViewModel
import com.neuronova.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronova.mimomento.ui.theme.themedCardBorder
import com.neuronova.mimomento.ui.theme.themedCardColors
import com.neuronova.mimomento.ui.theme.themedSwitchColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    themeViewModel: ThemeViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by themeViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val premiumRequiredMessage = stringResource(R.string.theme_premium_required)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.themes_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateUp,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Sección: Modo de apariencia (Selector compacto)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = themedCardColors(),
                border = themedCardBorder(),
            ) {
                ThemedCardAccentLine()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.theme_appearance_mode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    AppearanceModeSelector(
                        currentMode = uiState.appearanceMode,
                        onModeSelected = { themeViewModel.setAppearanceMode(it) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Cambio automático
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = themedCardColors(),
                border = themedCardBorder(),
            ) {
                ThemedCardAccentLine()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.theme_auto_change),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.theme_auto_change_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.autoThemeEnabled,
                            onCheckedChange = { themeViewModel.toggleAutoTheme(it) },
                            enabled = uiState.canEnableAutoTheme,
                            colors = themedSwitchColors(),
                        )
                    }

                    if (!uiState.canEnableAutoTheme) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.theme_rotation_requires_multiple),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Indicador compacto de vista previa temporal activa
            val currentPreview = uiState.previewTheme
            if (currentPreview != null) {
                ThemePreviewBanner(
                    previewTheme = currentPreview,
                    onUseTheme = {
                        val confirmed = themeViewModel.confirmPreviewTheme()
                        if (!confirmed) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(premiumRequiredMessage)
                            }
                        }
                    },
                    onExitPreview = {
                        themeViewModel.clearThemePreview()
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Cabecera Catálogo de temas
            val isHC = LocalHighContrast.current
            Text(
                text = stringResource(R.string.themes_catalog_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Exactamente los 5 temas
            uiState.themes.forEach { theme ->
                val isPreviewing = theme.id == uiState.previewTheme?.id
                ThemeCard(
                    theme = theme,
                    isSelected = theme.id == uiState.selectedTheme.id,
                    isActive = theme.id == uiState.effectiveTheme.id,
                    isPreviewing = isPreviewing,
                    isOwned = uiState.isThemeOwned(theme.id),
                    onSelect = { themeViewModel.selectTheme(theme.id) },
                    onPreview = { themeViewModel.previewTheme(theme.id) },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ThemeCard(
    theme: MiMomentoThemeDefinition,
    isSelected: Boolean,
    isActive: Boolean,
    isPreviewing: Boolean,
    isOwned: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHC = LocalHighContrast.current
    val borderColor = if (isSelected) {
        if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary
    } else if (isPreviewing) {
        if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.tertiary
    } else {
        if (isHC) Color.White else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (isSelected || isPreviewing) 2.dp else if (isHC) 2.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            )
            .clickable {
                if (isOwned) {
                    onSelect()
                } else {
                    onPreview()
                }
            },
        shape = MaterialTheme.shapes.medium,
        colors = themedCardColors(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
            ) {
                Image(
                    painter = painterResource(id = theme.previewRes),
                    contentDescription = stringResource(theme.nameRes),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Badges en la imagen de preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (theme.id == MiMomentoThemeId.SKY) {
                        ThemeBadge(
                            text = stringResource(R.string.theme_default),
                            backgroundColor = if (isHC) Color(0xFF101010) else MaterialTheme.colorScheme.primaryContainer,
                            textColor = if (isHC) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else if (theme.isPremium) {
                        ThemeBadge(
                            text = stringResource(R.string.theme_premium),
                            icon = Icons.Default.Lock,
                            backgroundColor = if (isHC) Color(0xFF101010) else MaterialTheme.colorScheme.secondaryContainer,
                            textColor = if (isHC) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isPreviewing) {
                            ThemeBadge(
                                text = stringResource(R.string.theme_preview),
                                icon = Icons.Default.Visibility,
                                backgroundColor = if (isHC) Color(0xFF101010) else MaterialTheme.colorScheme.tertiaryContainer,
                                textColor = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }

                        if (isSelected) {
                            ThemeBadge(
                                text = stringResource(R.string.theme_current),
                                icon = Icons.Default.Check,
                                backgroundColor = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
                                textColor = if (isHC) Color.Black else MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(theme.nameRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = if (isSelected) {
                            stringResource(R.string.theme_current)
                        } else if (isPreviewing) {
                            stringResource(R.string.theme_preview_temporary)
                        } else if (isOwned) {
                            stringResource(R.string.theme_available)
                        } else {
                            stringResource(R.string.theme_locked)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected || isPreviewing) {
                            if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary
                        } else if (isOwned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(theme.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Botón discreto en la tarjeta si está bloqueado y no previsualizando
                if (!isOwned && !isPreviewing) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = onPreview,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.theme_preview),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val isHC = LocalHighContrast.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = if (isHC) BorderStroke(1.dp, textColor) else null,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ThemePreviewBanner(
    previewTheme: MiMomentoThemeDefinition,
    onUseTheme: () -> Unit,
    onExitPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHC = LocalHighContrast.current
    val fontScale = LocalDensity.current.fontScale

    val containerColor = if (isHC) Color(0xFF101010) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    val contentColor = if (isHC) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
    val borderColor = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(if (isHC) 2.dp else 1.5.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.theme_preview_temporary),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(previewTheme.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
                if (previewTheme.isPremium) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isHC) Color(0xFF101010) else MaterialTheme.colorScheme.secondaryContainer,
                        border = if (isHC) BorderStroke(1.dp, Color(0xFFFFD600)) else null,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(10.dp),
                            )
                            Text(
                                text = stringResource(R.string.theme_premium),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Disposición de botones: si fontScale > 1.15f (Grande / Muy grande), disposición vertical
            if (fontScale > 1.15f) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onUseTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHC) Color.Black else MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.theme_use_this_theme),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }

                    OutlinedButton(
                        onClick = onExitPreview,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = if (isHC) 2.dp else 1.dp,
                            color = if (isHC) Color.White else MaterialTheme.colorScheme.outline,
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isHC) Color.Black else Color.Transparent,
                            contentColor = if (isHC) Color.White else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preview_exit),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onUseTheme,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHC) Color.Black else MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.theme_use_this_theme),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }

                    OutlinedButton(
                        onClick = onExitPreview,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            width = if (isHC) 2.dp else 1.dp,
                            color = if (isHC) Color.White else MaterialTheme.colorScheme.outline,
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isHC) Color.Black else Color.Transparent,
                            contentColor = if (isHC) Color.White else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.theme_preview_exit),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceModeSelector(
    currentMode: AppearanceMode,
    onModeSelected: (AppearanceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHC = LocalHighContrast.current
    val options = listOf(
        AppearanceMode.DAY to stringResource(R.string.theme_appearance_day),
        AppearanceMode.NIGHT to stringResource(R.string.theme_appearance_night),
        AppearanceMode.SYSTEM to stringResource(R.string.theme_appearance_system),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (mode, label) ->
            val isSelected = currentMode == mode

            val backgroundColor = if (isHC) {
                if (isSelected) Color(0xFFFFD600) else Color(0xFF101010)
            } else {
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }

            val textColor = if (isHC) {
                if (isSelected) Color.Black else Color.White
            } else {
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            }

            val borderColor = if (isHC) {
                if (isSelected) Color(0xFFFFD600) else Color.White
            } else {
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            }

            val borderWidth = if (isHC) 2.dp else if (isSelected) 1.5.dp else 1.dp

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .selectable(
                        selected = isSelected,
                        onClick = { onModeSelected(mode) },
                        role = Role.RadioButton,
                    ),
                color = backgroundColor,
                shape = RoundedCornerShape(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
