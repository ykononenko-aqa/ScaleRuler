package com.ykononenko.controls

import com.ykononenko.measuringSession
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

    @Test
    fun measurementModeWarningIsHiddenUntilTriggered() {
        val session = com.ykononenko.sessionWithRuler()

        assertFalse(MeasurementModeWarning.shouldShow(session))

        session.requestMeasurementMode()

        assertTrue(MeasurementModeWarning.shouldShow(session))
    }

    @Test
    fun measurementModeWarningIsHiddenWhenWarningsSuppressed() {
        val session = com.ykononenko.sessionWithRuler()
        session.requestMeasurementMode()
        session.updateSuppressWarnings(true)

        assertFalse(MeasurementModeWarning.shouldShow(session))
    }

    @Test
    fun measurementDeleteWarningIsShownAndCanBeSuppressed() {
        val session = measuringSession()
        session.setMeasurementPoint(com.ykononenko.model.Point(1f, 2f))
        session.requestDeleteMeasurement()

        assertTrue(MeasurementDeleteWarning.shouldShow(session))

        session.updateSuppressWarnings(true)

        assertFalse(MeasurementDeleteWarning.shouldShow(session))
    }
}
