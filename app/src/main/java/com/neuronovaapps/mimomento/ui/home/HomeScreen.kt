package com.neuronovaapps.mimomento.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuronovaapps.mimomento.R
import com.neuronovaapps.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronovaapps.mimomento.ui.theme.themedCardBorder
import com.neuronovaapps.mimomento.ui.theme.themedCardColors

@Composable
fun HomeScreen(
    onNavigateToDevotionals: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    devotionalCount: Int = 0,
    homeViewModel: HomeViewModel? = null,
) {
    val uiState by homeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(HomeUiState())
    }

    HomeScreenContent(
        uiState = uiState,
        onNavigateToDevotionals = onNavigateToDevotionals,
        onNavigateToPrayers = onNavigateToPrayers,
        onNavigateToJournal = onNavigateToJournal,
        onNavigateToProgress = onNavigateToProgress,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
        devotionalCount = devotionalCount,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToDevotionals: () -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    devotionalCount: Int = 0,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_content_description),
                            tint = MaterialTheme.colorScheme.onSurface,
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.home_question),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.home_question_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToDevotionals,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.home_read_devotional),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            FilledTonalButton(
                onClick = onNavigateToPrayers,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.home_go_prayers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.home_quick_access),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(
                    onClick = onNavigateToJournal,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = themedCardColors(),
                    border = themedCardBorder(),
                ) {
                    ThemedCardAccentLine()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.nav_journal),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Card(
                    onClick = onNavigateToProgress,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = themedCardColors(),
                    border = themedCardBorder(),
                ) {
                    ThemedCardAccentLine()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.nav_progress),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // BLOQUE 1: Tu momento de hoy
            // ==========================================
            Text(
                text = stringResource(R.string.home_section_moment_today),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                onClick = onNavigateToJournal,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = themedCardColors(),
                border = themedCardBorder(),
            ) {
                ThemedCardAccentLine()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                ) {
                    if (uiState.journalEntriesToday == 0) {
                        Text(
                            text = stringResource(R.string.home_moment_today_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_moment_today_empty_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FilledTonalButton(
                            onClick = onNavigateToJournal,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_moment_today_action_write),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.home_moment_today_recorded_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.journalEntriesToday == 1) {
                                stringResource(R.string.home_moment_today_entry_single)
                            } else {
                                stringResource(
                                    R.string.home_moment_today_entries_multiple,
                                    uiState.journalEntriesToday,
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        FilledTonalButton(
                            onClick = onNavigateToJournal,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.home_moment_today_action_view),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // BLOQUE 2: Esta semana
            // ==========================================
            Text(
                text = stringResource(R.string.home_section_this_week),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                onClick = onNavigateToProgress,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = themedCardColors(),
                border = themedCardBorder(),
            ) {
                ThemedCardAccentLine()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                ) {
                    val hasWeeklyActivity = uiState.activeDaysThisWeek > 0 ||
                        uiState.devotionalsThisWeek > 0 ||
                        uiState.journalEntriesThisWeek > 0

                    if (!hasWeeklyActivity) {
                        Text(
                            text = stringResource(R.string.home_weekly_empty_activity),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = if (uiState.activeDaysThisWeek == 1) {
                                stringResource(R.string.home_weekly_active_day_single)
                            } else {
                                stringResource(
                                    R.string.home_weekly_active_days_multiple,
                                    uiState.activeDaysThisWeek,
                                )
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val devotionalsText = if (uiState.devotionalsThisWeek == 1) {
                            stringResource(R.string.home_weekly_devotional_single)
                        } else {
                            stringResource(
                                R.string.home_weekly_devotionals_multiple,
                                uiState.devotionalsThisWeek,
                            )
                        }

                        val journalText = if (uiState.journalEntriesThisWeek == 1) {
                            stringResource(R.string.home_weekly_journal_single)
                        } else {
                            stringResource(
                                R.string.home_weekly_journals_multiple,
                                uiState.journalEntriesThisWeek,
                            )
                        }

                        Text(
                            text = "$devotionalsText · $journalText",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FilledTonalButton(
                        onClick = onNavigateToProgress,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.home_weekly_action_view_progress),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
