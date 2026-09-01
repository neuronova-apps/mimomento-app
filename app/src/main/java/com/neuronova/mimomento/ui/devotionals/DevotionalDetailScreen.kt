package com.neuronova.mimomento.ui.devotionals

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
    devotional: Devotional?,
    categoryName: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        if (devotional == null) {
            DevotionalNotFoundView(
                onNavigateUp = onNavigateUp,
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            DevotionalDetailContent(
                devotional = devotional,
                categoryName = categoryName,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun DevotionalDetailContent(
    devotional: Devotional,
    categoryName: String,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SuggestionChip(
                onClick = { /* informative badge */ },
                label = {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.labelSmall,
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

        Text(
            text = devotional.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = devotional.bibleReference,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(20.dp))

        DetailSection(
            title = stringResource(R.string.detail_central_idea),
            content = devotional.centralIdea,
            isHighlighted = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailSection(
            title = stringResource(R.string.detail_reflection),
            content = devotional.reflection,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailSection(
            title = stringResource(R.string.detail_personal_question),
            content = devotional.personalQuestion,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailSection(
            title = stringResource(R.string.detail_prayer_guide),
            content = devotional.prayerGuide,
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailSection(
            title = stringResource(R.string.detail_daily_action),
            content = devotional.dailyAction,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            },
        ),
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
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
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
            Button(onClick = onNavigateUp) {
                Text(text = stringResource(R.string.action_back))
            }
        }
    }
}
