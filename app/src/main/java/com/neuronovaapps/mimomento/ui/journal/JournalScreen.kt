package com.neuronovaapps.mimomento.ui.journal

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.neuronovaapps.mimomento.R
import com.neuronovaapps.mimomento.data.model.JournalEntry
import com.neuronovaapps.mimomento.data.model.JournalMood
import com.neuronovaapps.mimomento.ui.components.LoadingView
import com.neuronovaapps.mimomento.ui.theme.LocalActiveTheme
import com.neuronovaapps.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronovaapps.mimomento.ui.theme.themedCardBorder
import com.neuronovaapps.mimomento.ui.theme.themedCardColors
import com.neuronovaapps.mimomento.ui.theme.themedTopAppBarColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    JournalScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNewEntryClick = viewModel::openNewEntryEditor,
        onEntryClick = viewModel::openEditEntry,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
        onCloseEditor = viewModel::closeEditor,
        onEditorTextChanged = viewModel::updateEditorText,
        onSelectMood = viewModel::selectMood,
        onSaveEntry = viewModel::saveEntry,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreenContent(
    uiState: JournalUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNewEntryClick: () -> Unit = {},
    onEntryClick: (JournalEntry) -> Unit = {},
    onRequestDelete: (JournalEntry) -> Unit = {},
    onConfirmDelete: () -> Unit = {},
    onCancelDelete: () -> Unit = {},
    onCloseEditor: () -> Unit = {},
    onEditorTextChanged: (String) -> Unit = {},
    onSelectMood: (JournalMood?) -> Unit = {},
    onSaveEntry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.journal_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = themedTopAppBarColors(),
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && uiState.entries.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNewEntryClick,
                    containerColor = activeTheme.visual.buttonColor,
                    contentColor = activeTheme.visual.onButtonColor,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = {
                        Text(
                            text = stringResource(R.string.journal_new_entry),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }

                uiState.entries.isEmpty() -> {
                    JournalEmptyState(
                        onNewEntryClick = onNewEntryClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 16.dp,
                            bottom = 88.dp, // Spacing for FAB
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(
                            items = uiState.entries,
                            key = { it.id },
                        ) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                onClick = { onEntryClick(entry) },
                                onDeleteClick = { onRequestDelete(entry) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Editor Dialog
    if (uiState.isEditorOpen) {
        JournalEditorDialog(
            isEditing = uiState.editingEntryId != null,
            text = uiState.editorText,
            selectedMood = uiState.editorMood,
            errorMessage = uiState.editorError,
            onTextChanged = onEditorTextChanged,
            onSelectMood = onSelectMood,
            onSave = onSaveEntry,
            onDismiss = onCloseEditor,
            onDelete = {
                val entry = uiState.entries.firstOrNull { it.id == uiState.editingEntryId }
                if (entry != null) {
                    onRequestDelete(entry)
                }
            },
        )
    }

    // Delete Confirmation Dialog
    if (uiState.entryPendingDelete != null) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = {
                Text(
                    text = stringResource(R.string.journal_delete_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.journal_delete_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(stringResource(R.string.journal_action_delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onCancelDelete) {
                    Text(stringResource(R.string.journal_action_cancel))
                }
            },
            shape = RoundedCornerShape(16.dp),
        )
    }
}

@Composable
fun JournalEmptyState(
    onNewEntryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = themedCardColors(),
            border = themedCardBorder(),
            shape = MaterialTheme.shapes.large,
        ) {
            ThemedCardAccentLine()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = activeTheme.visual.surfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.size(64.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = activeTheme.visual.primary,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.journal_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.journal_empty_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNewEntryClick,
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeTheme.visual.buttonColor,
                        contentColor = activeTheme.visual.onButtonColor,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.journal_empty_action),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTheme = LocalActiveTheme.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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
            // Header Row: Date, Mood chip, Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatJournalDate(entry.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (entry.mood != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = activeTheme.visual.surfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier.padding(end = 4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = "${entry.mood.emoji} ${entry.mood.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = activeTheme.visual.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.journal_action_delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body preview
            Text(
                text = entry.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorDialog(
    isEditing: Boolean,
    text: String,
    selectedMood: JournalMood?,
    errorMessage: String?,
    onTextChanged: (String) -> Unit,
    onSelectMood: (JournalMood?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit = {},
) {
    val activeTheme = LocalActiveTheme.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 620.dp),
            colors = themedCardColors(),
            border = themedCardBorder(),
            shape = MaterialTheme.shapes.large,
        ) {
            ThemedCardAccentLine()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = if (isEditing) {
                            stringResource(R.string.journal_edit_entry)
                        } else {
                            stringResource(R.string.journal_new_entry)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.journal_action_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mood selector label
                Text(
                    text = stringResource(R.string.journal_mood_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Mood chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    JournalMood.entries.forEach { mood ->
                        val isSelected = selectedMood == mood
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMood(mood) },
                            label = {
                                Text(
                                    text = "${mood.emoji} ${mood.displayName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = activeTheme.visual.surfaceVariant,
                                selectedLabelColor = activeTheme.visual.primary,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text Input
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.journal_input_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    },
                    isError = errorMessage != null,
                    supportingText = if (errorMessage != null) {
                        {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeTheme.visual.primary,
                        unfocusedBorderColor = activeTheme.visual.borderColor,
                    ),
                    shape = MaterialTheme.shapes.medium,
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (isEditing) {
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.journal_action_delete))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    OutlinedButton(onClick = onDismiss) {
                        Text(stringResource(R.string.journal_action_cancel))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeTheme.visual.buttonColor,
                            contentColor = activeTheme.visual.onButtonColor,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.journal_action_save),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

private fun formatJournalDate(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("d 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
    return formatter.format(date)
}
