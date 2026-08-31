package com.neuronova.mimomento

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectFoundationTest {
    @Test
    fun applicationId_matchesNeuronovaConvention() {
        assertEquals("com.neuronova.mimomento", BuildConfig.APPLICATION_ID)
    }
}
