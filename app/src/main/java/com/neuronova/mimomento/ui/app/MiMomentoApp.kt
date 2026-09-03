package com.neuronova.mimomento.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.neuronova.mimomento.data.repository.MiMomentoContentRepository
import com.neuronova.mimomento.data.validation.MiMomentoContentValidator
import com.neuronova.mimomento.ui.components.ErrorView
import com.neuronova.mimomento.ui.components.LoadingView
import com.neuronova.mimomento.ui.devotionals.DevotionalsViewModel
import com.neuronova.mimomento.ui.navigation.MiMomentoDestinations
import com.neuronova.mimomento.ui.navigation.MiMomentoNavHost
import com.neuronova.mimomento.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.neuronova.mimomento.ui.navigation.shouldShowBottomBar
import com.neuronova.mimomento.ui.prayers.PrayersViewModel

@Composable
fun MiMomentoApp(
    repository: MiMomentoContentRepository,
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
    navController: NavHostController = rememberNavController(),
) {
    val contentState by appContentViewModel.uiState.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
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
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
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
                                                Text(text = stringResource(topLevel.labelRes))
                                            },
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
