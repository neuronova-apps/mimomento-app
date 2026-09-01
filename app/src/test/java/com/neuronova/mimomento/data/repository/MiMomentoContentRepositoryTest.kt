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
}
