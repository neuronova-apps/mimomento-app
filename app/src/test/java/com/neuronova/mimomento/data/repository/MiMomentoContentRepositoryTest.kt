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
}
