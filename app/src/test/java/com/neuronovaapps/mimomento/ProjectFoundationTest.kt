package com.neuronovaapps.mimomento

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectFoundationTest {
    @Test
    fun applicationId_matchesNeuronovaConvention() {
        assertEquals("com.neuronovaapps.mimomento", BuildConfig.APPLICATION_ID)
    }
}
