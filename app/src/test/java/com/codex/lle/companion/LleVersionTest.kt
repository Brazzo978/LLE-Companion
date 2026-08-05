package com.codex.lle.companion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LleVersionTest {
    @Test
    fun parsesFourNumericComponents() {
        assertEquals("1.0.5.3", LleVersion.parse("1.0.5.3").toString())
        assertEquals("1.0.5.3", LleVersion.parse("v1.0.5.3").toString())
    }

    @Test
    fun rejectsUnexpectedFormats() {
        assertNull(LleVersion.parse("1.0.5"))
        assertNull(LleVersion.parse("1.0.5.3-beta"))
        assertNull(LleVersion.parse("01.0.5.3"))
        assertNull(LleVersion.parse("hello"))
    }

    @Test
    fun comparesEachPartNumerically() {
        assertTrue(LleVersion.parse("1.0.5.10")!! > LleVersion.parse("1.0.5.3")!!)
        assertTrue(LleVersion.parse("2.0.0.0")!! > LleVersion.parse("1.99.99.99")!!)
        assertEquals(0, LleVersion.parse("1.0.5.3")!!.compareTo(LleVersion.parse("1.0.5.3")!!))
    }
}
