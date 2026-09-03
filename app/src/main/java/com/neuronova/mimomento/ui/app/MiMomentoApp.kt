package com.neuronova.mimomento.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.repository.ThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import com.neuronova.mimomento.ui.components.ErrorView
import com.neuronova.mimomento.ui.components.LoadingView
import com.neuronova.mimomento.ui.devotionals.DevotionalsViewModel
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.MiMomentoNavHost
import com.neuronova.mimomento.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import com.neuronova.mimomento.ui.prayers.PrayersViewModel
import com.neuronova.mimomento.ui.theme.MiMomentoTheme
import com.neuronova.mimomento.ui.theme.ThemedBackground
import com.neuronova.mimomento.ui.theme.ThemeUiState
import com.neuronova.mimomento.ui.theme.ThemeViewModel

@Composable
fun MiMomentoApp(
    repository: MiMomentoContentRepository,
    themeRepository: ThemePreferencesRepository? = null,
    availabilityPolicy: ThemeAvailabilityPolicy = DefaultThemeAvailabilityPolicy(),
    previewPolicy: com.neuronova.mimomento.data.repository.DebugThemePreviewPolicy = com.neuronova.mimomento.data.repository.DefaultDebugThemePreviewPolicy(),
    validator: MiMomentoContentValidator = MiMomentoContentValidator(),
    appContentViewModel: AppContentViewModel = viewModel(
        factory = AppContentViewModel.provideFactory(repository, validator),
    ),
    devotionalsViewModel: DevotionalsViewModel = viewModel(
        factory = DevotionalsViewModel.provideFactory(repository),
    ),
    prayersViewModel: PrayersViewModel = viewModel(
        factory = PrayersViewModel.provideFactory(repository),
    ),
    themeViewModel: ThemeViewModel? = themeRepository?.let {
        viewModel(factory = ThemeViewModel.provideFactory(it, availabilityPolicy, previewPolicy))
    },
    navController: NavHostController = rememberNavController(),
) {
    val contentState by appContentViewModel.uiState.collectAsState()
    val themeUiState by themeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(ThemeUiState())
    }
    val activeTheme = themeUiState.activeTheme

    MiMomentoTheme(theme = activeTheme) {
        ThemedBackground(theme = activeTheme) {
            when (val state = contentState) {
                AppContentUiState.Loading -> {
                    LoadingView()
                }

                is AppContentUiState.Error -> {
                    ErrorView(
                        message = state.message ?: "No se pudo cargar el contenido local",
                        onRetry = { appContentViewModel.retry() },
                    )
                }

                is AppContentUiState.Ready -> {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val showBottomBar = shouldShowBottomBar(currentDestination?.route)

                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar(
                                    containerColor = activeTheme.visual.cardColor.copy(alpha = 0.94f),
                                    modifier = Modifier.drawBehind {
                                        drawLine(
                                            color = activeTheme.visual.borderColor.copy(alpha = 0.5f),
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = 1.dp.toPx(),
                                        )
                                    },
                                ) {
                                    TOP_LEVEL_DESTINATIONS.forEach { topLevel ->
                                        val isSelected = currentDestination?.hierarchy?.any {
                                            it.route == topLevel.route
                                        } == true

                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = {
                                                navController.navigate(topLevel.route) {
                                                    popUpTo(MiMomentoDestinations.HOME) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = topLevel.icon,
                                                    contentDescription = stringResource(
                                                        topLevel.contentDescriptionRes,
                                                    ),
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = stringResource(topLevel.labelRes),
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = activeTheme.visual.primary,
                                                selectedTextColor = activeTheme.visual.primary,
                                                indicatorColor = activeTheme.visual.surfaceVariant.copy(alpha = 0.85f),
                                                unselectedIconColor = activeTheme.visual.onSurface.copy(alpha = 0.65f),
                                                unselectedTextColor = activeTheme.visual.onSurface.copy(alpha = 0.65f),
                                            ),
                                        )
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        MiMomentoNavHost(
                            navController = navController,
                            devotionalsViewModel = devotionalsViewModel,
                            prayersViewModel = prayersViewModel,
                            themeViewModel = themeViewModel,
                            devotionalCount = state.devotionalCount,
                            onNavigateToDevotionals = {
                                navController.navigate(MiMomentoDestinations.DEVOTIONALS) {
                                    popUpTo(MiMomentoDestinations.HOME) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(innerPadding),
                        )
                    }
                }
            }
        }
    }
}
