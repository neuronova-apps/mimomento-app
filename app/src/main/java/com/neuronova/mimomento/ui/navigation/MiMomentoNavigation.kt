package com.neuronova.mimomento.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.neuronova.mimomento.R

object MiMomentoDestinations {
    const val HOME = "home"
    const val DEVOTIONALS = "devotionals"
    const val PRAYERS = "prayers"
    const val JOURNAL = "journal"
    const val PROGRESS = "progress"
    const val DEVOTIONAL_ID_ARG = "devotionalId"
    const val DEVOTIONAL_DETAIL_ROUTE = "devotionals/{$DEVOTIONAL_ID_ARG}"

    fun devotionalDetail(devotionalId: String): String = "devotionals/$devotionalId"
}

data class TopLevelDestination(
    val route: String,
    @get:StringRes val labelRes: Int,
    val icon: ImageVector,
    @get:StringRes val contentDescriptionRes: Int,
)

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = MiMomentoDestinations.HOME,
        labelRes = R.string.nav_home,
        icon = Icons.Default.Home,
        contentDescriptionRes = R.string.nav_home,
    ),
    TopLevelDestination(
        route = MiMomentoDestinations.DEVOTIONALS,
        labelRes = R.string.nav_devotionals,
        icon = Icons.AutoMirrored.Filled.MenuBook,
        contentDescriptionRes = R.string.nav_devotionals,
    ),
    TopLevelDestination(
        route = MiMomentoDestinations.PRAYERS,
        labelRes = R.string.nav_prayers,
        icon = Icons.Default.Favorite,
        contentDescriptionRes = R.string.nav_prayers,
    ),
    TopLevelDestination(
        route = MiMomentoDestinations.JOURNAL,
        labelRes = R.string.nav_journal,
        icon = Icons.Default.Edit,
        contentDescriptionRes = R.string.nav_journal,
    ),
    TopLevelDestination(
        route = MiMomentoDestinations.PROGRESS,
        labelRes = R.string.nav_progress,
        icon = Icons.Default.DateRange,
        contentDescriptionRes = R.string.nav_progress,
    ),
)
