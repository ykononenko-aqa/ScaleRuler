package com.ykononenko.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun decimalInputValidation() {
        assertTrue(isDecimalInput(""))
        assertTrue(isDecimalInput("1"))
        assertTrue(isDecimalInput("1.5"))
        assertTrue(isDecimalInput(".5"))
        assertTrue(isDecimalInput("."))
        assertFalse(isDecimalInput("1.2.3"))
        assertFalse(isDecimalInput("a"))
        assertFalse(isDecimalInput("-1"))
    }

    @Test
    fun integerInputValidation() {
        assertTrue(isIntegerInput(""))
        assertTrue(isIntegerInput("0"))
        assertTrue(isIntegerInput("12"))
        assertFalse(isIntegerInput("1.0"))
        assertFalse(isIntegerInput("a"))
    }

    @Test
    fun positiveFloatParsing() {
        assertEquals(1f, parsePositiveFloat("1"))
        assertEquals(1.5f, parsePositiveFloat("1.5"))
        assertNull(parsePositiveFloat("0"))
        assertNull(parsePositiveFloat("-1"))
        assertNull(parsePositiveFloat(""))
        assertNull(parsePositiveFloat("NaN"))
        assertNull(parsePositiveFloat("Infinity"))
    }

    @Test
    fun positiveIntParsing() {
        assertEquals(1, parsePositiveInt("1"))
        assertNull(parsePositiveInt("0"))
        assertNull(parsePositiveInt("-1"))
        assertNull(parsePositiveInt(""))
        assertNull(parsePositiveInt("1.5"))
    }

    @Test
    fun defaultConstants() {
        assertEquals(10f, IMAGE_MARGIN_PX)
        assertEquals("5", DEFAULT_CELL_COUNT_TEXT)
        assertEquals("1", DEFAULT_SCALE_TEXT)
    }
}
