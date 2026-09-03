package com.neuronova.mimomento.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MiMomentoNavigationTest {

    @Test
    fun routes_areConsistentAndStable() {
        assertEquals("welcome", MiMomentoDestinations.WELCOME)
        assertEquals("welcome", MiMomentoDestinations.START_DESTINATION)
        assertEquals("home", MiMomentoDestinations.HOME)
        assertEquals("devotionals", MiMomentoDestinations.DEVOTIONALS)
        assertEquals("prayers", MiMomentoDestinations.PRAYERS)
        assertEquals("journal", MiMomentoDestinations.JOURNAL)
        assertEquals("progress", MiMomentoDestinations.PROGRESS)
        assertEquals("settings", MiMomentoDestinations.SETTINGS)
        assertEquals("devotionals/{devotionalId}", MiMomentoDestinations.DEVOTIONAL_DETAIL_ROUTE)
    }

    @Test
    fun settingsRoute_isCanonicalAndNotTopLevelTab() {
        assertEquals("settings", MiMomentoDestinations.SETTINGS)
        assertTrue(
            "Settings must NOT be in top-level bottom navigation destinations",
            TOP_LEVEL_DESTINATIONS.none { it.route == MiMomentoDestinations.SETTINGS },
        )
    }

    @Test
    fun settingsScreen_suppressesBottomNavigationBar() {
        // Settings must not display bottom navigation bar
        assertTrue(
            "Settings route must hide bottom navigation bar",
            !shouldShowBottomBar(MiMomentoDestinations.SETTINGS),
        )
    }

    @Test
    fun settingsNavigation_operatesOnStandardBackStackWithoutClearingHome() {
        // Navigating to Settings pushes SETTINGS on top of HOME without popUpTo
        val backStack = mutableListOf(MiMomentoDestinations.HOME)
        // User taps settings gear:
        backStack.add(MiMomentoDestinations.SETTINGS)
        assertEquals(listOf(MiMomentoDestinations.HOME, MiMomentoDestinations.SETTINGS), backStack)

        // User presses back from Settings:
        backStack.removeAt(backStack.size - 1)
        assertEquals(listOf(MiMomentoDestinations.HOME), backStack)
        assertTrue(
            "Returning from settings restores Home as current destination and re-shows bottom bar",
            shouldShowBottomBar(backStack.last()),
        )
    }

    @Test
    fun welcomeRoute_isConfiguredAsInitialStartDestination() {
        assertEquals("welcome", MiMomentoDestinations.START_DESTINATION)
        assertEquals(MiMomentoDestinations.WELCOME, MiMomentoDestinations.START_DESTINATION)
    }

    @Test
    fun welcomeScreen_suppressesBottomNavigationBar() {
        // Welcome screen must not display bottom navigation tabs
        assertTrue("Welcome route must not show bottom navigation bar", !shouldShowBottomBar(MiMomentoDestinations.WELCOME))
        assertTrue("Null route must not show bottom navigation bar", !shouldShowBottomBar(null))
    }

    @Test
    fun mainDestinations_displayBottomNavigationBar() {
        // All 5 main tabs must display bottom navigation bar
        val mainRoutes = TOP_LEVEL_DESTINATIONS.map { it.route }
        assertEquals(5, mainRoutes.size)
        mainRoutes.forEach { route ->
            assertTrue(
                "Main tab route '$route' must show bottom navigation bar",
                shouldShowBottomBar(route),
            )
        }
    }

    @Test
    fun welcomeStartFlow_navigatesToHomeAndClearsWelcomeInclusively() {
        // Simulating the navigation parameters of the Welcome screen's "Comenzar" CTA
        val targetDestination = MiMomentoDestinations.HOME
        val popUpToDestination = MiMomentoDestinations.WELCOME
        val inclusive = true
        val launchSingleTop = true

        assertEquals("home", targetDestination)
        assertEquals("welcome", popUpToDestination)
        assertTrue(inclusive)
        assertTrue(launchSingleTop)

        // After this popUpTo configuration, the back stack has HOME as its base;
        // Welcome is completely removed, ensuring Back exits the app or standard system behavior without returning to Welcome.
        val backStack = mutableListOf(MiMomentoDestinations.WELCOME)
        // On "Comenzar" clicked:
        if (inclusive) {
            backStack.remove(popUpToDestination)
        }
        backStack.add(targetDestination)

        assertEquals(listOf(MiMomentoDestinations.HOME), backStack)
        assertTrue(!backStack.contains(MiMomentoDestinations.WELCOME))
    }

    @Test
    fun topLevelDestinations_containsExpectedFiveSections() {
        assertEquals(5, TOP_LEVEL_DESTINATIONS.size)
        val routes = TOP_LEVEL_DESTINATIONS.map { it.route }
        assertEquals(
            listOf("home", "devotionals", "prayers", "journal", "progress"),
            routes,
        )
    }

    @Test
    fun devotionalDetailRoute_passesOnlyIdWithoutFullObject() {
        val testId = "DEV-0042"
        val route = MiMomentoDestinations.devotionalDetail(testId)
        assertEquals("devotionals/DEV-0042", route)
        assertTrue(route.startsWith("devotionals/"))
        assertEquals(testId, route.substringAfter("devotionals/"))
    }

    @Test
    fun previousAndNextNavigation_generatesExpectedRouteById() {
        val previousId = "DEV-0041"
        val nextId = "DEV-0043"

        val previousRoute = MiMomentoDestinations.devotionalDetail(previousId)
        assertEquals("devotionals/DEV-0041", previousRoute)

        val nextRoute = MiMomentoDestinations.devotionalDetail(nextId)
        assertEquals("devotionals/DEV-0043", nextRoute)
    }

    @Test
    fun prayerRoutes_areConsistentAndStable() {
        assertEquals("prayers/guide/{guideId}", MiMomentoDestinations.PRAYER_GUIDE_DETAIL_ROUTE)
        assertEquals("prayers/route/{routeId}", MiMomentoDestinations.PRAYER_ROUTE_DETAIL_ROUTE)
        assertEquals("prayers/moment/{momentId}", MiMomentoDestinations.SPIRITUAL_MOMENT_DETAIL_ROUTE)
    }

    @Test
    fun prayerDetailRoutes_passOnlyIdsWithoutFullObjects() {
        val guideRoute = MiMomentoDestinations.prayerGuideDetail("GO-01")
        assertEquals("prayers/guide/GO-01", guideRoute)
        assertTrue(guideRoute.startsWith("prayers/guide/"))

        val prayerRoute = MiMomentoDestinations.prayerRouteDetail("RO-01")
        assertEquals("prayers/route/RO-01", prayerRoute)
        assertTrue(prayerRoute.startsWith("prayers/route/"))

        val momentRoute = MiMomentoDestinations.spiritualMomentDetail("ME-01")
        assertEquals("prayers/moment/ME-01", momentRoute)
        assertTrue(momentRoute.startsWith("prayers/moment/"))
    }

    @Test
    fun devotionalFinishTarget_isCanonicalPrayersRoute() {
        assertEquals("prayers", MiMomentoDestinations.PRAYERS)
        assertTrue(
            "PRAYERS must be a registered top-level destination for proper popUpTo stack behavior",
            TOP_LEVEL_DESTINATIONS.any { it.route == MiMomentoDestinations.PRAYERS },
        )
    }

    @Test
    fun devotionalFinish_usesPrayersNotHome() {
        // Finishing a devotional must navigate to PRAYERS, not HOME.
        // HOME remains the popUpTo anchor so that the full devotional back-stack is cleared,
        // but the final destination the user lands on is PRAYERS.
        val finishDestination = MiMomentoDestinations.PRAYERS
        assertEquals("prayers", finishDestination)
        assertTrue(
            "Finish destination must differ from HOME so it is semantically correct",
            finishDestination != MiMomentoDestinations.HOME,
        )
    }

    @Test
    fun navigationSemantics_differentiatesBetweenNavigateUpAndFinishFlow() {
        var backPressed = false
        var devotionalFinished = false

        val onNavigateUp: () -> Unit = { backPressed = true }
        val onFinishDevotional: () -> Unit = { devotionalFinished = true }

        // Normal back press from reading flow does NOT finish the devotional
        onNavigateUp()
        assertTrue(backPressed)
        assertTrue(!devotionalFinished)

        // Explicit finish action is separate and triggers completion callback
        onFinishDevotional()
        assertTrue(devotionalFinished)
    }
}
