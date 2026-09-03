package com.neuronova.mimomento.ui.prayers

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neuronova.mimomento.R
import com.neuronova.mimomento.data.model.Devotional
import com.neuronova.mimomento.data.model.PrayerGuide
import com.neuronova.mimomento.data.model.PrayerRoute
import com.neuronova.mimomento.data.model.SpiritualMoment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerGuideDetailScreen(
    uiState: PrayerGuideDetailUiState,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val guide = uiState.guide
    val isNotFound = uiState.isNotFound || guide == null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.prayer_guide_detail_title),
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
    ) { innerPadding ->
        if (isNotFound) {
            PrayerNotFoundView(
                onNavigateUp = onNavigateUp,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            PrayerGuideDetailContent(
                guide = guide!!,
                categoryNames = uiState.categoryNames,
                situationLabels = uiState.situationLabels,
                suggestedDevotional = uiState.suggestedDevotional,
                suggestedDevotionalCategoryName = uiState.suggestedDevotionalCategoryName,
                onNavigateUp = onNavigateUp,
                onNavigateToDevotional = onNavigateToDevotional,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrayerGuideDetailContent(
    guide: PrayerGuide,
    categoryNames: List<String>,
    situationLabels: List<String>,
    suggestedDevotional: Devotional?,
    suggestedDevotionalCategoryName: String,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (guide.mode.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrayerInfoBadge(
                    text = guide.mode,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = guide.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrayerSectionCard(
            title = stringResource(R.string.prayer_guide_purpose_title),
            content = guide.purpose,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrayerSectionCard(
            title = stringResource(R.string.prayer_guide_orientation_title),
            content = guide.guidance,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        if (categoryNames.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.prayer_topics_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                categoryNames.forEach { name ->
                    PrayerInfoBadge(text = name)
                }
            }
        }

        if (situationLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.prayer_situations_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                situationLabels.forEach { label ->
                    PrayerInfoBadge(text = label)
                }
            }
        }

        if (suggestedDevotional != null) {
            Spacer(modifier = Modifier.height(20.dp))
            SuggestedDevotionalCard(
                sectionTitle = stringResource(R.string.prayer_guide_deepen_title),
                sectionDescription = stringResource(R.string.prayer_guide_deepen_description),
                devotional = suggestedDevotional,
                categoryName = suggestedDevotionalCategoryName,
                onDevotionalClick = onNavigateToDevotional,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = onNavigateUp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.action_back_to_prayers))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerRouteDetailScreen(
    routeId: String,
    prayersViewModel: PrayersViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var isCompleted by rememberSaveable { mutableStateOf(false) }

    val sessionState = prayersViewModel.getRouteSessionUiState(
        routeId = routeId,
        stepIndex = currentStepIndex,
        isCompleted = isCompleted,
    )

    BackHandler {
        if (isCompleted) {
            onNavigateUp()
        } else if (currentStepIndex > 0) {
            currentStepIndex--
        } else {
            onNavigateUp()
        }
    }

    if (sessionState.isNotFound) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.prayer_route_detail_title),
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
        ) { innerPadding ->
            PrayerNotFoundView(
                onNavigateUp = onNavigateUp,
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else if (sessionState.isCompleted) {
        PrayerRouteCompletionContent(
            route = sessionState.route,
            totalSteps = sessionState.totalSteps,
            suggestedDevotional = sessionState.suggestedDevotional,
            suggestedDevotionalCategoryName = sessionState.suggestedDevotionalCategoryName,
            onNavigateToDevotional = onNavigateToDevotional,
            onNavigateUp = onNavigateUp,
            modifier = modifier,
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = sessionState.route?.name.orEmpty(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (currentStepIndex > 0) {
                                    currentStepIndex--
                                } else {
                                    onNavigateUp()
                                }
                            },
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (sessionState.hasPrevious) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(text = stringResource(R.string.action_previous))
                        }
                    }

                    if (sessionState.hasNext) {
                        Button(
                            onClick = { currentStepIndex++ },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(text = stringResource(R.string.action_next))
                        }
                    } else {
                        Button(
                            onClick = { isCompleted = true },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                        ) {
                            Text(text = stringResource(R.string.action_finish))
                        }
                    }
                }
            },
        ) { innerPadding ->
            val currentGuide = sessionState.currentGuide
            if (currentGuide != null) {
                PrayerRouteStepContent(
                    stepIndex = sessionState.currentStepIndex + 1,
                    totalSteps = sessionState.totalSteps,
                    guide = currentGuide,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun PrayerRouteStepContent(
    stepIndex: Int,
    totalSteps: Int,
    guide: PrayerGuide,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrayerInfoBadge(
                text = stringResource(R.string.prayer_step_progress, stepIndex, totalSteps),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            PrayerInfoBadge(
                text = guide.mode,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { if (totalSteps > 0) stepIndex.toFloat() / totalSteps.toFloat() else 0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = guide.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrayerSectionCard(
            title = stringResource(R.string.prayer_purpose_title),
            content = guide.purpose,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrayerSectionCard(
            title = stringResource(R.string.prayer_guidance_title),
            content = guide.guidance,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerRouteCompletionContent(
    route: PrayerRoute?,
    totalSteps: Int,
    suggestedDevotional: Devotional?,
    suggestedDevotionalCategoryName: String,
    onNavigateToDevotional: (String) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = route?.name.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.prayer_route_completed_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    R.string.prayer_route_completed_message,
                    totalSteps,
                    route?.name.orEmpty(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (suggestedDevotional != null) {
                Spacer(modifier = Modifier.height(24.dp))
                SuggestedDevotionalCard(
                    sectionTitle = stringResource(R.string.prayer_deepen_title),
                    sectionDescription = stringResource(R.string.prayer_deepen_description),
                    devotional = suggestedDevotional,
                    categoryName = suggestedDevotionalCategoryName,
                    onDevotionalClick = onNavigateToDevotional,
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = onNavigateUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.action_back_to_prayers))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritualMomentDetailScreen(
    uiState: SpiritualMomentDetailUiState,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val moment = uiState.moment
    val isNotFound = uiState.isNotFound || moment == null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.spiritual_moment_detail_title),
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
    ) { innerPadding ->
        if (isNotFound) {
            PrayerNotFoundView(
                onNavigateUp = onNavigateUp,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            SpiritualMomentDetailContent(
                moment = moment!!,
                suggestedDevotional = uiState.suggestedDevotional,
                suggestedDevotionalCategoryName = uiState.suggestedDevotionalCategoryName,
                onNavigateUp = onNavigateUp,
                onNavigateToDevotional = onNavigateToDevotional,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun SpiritualMomentDetailContent(
    moment: SpiritualMoment,
    suggestedDevotional: Devotional?,
    suggestedDevotionalCategoryName: String,
    onNavigateUp: () -> Unit,
    onNavigateToDevotional: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        if (moment.timeOfDay.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrayerInfoBadge(
                    text = moment.timeOfDay,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = moment.label,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (moment.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = moment.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
        }

        if (moment.suggestedInteraction.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))
            PrayerSectionCard(
                title = stringResource(R.string.prayer_moment_pause_title),
                content = moment.suggestedInteraction,
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                titleColor = MaterialTheme.colorScheme.primary,
            )
        }

        if (suggestedDevotional != null) {
            Spacer(modifier = Modifier.height(24.dp))
            SuggestedDevotionalCard(
                sectionTitle = stringResource(R.string.prayer_continue_moment_title),
                sectionDescription = stringResource(R.string.prayer_continue_moment_description),
                devotional = suggestedDevotional,
                categoryName = suggestedDevotionalCategoryName,
                onDevotionalClick = onNavigateToDevotional,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedButton(
            onClick = onNavigateUp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(text = stringResource(R.string.action_back_to_prayers))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SuggestedDevotionalCard(
    sectionTitle: String,
    sectionDescription: String,
    devotional: Devotional,
    categoryName: String,
    onDevotionalClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = sectionDescription,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClick = { onDevotionalClick(devotional.id) },
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
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
                    PrayerInfoBadge(
                        text = categoryName.ifBlank { devotional.categoryId },
                    )

                    Text(
                        text = stringResource(
                            R.string.devotional_minutes_format,
                            devotional.estimatedMinutes,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = devotional.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (devotional.bibleReference.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = devotional.bibleReference,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.prayer_read_devotional),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerSectionCard(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    titleColor: Color = MaterialTheme.colorScheme.primary,
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
fun PrayerNotFoundView(
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
                text = stringResource(R.string.prayer_not_found_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.prayer_not_found_message),
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
