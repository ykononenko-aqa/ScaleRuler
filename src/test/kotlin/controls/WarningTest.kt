package com.ykononenko.controls

import com.ykononenko.photoLoadedSession
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WarningTest {

    @Test
    fun northArrowWarningIsHiddenUntilTriggered() {
        val session = photoLoadedSession()

        assertFalse(NorthArrowWarning.shouldShow(session))
    }

    @Test
    fun northArrowWarningIsShownWhenTriggeredAndWarningsEnabled() {
        val session = photoLoadedSession()
        session.requestAddNorth()

        assertTrue(NorthArrowWarning.shouldShow(session))
    }

    @Test
    fun northArrowWarningIsHiddenWhenWarningsSuppressed() {
        val session = photoLoadedSession()
        session.requestAddNorth()
        session.updateSuppressWarnings(true)

        assertFalse(NorthArrowWarning.shouldShow(session))
    }
}
