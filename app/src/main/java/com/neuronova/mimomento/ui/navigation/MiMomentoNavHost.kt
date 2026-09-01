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
import com.neuronova.mimomento.ui.home.HomeScreen
import com.neuronova.mimomento.ui.journal.JournalScreen
import com.neuronova.mimomento.ui.prayers.PrayersScreen
import com.neuronova.mimomento.ui.progress.ProgressScreen

@Composable
fun MiMomentoNavHost(
    navController: NavHostController,
    devotionalsViewModel: DevotionalsViewModel,
    devotionalCount: Int,
    onNavigateToDevotionals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MiMomentoDestinations.HOME,
        modifier = modifier,
    ) {
        composable(route = MiMomentoDestinations.HOME) {
            HomeScreen(
                devotionalCount = devotionalCount,
                onNavigateToDevotionals = onNavigateToDevotionals,
            )
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
            PrayersScreen()
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
            val devotional = devotionalsViewModel.getDevotionalById(devotionalId)
            val categoryName = devotional?.let {
                devotionalsViewModel.getCategoryName(it.categoryId)
            }.orEmpty()

            DevotionalDetailScreen(
                devotional = devotional,
                categoryName = categoryName,
                onNavigateUp = { navController.navigateUp() },
            )
        }
    }
}
