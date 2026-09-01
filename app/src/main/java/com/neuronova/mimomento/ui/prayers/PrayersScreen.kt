package com.neuronova.mimomento.ui.prayers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.neuronova.mimomento.R
import com.neuronova.mimomento.ui.components.PlaceholderSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.placeholder_prayers_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                },
            )
        },
    ) { innerPadding ->
        PlaceholderSection(
            title = stringResource(R.string.placeholder_prayers_title),
            message = stringResource(R.string.placeholder_prayers_message),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
