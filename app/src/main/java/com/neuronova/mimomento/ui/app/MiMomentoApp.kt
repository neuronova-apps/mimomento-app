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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neuronova.mimomento.data.repository.AccessibilityPreferencesRepository
import com.neuronova.mimomento.ui.settings.AccessibilityUiState
import com.neuronova.mimomento.ui.settings.AccessibilityViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neuronova.mimomento.data.local.FileJournalStorage
import com.neuronova.mimomento.data.local.FileProgressStorage
import com.neuronova.mimomento.data.model.MiMomentoThemeCatalog
import com.neuronova.mimomento.data.repository.DefaultThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.JournalRepository
import com.neuronova.mimomento.data.repository.LocalJournalRepository
import com.neuronova.mimomento.data.repository.LocalProgressRepository
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.repository.ProgressRepository
import com.neuronova.mimomento.data.repository.ThemeAvailabilityPolicy
import com.neuronova.mimomento.data.repository.ThemePreferencesRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import com.neuronova.mimomento.ui.components.ErrorView
import com.neuronova.mimomento.ui.components.LoadingView
import com.neuronova.mimomento.ui.devotionals.DevotionalsViewModel
import com.neuronova.mimomento.ui.journal.JournalViewModel
import com.neuronova.mimomento.ui.home.HomeViewModel
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.MiMomentoNavHost
import com.neuronova.mimomento.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import com.neuronova.mimomento.ui.prayers.PrayersViewModel
import com.neuronova.mimomento.ui.progress.ProgressViewModel
import com.neuronova.mimomento.ui.theme.MiMomentoTheme
import com.neuronova.mimomento.ui.theme.ThemedBackground
import com.neuronova.mimomento.ui.theme.ThemeUiState
import com.neuronova.mimomento.ui.theme.ThemeViewModel
import java.io.File

@Composable
fun MiMomentoApp(
    repository: MiMomentoContentRepository,
    themeRepository: ThemePreferencesRepository? = null,
    accessibilityRepository: AccessibilityPreferencesRepository? = null,
    journalRepository: JournalRepository? = null,
    progressRepository: ProgressRepository? = null,
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
    journalViewModel: JournalViewModel? = null,
    progressViewModel: ProgressViewModel? = null,
    homeViewModel: HomeViewModel? = null,
    themeViewModel: ThemeViewModel? = themeRepository?.let {
        viewModel(factory = ThemeViewModel.provideFactory(it, availabilityPolicy, previewPolicy))
    },
    accessibilityViewModel: AccessibilityViewModel? = accessibilityRepository?.let {
        viewModel(factory = AccessibilityViewModel.provideFactory(it))
    },
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val effectiveJournalRepository: JournalRepository = journalRepository ?: remember {
        LocalJournalRepository(FileJournalStorage(File(context.filesDir, "journal_entries.json")))
    }
    val effectiveProgressRepository: ProgressRepository = progressRepository ?: remember {
        LocalProgressRepository(
            storage = FileProgressStorage(File(context.filesDir, "progress_events.json")),
            journalRepository = effectiveJournalRepository,
        )
    }
    val effectiveJournalViewModel: JournalViewModel = journalViewModel ?: viewModel(
        factory = JournalViewModel.provideFactory(
            repository = effectiveJournalRepository,
            progressRepository = effectiveProgressRepository,
        )
    )
    val effectiveProgressViewModel: ProgressViewModel = progressViewModel ?: viewModel(
        factory = ProgressViewModel.provideFactory(
            repository = effectiveProgressRepository,
        )
    )
    val effectiveHomeViewModel: HomeViewModel = homeViewModel ?: viewModel(
        factory = HomeViewModel.provideFactory(
            journalRepository = effectiveJournalRepository,
            progressRepository = effectiveProgressRepository,
        )
    )
    val contentState by appContentViewModel.uiState.collectAsState()
    val themeUiState by themeViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(ThemeUiState())
    }
    val accessibilityUiState by accessibilityViewModel?.uiState?.collectAsState() ?: remember {
        mutableStateOf(AccessibilityUiState())
    }
    val activeTheme = themeUiState.activeTheme
    val systemInDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = com.neuronova.mimomento.data.model.resolveIsDarkTheme(themeUiState.appearanceMode, systemInDark)

    val currentDensity = LocalDensity.current
    val effectiveDensity = remember(currentDensity, accessibilityUiState.textScale) {
        Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * accessibilityUiState.effectiveMultiplier,
        )
    }

    CompositionLocalProvider(LocalDensity provides effectiveDensity) {
        MiMomentoTheme(
            theme = activeTheme,
            isDarkTheme = isDarkTheme,
            highContrast = accessibilityUiState.highContrast,
        ) {
            val currentEffectiveTheme = com.neuronova.mimomento.ui.theme.LocalActiveTheme.current
            ThemedBackground(theme = currentEffectiveTheme) {
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
                                val isHC = accessibilityUiState.highContrast
                                NavigationBar(
                                    containerColor = if (isHC) Color.Black else currentEffectiveTheme.visual.cardColor.copy(alpha = 0.94f),
                                    modifier = Modifier.drawBehind {
                                        drawLine(
                                            color = if (isHC) Color.White else currentEffectiveTheme.visual.borderColor.copy(alpha = 0.5f),
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = if (isHC) 2.dp.toPx() else 1.dp.toPx(),
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
                                                selectedIconColor = if (isHC) Color(0xFFFFD600) else currentEffectiveTheme.visual.primary,
                                                selectedTextColor = if (isHC) Color(0xFFFFD600) else currentEffectiveTheme.visual.primary,
                                                indicatorColor = if (isHC) Color(0xFF101010) else currentEffectiveTheme.visual.surfaceVariant.copy(alpha = 0.85f),
                                                unselectedIconColor = if (isHC) Color.White else currentEffectiveTheme.visual.onSurface.copy(alpha = 0.65f),
                                                unselectedTextColor = if (isHC) Color.White else currentEffectiveTheme.visual.onSurface.copy(alpha = 0.65f),
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
                            journalViewModel = effectiveJournalViewModel,
                            progressViewModel = effectiveProgressViewModel,
                            homeViewModel = effectiveHomeViewModel,
                            progressRepository = effectiveProgressRepository,
                            themeViewModel = themeViewModel,
                            accessibilityViewModel = accessibilityViewModel,
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
}
