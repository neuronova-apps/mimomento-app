package com.neuronova.mimomento.ui.devotionals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neuronova.mimomento.R
import com.neuronova.mimomento.data.model.Devotional

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevotionalDetailScreen(
    uiState: DevotionalDetailUiState,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit = {},
    onFinishDevotional: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val devotional = uiState.devotional
    val isNotFound = uiState.isNotFound || devotional == null
    var showFinishDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
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
            )
        },
        bottomBar = {
            if (!isNotFound) {
                DevotionalDetailBottomBar(
                    previousDevotionalId = uiState.previousDevotionalId,
                    nextDevotionalId = uiState.nextDevotionalId,
                    onNavigateToDevotional = onNavigateToDevotional,
                )
            }
        },
    ) { innerPadding ->
        if (isNotFound) {
            DevotionalNotFoundView(
                onNavigateUp = onNavigateUp,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            DevotionalDetailContent(
                devotional = devotional!!,
                categoryName = uiState.categoryName,
                onFinishClick = { showFinishDialog = true },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.devotional_completed_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.devotional_completed_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        onFinishDevotional()
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(text = stringResource(R.string.devotional_back_to_prayers))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showFinishDialog = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(text = stringResource(R.string.devotional_continue_here))
                }
            },
        )
    }
}

@Composable
fun DevotionalDetailScreen(
    devotional: Devotional?,
    categoryName: String,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit = {},
    onFinishDevotional: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DevotionalDetailScreen(
        uiState = DevotionalDetailUiState(
            devotional = devotional,
            categoryName = categoryName,
            isNotFound = devotional == null,
        ),
        onNavigateUp = onNavigateUp,
        onNavigateToDevotional = onNavigateToDevotional,
        onFinishDevotional = onFinishDevotional,
        modifier = modifier,
    )
}

@Composable
private fun DevotionalDetailContent(
    devotional: Devotional,
    categoryName: String,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // 1. Category & Estimated Reading Time
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuggestionChip(
                onClick = { /* Informative category badge */ },
                label = {
                    Text(
                        text = categoryName.ifBlank { devotional.categoryId },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                    )
                },
            )

            Text(
                text = stringResource(
                    R.string.devotional_minutes_format,
                    devotional.estimatedMinutes,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Title
        Text(
            text = devotional.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Palabra / Referencia Bíblica
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = devotional.bibleReference,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Idea Central
        DetailSectionCard(
            title = stringResource(R.string.detail_central_idea),
            content = devotional.centralIdea,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Reflexión
        DetailSectionCard(
            title = stringResource(R.string.detail_reflection),
            content = devotional.reflection,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Pregunta personal
        DetailSectionCard(
            title = stringResource(R.string.detail_personal_question),
            content = devotional.personalQuestion,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.tertiary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Oración
        DetailSectionCard(
            title = stringResource(R.string.detail_prayer_guide),
            content = devotional.prayerGuide,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Acción para hoy
        DetailSectionCard(
            title = stringResource(R.string.detail_daily_action),
            content = devotional.dailyAction,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 9. Finalizar devocional
        Button(
            onClick = onFinishClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.devotional_finish),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
        }
    }
}

@Composable
private fun DevotionalDetailBottomBar(
    previousDevotionalId: String?,
    nextDevotionalId: String?,
    onNavigateToDevotional: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = {
                    previousDevotionalId?.let(onNavigateToDevotional)
                },
                enabled = previousDevotionalId != null,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.action_previous),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Button(
                onClick = {
                    nextDevotionalId?.let(onNavigateToDevotional)
                },
                enabled = nextDevotionalId != null,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.action_next),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun DevotionalNotFoundView(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.detail_not_found_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.detail_not_found_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onNavigateUp,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}
