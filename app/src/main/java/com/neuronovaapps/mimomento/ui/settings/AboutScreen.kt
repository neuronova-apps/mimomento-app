package com.neuronovaapps.mimomento.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neuronovaapps.mimomento.BuildConfig
import com.neuronovaapps.mimomento.R
import com.neuronovaapps.mimomento.ui.theme.LocalHighContrast
import com.neuronovaapps.mimomento.ui.theme.ThemedCardAccentLine
import com.neuronovaapps.mimomento.ui.theme.themedCardBorder
import com.neuronovaapps.mimomento.ui.theme.themedCardColors
import java.util.Calendar

object AboutUrls {
    const val PRIVACY_POLICY = "https://neuronova-apps.github.io/mimomento-app/privacy/"
    const val SUPPORT = "https://neuronova-apps.github.io/support/"
    const val REPORT_ISSUE = "https://neuronova-apps.github.io/support/#reportar-problema"
    const val MORE_APPS = "https://neuronova-apps.github.io/apps/"
    const val OFFICIAL_WEBSITE = "https://neuronova-apps.github.io/"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.about_title),
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
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Hero
            AboutHero()

            Spacer(modifier = Modifier.height(24.dp))

            // Sección 1: Sobre Mi Momento
            AboutCard(title = stringResource(R.string.about_section_about)) {
                Text(
                    text = stringResource(R.string.about_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 2: Propósito
            AboutCard(title = stringResource(R.string.about_section_purpose)) {
                Text(
                    text = stringResource(R.string.about_purpose_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 3: Características (Solo funcionalidades reales existentes)
            AboutCard(title = stringResource(R.string.about_section_features)) {
                val features = listOf(
                    stringResource(R.string.about_feature_devotionals),
                    stringResource(R.string.about_feature_prayers),
                    stringResource(R.string.about_feature_journal),
                    stringResource(R.string.about_feature_progress),
                    stringResource(R.string.about_feature_themes),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    features.forEach { feature ->
                        BulletItem(text = feature)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 4: Privacidad y datos
            AboutCard(title = stringResource(R.string.about_section_privacy)) {
                val privacyPoints = listOf(
                    stringResource(R.string.about_privacy_journal),
                    stringResource(R.string.about_privacy_progress),
                    stringResource(R.string.about_privacy_account),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    privacyPoints.forEach { point ->
                        BulletItem(text = point)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                AboutExternalLink(
                    label = stringResource(R.string.about_privacy_policy),
                    url = AboutUrls.PRIVACY_POLICY,
                    onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Exception) {
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 5: Soporte
            AboutCard(title = stringResource(R.string.about_section_support)) {
                AboutExternalLink(
                    label = stringResource(R.string.about_support_contact),
                    url = AboutUrls.SUPPORT,
                    onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Exception) {
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutExternalLink(
                    label = stringResource(R.string.about_support_report),
                    url = AboutUrls.REPORT_ISSUE,
                    onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Exception) {
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 6: NeuroNova Apps
            AboutCard(title = stringResource(R.string.about_section_neuronova)) {
                AboutExternalLink(
                    label = stringResource(R.string.about_more_apps),
                    url = AboutUrls.MORE_APPS,
                    onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Exception) {
                        }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutExternalLink(
                    label = stringResource(R.string.about_official_website),
                    url = AboutUrls.OFFICIAL_WEBSITE,
                    onOpenUrl = { url ->
                        try {
                            uriHandler.openUri(url)
                        } catch (_: Exception) {
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 7: Información de la aplicación
            AboutCard(title = stringResource(R.string.about_section_app_info)) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppInfoItem(
                        label = stringResource(R.string.about_info_version),
                        value = BuildConfig.VERSION_NAME,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AppInfoItem(
                        label = stringResource(R.string.about_info_compilation),
                        value = BuildConfig.VERSION_CODE.toString(),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    AppInfoItem(
                        label = stringResource(R.string.about_info_developer),
                        value = stringResource(R.string.about_developer_name),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sección 8: Créditos
            AboutCard(title = stringResource(R.string.about_section_credits)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.about_credits_developed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.about_credits_creator),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Copyright al final
            Text(
                text = stringResource(R.string.about_copyright_format, currentYear),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isHC = LocalHighContrast.current
        Surface(
            modifier = Modifier.size(96.dp),
            shape = MaterialTheme.shapes.large,
            color = if (isHC) Color(0xFF101010) else Color.White,
            border = themedCardBorder(),
            tonalElevation = if (isHC) 0.dp else 2.dp,
            shadowElevation = if (isHC) 0.dp else 2.dp,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.mimomento_logo),
                    contentDescription = stringResource(R.string.about_app_name),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.about_app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.about_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.about_version_format, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "•",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.about_developer_name),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AboutCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            val isHC = LocalHighContrast.current
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun BulletItem(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AboutExternalLink(
    label: String,
    url: String,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val a11yDescription = stringResource(R.string.about_external_link_a11y, label)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = { onOpenUrl(url) })
            .semantics {
                contentDescription = a11yDescription
                role = Role.Button
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isHC = LocalHighContrast.current
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isHC) Color(0xFFFFD600) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun AppInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
