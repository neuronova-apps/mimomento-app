package com.neuronova.mimomento.data.repository

import com.neuronova.mimomento.data.TestContentFixture
import com.neuronova.mimomento.data.local.MiMomentoContentSource
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MiMomentoContentRepositoryTest {
    @Test
    fun exposesTheRequiredQueriesAndLoadsOnlyOnce() {
        val loadCount = AtomicInteger(0)
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource {
                loadCount.incrementAndGet()
                TestContentFixture.loaded
            },
        )

        assertEquals(360, repository.getAllDevotionals().size)
        assertEquals("DEV-0001", repository.getDevotionalById("DEV-0001")?.id)
        assertNull(repository.getDevotionalById("DEV-9999"))
        assertEquals(12, repository.getCategories().size)
        assertEquals(12, repository.getSituations().size)
        assertEquals(48, repository.getSubthemes().size)
        assertEquals(212, repository.getTags().size)
        assertEquals(1, loadCount.get())
    }

    @Test
    fun firstDevotional_hasNoPreviousAndHasNextDev0002() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        assertNull(repository.getPreviousDevotionalId("DEV-0001"))
        assertEquals("DEV-0002", repository.getNextDevotionalId("DEV-0001"))
    }

    @Test
    fun lastDevotional_hasPreviousDev0359AndHasNoNext() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        assertEquals("DEV-0359", repository.getPreviousDevotionalId("DEV-0360"))
        assertNull(repository.getNextDevotionalId("DEV-0360"))
    }

    @Test
    fun intermediateDevotional_hasCorrectPreviousAndNext() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        assertEquals("DEV-0041", repository.getPreviousDevotionalId("DEV-0042"))
        assertEquals("DEV-0043", repository.getNextDevotionalId("DEV-0042"))

        assertEquals("DEV-0179", repository.getPreviousDevotionalId("DEV-0180"))
        assertEquals("DEV-0181", repository.getNextDevotionalId("DEV-0180"))
    }

    @Test
    fun nonExistentDevotional_returnsNullForPreviousAndNextSafely() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        assertNull(repository.getPreviousDevotionalId("DEV-INVALID-9999"))
        assertNull(repository.getNextDevotionalId("DEV-INVALID-9999"))
    }

    @Test
    fun spiritualMoments_loadedAndAccessible() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val moments = repository.getSpiritualMoments()
        assertEquals(8, moments.size)
        val me01 = repository.getSpiritualMomentById("ME-01")
        assertEquals("ME-01", me01?.id)
        assertEquals("Al despertar", me01?.label)
        assertNull(repository.getSpiritualMomentById("ME-9999"))
    }

    @Test
    fun prayerRoutes_loadedAndAccessible() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val routes = repository.getPrayerRoutes()
        assertEquals(4, routes.size)
        val ro01 = repository.getPrayerRouteById("RO-01")
        assertEquals("RO-01", ro01?.id)
        assertEquals("Oración breve para comenzar", ro01?.name)
        assertEquals(listOf("GO-01", "GO-02", "GO-03"), ro01?.guideIds)
        assertNull(repository.getPrayerRouteById("RO-9999"))
    }

    @Test
    fun prayerGuides_loadedAndAccessible() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val guides = repository.getPrayerGuides()
        assertEquals(9, guides.size)
        val go01 = repository.getPrayerGuideById("GO-01")
        assertEquals("GO-01", go01?.id)
        assertEquals("Disposición", go01?.name)
        assertNull(repository.getPrayerGuideById("GO-9999"))
    }

    @Test
    fun specialContexts_loadedAndAccessible() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val contexts = repository.getSpecialContexts()
        assertEquals(5, contexts.size)
        val ctx02 = repository.getSpecialContextById("CTX-02")
        assertEquals("CTX-02", ctx02?.id)
        assertEquals("Tiempo de espera", ctx02?.label)
        assertNull(repository.getSpecialContextById("CTX-9999"))
    }

    @Test
    fun getSuggestedDevotional_delegatesCorrectly() {
        val repository = MiMomentoContentRepository(
            source = MiMomentoContentSource { TestContentFixture.loaded },
        )
        val dev = repository.getSuggestedDevotional(listOf("SIT-03"), listOf("CAT-07"))
        assertEquals("DEV-0002", dev?.id)
    }
}
