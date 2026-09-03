package com.neuronova.mimomento.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neuronova.mimomento.ui.devotionals.DevotionalDetailScreen
import com.neuronova.mimomento.ui.devotionals.DevotionalsScreen
import com.neuronova.mimomento.ui.devotionals.DevotionalsViewModel
import androidx.compose.ui.res.stringResource
import com.neuronova.mimomento.ui.home.HomeScreen
import com.neuronova.mimomento.ui.journal.JournalScreen
import com.neuronova.mimomento.ui.prayers.PrayerGuideDetailScreen
import com.neuronova.mimomento.ui.prayers.PrayerRouteDetailScreen
import com.neuronova.mimomento.ui.prayers.PrayersScreen
import com.neuronova.mimomento.ui.prayers.PrayersViewModel
import com.neuronova.mimomento.ui.prayers.SpiritualMomentDetailScreen
import com.neuronova.mimomento.ui.progress.ProgressScreen
import com.neuronova.mimomento.ui.settings.SettingsScreen
import com.neuronova.mimomento.ui.settings.ThemesScreen
import com.neuronova.mimomento.ui.theme.ThemeViewModel
import com.neuronova.mimomento.ui.welcome.WelcomeScreen

@Composable
fun MiMomentoNavHost(
    navController: NavHostController,
    devotionalsViewModel: DevotionalsViewModel,
    prayersViewModel: PrayersViewModel,
    devotionalCount: Int,
    onNavigateToDevotionals: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = MiMomentoDestinations.START_DESTINATION,
    themeViewModel: ThemeViewModel? = null,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(route = MiMomentoDestinations.WELCOME) {
            WelcomeScreen(
                onStart = {
                    navController.navigate(MiMomentoDestinations.HOME) {
                        popUpTo(MiMomentoDestinations.WELCOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(route = MiMomentoDestinations.HOME) {
            HomeScreen(
                onNavigateToDevotionals = onNavigateToDevotionals,
                onNavigateToPrayers = {
                    navController.navigate(MiMomentoDestinations.PRAYERS) {
                        popUpTo(MiMomentoDestinations.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToJournal = {
                    navController.navigate(MiMomentoDestinations.JOURNAL) {
                        popUpTo(MiMomentoDestinations.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProgress = {
                    navController.navigate(MiMomentoDestinations.PROGRESS) {
                        popUpTo(MiMomentoDestinations.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToSettings = {
                    navController.navigate(MiMomentoDestinations.SETTINGS)
                },
                devotionalCount = devotionalCount,
            )
        }

        composable(route = MiMomentoDestinations.SETTINGS) {
            val currentThemeName = themeViewModel?.uiState?.collectAsState()?.value?.selectedTheme?.let {
                stringResource(it.nameRes)
            } ?: stringResource(com.neuronova.mimomento.R.string.theme_sky)

            SettingsScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToThemes = { navController.navigate(MiMomentoDestinations.THEMES) },
                currentThemeName = currentThemeName,
            )
        }

        composable(route = MiMomentoDestinations.THEMES) {
            if (themeViewModel != null) {
                ThemesScreen(
                    themeViewModel = themeViewModel,
                    onNavigateUp = { navController.navigateUp() },
                )
            }
        }

        composable(route = MiMomentoDestinations.DEVOTIONALS) {
            val uiState by devotionalsViewModel.uiState.collectAsState()
            DevotionalsScreen(
                uiState = uiState,
                onSelectCategory = devotionalsViewModel::selectCategory,
                onDevotionalClick = { devotionalId ->
                    navController.navigate(MiMomentoDestinations.devotionalDetail(devotionalId))
                },
            )
        }

        composable(route = MiMomentoDestinations.PRAYERS) {
            val uiState by prayersViewModel.uiState.collectAsState()
            PrayersScreen(
                uiState = uiState,
                onSelectSection = prayersViewModel::selectSection,
                onMomentClick = { momentId ->
                    navController.navigate(MiMomentoDestinations.spiritualMomentDetail(momentId))
                },
                onRouteClick = { routeId ->
                    navController.navigate(MiMomentoDestinations.prayerRouteDetail(routeId))
                },
                onGuideClick = { guideId ->
                    navController.navigate(MiMomentoDestinations.prayerGuideDetail(guideId))
                },
            )
        }

        composable(route = MiMomentoDestinations.JOURNAL) {
            JournalScreen()
        }

        composable(route = MiMomentoDestinations.PROGRESS) {
            ProgressScreen()
        }

        composable(
            route = MiMomentoDestinations.DEVOTIONAL_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MiMomentoDestinations.DEVOTIONAL_ID_ARG) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val devotionalId = backStackEntry.arguments?.getString(
                MiMomentoDestinations.DEVOTIONAL_ID_ARG,
            ).orEmpty()
            val detailUiState = devotionalsViewModel.getDevotionalDetailUiState(devotionalId)

            DevotionalDetailScreen(
                uiState = detailUiState,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToDevotional = { targetId ->
                    navController.navigate(MiMomentoDestinations.devotionalDetail(targetId)) {
                        popUpTo(MiMomentoDestinations.DEVOTIONAL_DETAIL_ROUTE) {
                            inclusive = true
                        }
                    }
                },
                onFinishDevotional = {
                    navController.navigate(MiMomentoDestinations.PRAYERS) {
                        popUpTo(MiMomentoDestinations.HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(
            route = MiMomentoDestinations.PRAYER_GUIDE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MiMomentoDestinations.PRAYER_GUIDE_ID_ARG) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val guideId = backStackEntry.arguments?.getString(
                MiMomentoDestinations.PRAYER_GUIDE_ID_ARG,
            ).orEmpty()
            val detailUiState = prayersViewModel.getGuideDetailUiState(guideId)

            PrayerGuideDetailScreen(
                uiState = detailUiState,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToDevotional = { devotionalId ->
                    navController.navigate(MiMomentoDestinations.devotionalDetail(devotionalId))
                },
            )
        }

        composable(
            route = MiMomentoDestinations.PRAYER_ROUTE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MiMomentoDestinations.PRAYER_ROUTE_ID_ARG) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString(
                MiMomentoDestinations.PRAYER_ROUTE_ID_ARG,
            ).orEmpty()

            PrayerRouteDetailScreen(
                routeId = routeId,
                prayersViewModel = prayersViewModel,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToDevotional = { devotionalId ->
                    navController.navigate(MiMomentoDestinations.devotionalDetail(devotionalId))
                },
            )
        }

        composable(
            route = MiMomentoDestinations.SPIRITUAL_MOMENT_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MiMomentoDestinations.SPIRITUAL_MOMENT_ID_ARG) {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val momentId = backStackEntry.arguments?.getString(
                MiMomentoDestinations.SPIRITUAL_MOMENT_ID_ARG,
            ).orEmpty()
            val detailUiState = prayersViewModel.getSpiritualMomentDetailUiState(momentId)

            SpiritualMomentDetailScreen(
                uiState = detailUiState,
                onNavigateUp = { navController.navigateUp() },
                onNavigateToDevotional = { devotionalId ->
                    navController.navigate(MiMomentoDestinations.devotionalDetail(devotionalId))
                },
            )
        }
    }
}
