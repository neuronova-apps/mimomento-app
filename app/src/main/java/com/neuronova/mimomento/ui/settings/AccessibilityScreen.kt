package com.neuronova.mimomento.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import com.neuronova.mimomento.R
import com.neuronova.mimomento.data.model.TextScale
import com.neuronova.mimomento.ui.theme.LocalHighContrast
import com.neuronova.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronova.mimomento.ui.theme.themedCardBorder
import com.neuronova.mimomento.ui.theme.themedCardColors
import com.neuronova.mimomento.ui.theme.themedRadioButtonColors
import com.neuronova.mimomento.ui.theme.themedSwitchColors

@Composable
fun AccessibilityScreen(
    viewModel: AccessibilityViewModel,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    AccessibilityContent(
        uiState = uiState,
        onTextScaleChange = viewModel::setTextScale,
        onHighContrastChange = viewModel::toggleHighContrast,
        onReduceMotionChange = viewModel::toggleReduceMotion,
        onNavigateUp = onNavigateUp,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityContent(
    uiState: AccessibilityUiState,
    onTextScaleChange: (TextScale) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onReduceMotionChange: (Boolean) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.accessibility_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
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
            // Subtítulo introductorio
            Text(
                text = stringResource(R.string.accessibility_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Sección: Tamaño de texto
            AccessibilitySectionHeader(
                title = stringResource(R.string.settings_text_size),
                icon = Icons.Default.FormatSize,
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        text = stringResource(R.string.accessibility_text_size_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Opciones de tamaño de texto con rol semántico RadioGroup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val options = listOf(
                            TextScale.NORMAL to stringResource(R.string.settings_text_normal),
                            TextScale.LARGE to stringResource(R.string.settings_text_large),
                            TextScale.VERY_LARGE to stringResource(R.string.settings_text_extra_large),
                        )

                        options.forEach { (scale, label) ->
                            val isSelected = uiState.textScale == scale
                            TextScaleOptionRow(
                                label = label,
                                scale = scale,
                                isSelected = isSelected,
                                onClick = { onTextScaleChange(scale) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Vista previa del tamaño de texto
                    val isHC = LocalHighContrast.current
                    Text(
                        text = stringResource(R.string.accessibility_preview_title),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = if (isHC) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isHC) BorderStroke(2.dp, Color.White) else null,
                    ) {
                        Text(
                            text = stringResource(R.string.accessibility_preview_sample),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isHC) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Sección: Visualización (Alto contraste)
            AccessibilitySectionHeader(
                title = stringResource(R.string.accessibility_section_visual),
                icon = Icons.Default.Contrast,
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(R.string.settings_high_contrast),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.accessibility_high_contrast_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.highContrast,
                            onCheckedChange = onHighContrastChange,
                            colors = themedSwitchColors(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Sección: Movimiento (Reducir animaciones)
            AccessibilitySectionHeader(
                title = stringResource(R.string.accessibility_section_motion),
                icon = Icons.Default.Animation,
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(R.string.accessibility_reduce_motion_title),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.accessibility_reduce_motion_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = uiState.reduceMotion,
                            onCheckedChange = onReduceMotionChange,
                            colors = themedSwitchColors(),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Sección: Información sobre el sistema
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = themedCardColors(),
                border = themedCardBorder(),
            ) {
                ThemedCardAccentLine()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isHC = LocalHighContrast.current
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = stringResource(R.string.accessibility_info_system),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isHC) Color(0xFFF2F2F2) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TextScaleOptionRow(
    label: String,
    scale: TextScale,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isHC = LocalHighContrast.current
    val borderColor = if (isHC) {
        if (isSelected) Color(0xFFFFD600) else Color.White
    } else {
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
    val strokeWidth = if (isHC) 2.dp else if (isSelected) 2.dp else 1.dp
    val surfaceColor = if (isHC) {
        Color.Black
    } else if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(MaterialTheme.shapes.small)
            .border(
                width = strokeWidth,
                color = borderColor,
                shape = MaterialTheme.shapes.small,
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton,
            ),
        color = surfaceColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null, // Handled by parent selectable
                    modifier = Modifier.size(20.dp),
                    colors = themedRadioButtonColors(),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHC) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }

            val factorText = when (scale) {
                TextScale.NORMAL -> "1.00x"
                TextScale.LARGE -> "1.15x"
                TextScale.VERY_LARGE -> "1.30x"
            }

            Text(
                text = factorText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHC) {
                    if (isSelected) Color(0xFFFFD600) else Color.White
                } else {
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun AccessibilitySectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val isHC = LocalHighContrast.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.semantics { heading() },
        )
    }
}
