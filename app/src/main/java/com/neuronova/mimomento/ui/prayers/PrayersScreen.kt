package com.neuronova.mimomento.ui.prayers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neuronova.mimomento.R
import com.neuronova.mimomento.data.model.PrayerGuide
import com.neuronova.mimomento.data.model.PrayerRoute
import com.neuronova.mimomento.data.model.SpiritualMoment
import com.neuronova.mimomento.ui.theme.LocalActiveTheme
import com.neuronova.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronova.mimomento.ui.theme.themedCardBorder
import com.neuronova.mimomento.ui.theme.themedCardColors
import com.neuronova.mimomento.ui.theme.themedTopAppBarColors

@Composable
fun PrayerInfoBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                icon()
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    uiState: PrayersUiState,
    onSelectSection: (PrayerSection) -> Unit,
    onMomentClick: (String) -> Unit,
    onRouteClick: (String) -> Unit,
    onGuideClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showHelpSheet by rememberSaveable { mutableStateOf(false) }

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            PrayersHelpBottomSheetContent(
                onDismiss = { showHelpSheet = false },
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.prayers_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showHelpSheet = true },
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = stringResource(R.string.prayers_help),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = themedTopAppBarColors(),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedSection.ordinal,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        color = LocalActiveTheme.current.visual.borderColor.copy(alpha = 0.5f),
                    )
                },
            ) {
                Tab(
                    selected = uiState.selectedSection == PrayerSection.MOMENTS,
                    onClick = { onSelectSection(PrayerSection.MOMENTS) },
                    text = {
                        Text(
                            text = "${stringResource(R.string.prayers_tab_moments)} (${uiState.spiritualMoments.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (uiState.selectedSection == PrayerSection.MOMENTS) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
                Tab(
                    selected = uiState.selectedSection == PrayerSection.ROUTES,
                    onClick = { onSelectSection(PrayerSection.ROUTES) },
                    text = {
                        Text(
                            text = "${stringResource(R.string.prayers_tab_routes)} (${uiState.prayerRoutes.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (uiState.selectedSection == PrayerSection.ROUTES) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
                Tab(
                    selected = uiState.selectedSection == PrayerSection.GUIDES,
                    onClick = { onSelectSection(PrayerSection.GUIDES) },
                    text = {
                        Text(
                            text = "${stringResource(R.string.prayers_tab_guides)} (${uiState.prayerGuides.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (uiState.selectedSection == PrayerSection.GUIDES) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }

            val explanationText = when (uiState.selectedSection) {
                PrayerSection.MOMENTS -> stringResource(R.string.prayers_moments_explanation)
                PrayerSection.ROUTES -> stringResource(R.string.prayers_routes_explanation)
                PrayerSection.GUIDES -> stringResource(R.string.prayers_guides_explanation)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            ) {
                Text(
                    text = explanationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                )
            }

            when (uiState.selectedSection) {
                PrayerSection.MOMENTS -> {
                    SpiritualMomentsList(
                        moments = uiState.spiritualMoments,
                        onMomentClick = onMomentClick,
                    )
                }

                PrayerSection.ROUTES -> {
                    PrayerRoutesList(
                        routes = uiState.prayerRoutes,
                        guidesById = uiState.prayerGuides.associateBy { it.id },
                        categoryNamesById = uiState.categoryNamesById,
                        onRouteClick = onRouteClick,
                    )
                }

                PrayerSection.GUIDES -> {
                    PrayerGuidesList(
                        guides = uiState.prayerGuides,
                        categoryNamesById = uiState.categoryNamesById,
                        onGuideClick = onGuideClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpiritualMomentsList(
    moments: List<SpiritualMoment>,
    onMomentClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (moments.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.prayers_empty_moments),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.prayers_moments_count, moments.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            items(
                items = moments,
                key = { it.id },
            ) { moment ->
                SpiritualMomentCard(
                    moment = moment,
                    onClick = { onMomentClick(moment.id) },
                )
            }
        }
    }
}

@Composable
private fun SpiritualMomentCard(
    moment: SpiritualMoment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (moment.timeOfDay.isNotBlank()) {
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
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = moment.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (moment.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = moment.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.prayer_open_moment),
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

@Composable
private fun PrayerRoutesList(
    routes: List<PrayerRoute>,
    guidesById: Map<String, PrayerGuide>,
    categoryNamesById: Map<String, String>,
    onRouteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (routes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.prayers_empty_routes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.prayers_routes_count, routes.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            items(
                items = routes,
                key = { it.id },
            ) { route ->
                val guideNames = route.guideIds.mapNotNull { id -> guidesById[id]?.name }
                PrayerRouteCard(
                    route = route,
                    guideNames = guideNames,
                    categoryNames = route.categoryIds.map { categoryNamesById[it] ?: it },
                    onClick = { onRouteClick(route.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrayerRouteCard(
    route: PrayerRoute,
    guideNames: List<String>,
    categoryNames: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
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
                PrayerInfoBadge(
                    text = route.estimatedDuration,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )

                PrayerInfoBadge(
                    text = stringResource(R.string.prayer_steps_count, route.guideIds.size),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = route.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = route.goal,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (guideNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    guideNames.forEachIndexed { index, name ->
                        PrayerInfoBadge(
                            text = "${index + 1}. $name",
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.prayer_start_route),
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

@Composable
private fun PrayerGuidesList(
    guides: List<PrayerGuide>,
    categoryNamesById: Map<String, String>,
    onGuideClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (guides.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.prayers_empty_guides),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.prayers_guides_count, guides.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            items(
                items = guides,
                key = { it.id },
            ) { guide ->
                PrayerGuideCard(
                    guide = guide,
                    categoryNames = guide.categoryIds.map { categoryNamesById[it] ?: it },
                    onClick = { onGuideClick(guide.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrayerGuideCard(
    guide: PrayerGuide,
    categoryNames: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (guide.mode.isNotBlank()) {
                PrayerInfoBadge(
                    text = guide.mode,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = guide.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (guide.purpose.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = guide.purpose,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (categoryNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    categoryNames.forEach { name ->
                        PrayerInfoBadge(
                            text = name,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.prayer_view_guide),
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

@Composable
private fun PrayersHelpBottomSheetContent(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(R.string.prayers_help_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrayersHelpSectionCard(
            title = stringResource(R.string.prayers_help_moments_title),
            body = stringResource(R.string.prayers_help_moments_body),
            extra = stringResource(R.string.prayers_help_moments_extra),
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrayersHelpSectionCard(
            title = stringResource(R.string.prayers_help_routes_title),
            body = stringResource(R.string.prayers_help_routes_body),
            extra = stringResource(R.string.prayers_help_routes_extra),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrayersHelpSectionCard(
            title = stringResource(R.string.prayers_help_guides_title),
            body = stringResource(R.string.prayers_help_guides_body),
            extra = stringResource(R.string.prayers_help_guides_extra),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
            titleColor = MaterialTheme.colorScheme.tertiary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.prayers_help_where_start_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prayers_help_where_start_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.prayers_help_dismiss),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun PrayersHelpSectionCard(
    title: String,
    body: String,
    extra: String,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalActiveTheme.current.visual.cardColor,
    titleColor: Color = MaterialTheme.colorScheme.primary,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine(alpha = 0.35f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = extra,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
            )
        }
    }
}
