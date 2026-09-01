package com.neuronova.mimomento.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MiMomentoNavigationTest {

    @Test
    fun routes_areConsistentAndStable() {
        assertEquals("home", MiMomentoDestinations.HOME)
        assertEquals("devotionals", MiMomentoDestinations.DEVOTIONALS)
        assertEquals("prayers", MiMomentoDestinations.PRAYERS)
        assertEquals("journal", MiMomentoDestinations.JOURNAL)
        assertEquals("progress", MiMomentoDestinations.PROGRESS)
        assertEquals("devotionals/{devotionalId}", MiMomentoDestinations.DEVOTIONAL_DETAIL_ROUTE)
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
}
