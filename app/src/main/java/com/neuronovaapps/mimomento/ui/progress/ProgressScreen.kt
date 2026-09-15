package com.neuronovaapps.mimomento.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neuronovaapps.mimomento.R
import com.neuronovaapps.mimomento.data.model.DayActivity
import com.neuronovaapps.mimomento.data.model.ProgressSummary
import com.neuronovaapps.mimomento.ui.components.LoadingView
import com.neuronovaapps.mimomento.ui.theme.LocalActiveTheme
import com.neuronovaapps.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronovaapps.mimomento.ui.theme.themedCardBorder
import com.neuronovaapps.mimomento.ui.theme.themedCardColors
import com.neuronovaapps.mimomento.ui.theme.themedTopAppBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.progress_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = themedTopAppBarColors(),
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            ProgressUiState.Loading -> {
                LoadingView(modifier = Modifier.padding(innerPadding))
            }
            is ProgressUiState.Ready -> {
                if (state.isEmpty) {
                    ProgressEmptyView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                } else {
                    ProgressContent(
                        summary = state.summary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressEmptyView(
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(activeTheme.visual.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = activeTheme.visual.primary,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.progress_empty_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.progress_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProgressContent(
    summary: ProgressSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Resumen General
        OverviewCard(summary = summary)

        // 2. Actividad últimos 7 días
        WeeklyActivityCard(summary = summary)

        // 3. Lectura y Oración
        ReadingAndPrayerCard(summary = summary)

        // 4. Actividad Mensual
        MonthlyActivityCard(summary = summary)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OverviewCard(
    summary: ProgressSummary,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.progress_section_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetricRow(
                icon = Icons.Outlined.CalendarMonth,
                title = stringResource(R.string.progress_active_days),
                value = stringResource(R.string.progress_active_days_format, summary.totalActiveDays),
                iconTint = activeTheme.visual.primary,
            )

            if (summary.currentStreakDays > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                MetricRow(
                    icon = Icons.Outlined.Spa,
                    title = stringResource(R.string.progress_streak_days),
                    value = stringResource(R.string.progress_streak_format, summary.currentStreakDays),
                    iconTint = activeTheme.visual.secondary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                icon = Icons.Outlined.EditNote,
                title = stringResource(R.string.progress_journal_entries),
                value = stringResource(R.string.progress_journal_entries_format, summary.journalEntriesCount),
                iconTint = activeTheme.visual.iconTint,
            )
        }
    }
}

@Composable
private fun WeeklyActivityCard(
    summary: ProgressSummary,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current
    val weekly = summary.weeklyActivity

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.progress_section_weekly),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.progress_weekly_active_format, weekly.activeDaysCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 7 Days Visual Representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                weekly.days.forEach { dayActivity ->
                    DayIndicator(
                        dayActivity = dayActivity,
                        activeThemeColor = activeTheme.visual.primary,
                        inactiveColor = activeTheme.visual.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayIndicator(
    dayActivity: DayActivity,
    activeThemeColor: androidx.compose.ui.graphics.Color,
    inactiveColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val dayLabel = formatDayLabel(dayActivity.dateMillis)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (dayActivity.hasActivity) activeThemeColor else inactiveColor),
            contentAlignment = Alignment.Center,
        ) {
            if (dayActivity.hasActivity) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Composable
private fun ReadingAndPrayerCard(
    summary: ProgressSummary,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.progress_section_reading_prayer),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetricRow(
                icon = Icons.Outlined.CheckCircle,
                title = stringResource(R.string.progress_devotionals_completed),
                value = "${summary.devotionalsCompletedCount}",
                iconTint = activeTheme.visual.primary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = stringResource(R.string.progress_devotionals_opened),
                value = "${summary.devotionalsOpenedCount}",
                iconTint = activeTheme.visual.secondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            MetricRow(
                icon = Icons.Outlined.SelfImprovement,
                title = stringResource(R.string.progress_prayers_opened),
                value = "${summary.prayersOpenedCount}",
                iconTint = activeTheme.visual.iconTint,
            )
        }
    }
}

@Composable
private fun MonthlyActivityCard(
    summary: ProgressSummary,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current
    val monthly = summary.monthlyActivity

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = themedCardColors(),
        border = themedCardBorder(),
        shape = MaterialTheme.shapes.medium,
    ) {
        ThemedCardAccentLine()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                text = stringResource(R.string.progress_section_monthly),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            MetricRow(
                icon = Icons.Outlined.CalendarMonth,
                title = stringResource(R.string.progress_monthly_active_format, monthly.activeDaysCount),
                value = stringResource(R.string.progress_monthly_events_format, monthly.totalEventsCount),
                iconTint = activeTheme.visual.primary,
            )
        }
    }
}

@Composable
private fun MetricRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTint,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatDayLabel(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("EEE", Locale.forLanguageTag("es-ES"))
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}
