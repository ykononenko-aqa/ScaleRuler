package com.ykononenko.controls

import com.ykononenko.FakeImageFileService
import com.ykononenko.FakeSettingsStore
import com.ykononenko.awaitingPoint1Session
import com.ykononenko.measuringSession
import com.ykononenko.photoLoadedSession
import com.ykononenko.sessionWithNorth
import com.ykononenko.sessionWithPoints
import com.ykononenko.sessionWithRuler
import com.ykononenko.sessionWithRulerAndNorth
import com.ykononenko.controls.buttons.SavePointButton
import com.ykononenko.model.Point
import com.ykononenko.state.AppState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private fun List<Control>.countNamed(name: String): Int =
    count { it::class.simpleName == name }

private fun List<Control>.containsNamed(name: String): Boolean =
    any { it::class.simpleName == name }

class BottomBarModelTest {

    @Test
    fun idleShowsOnlyLoadPhotoButton() {
        val model = BottomBarModel.default()
        val session = EditorSession(
            FakeImageFileService(),
            settingsStore = FakeSettingsStore()
        )

        val controls = model.visibleRows(session).flatten()

        assertEquals(1, controls.size)
        assertEquals("LoadPhotoButton", controls.single()::class.simpleName)
    }

    @Test
    fun photoLoadedShowsPointAndNorthButtonsInPrimaryRow() {
        val model = BottomBarModel.default()
        val session = photoLoadedSession()

        val rows = model.visibleRows(session)
        val controls = rows.flatten()

        assertTrue(controls.containsNamed("StartPointPlacementButton"))
        assertTrue(controls.containsNamed("AddNorthButton"))
        assertFalse(controls.containsNamed("LoadPhotoButton"))

        val startButton = controls.first { it::class.simpleName == "StartPointPlacementButton" }
        val addNorthButton = controls.first { it::class.simpleName == "AddNorthButton" }

        assertEquals(ControlRow.PRIMARY, startButton.row)
        assertEquals(ControlRow.PRIMARY, addNorthButton.row)

        val primaryRow = rows.first { it.contains(startButton) }
        assertTrue(primaryRow.contains(addNorthButton))
    }

    @Test
    fun awaitingPoint1ShowsSaveFirstPointButton() {
        val model = BottomBarModel.default()
        val session = awaitingPoint1Session()

        val saveControls = model.visibleRows(session).flatten()
            .filter { it::class.simpleName == "SavePointButton" }

        assertEquals(1, saveControls.size)

        session.setPendingPoint(Point(5f, 5f))
        (saveControls.single() as ButtonControl).action(session)

        assertEquals(AppState.AWAITING_POINT_2, session.appState)
    }

    @Test
    fun awaitingPoint2ShowsSaveSecondPointButton() {
        val model = BottomBarModel.default()
        val session = awaitingPoint1Session()
        session.setPendingPoint(Point(5f, 5f))
        SavePointButton(1).action(session)
        assertEquals(AppState.AWAITING_POINT_2, session.appState)

        val saveControls = model.visibleRows(session).flatten()
            .filter { it::class.simpleName == "SavePointButton" }

        assertEquals(1, saveControls.size)

        session.setPendingPoint(Point(15f, 15f))
        (saveControls.single() as ButtonControl).action(session)

        assertEquals(AppState.POINTS_SET, session.appState)
    }

    @Test
    fun pointsSetShowsFourSettingsControlsAndBuildRulerButton() {
        val model = BottomBarModel.default()
        val session = sessionWithPoints()

        val rows = model.visibleRows(session)
        val controls = rows.flatten()

        val settingsRows = rows.filter { row -> row.any { it.row == ControlRow.SETTINGS } }
        assertEquals(1, settingsRows.size)

        val settings = settingsRows.single()
        assertEquals(4, settings.size)
        assertTrue(settings.all { it.row == ControlRow.SETTINGS })

        assertEquals(1, controls.countNamed("BuildRulerButton"))
    }

    @Test
    fun rulerPlacedShowsRulerControls() {
        val model = BottomBarModel.default()
        val session = sessionWithRuler()

        val controls = model.visibleRows(session).flatten()

        assertEquals(4, controls.countNamed("MoveToCornerButton"))
        assertEquals(1, controls.countNamed("ToggleOrientationButton"))
        assertEquals(1, controls.countNamed("SelectControl"))
        assertEquals(1, controls.countNamed("BatchButton"))
        assertEquals(1, controls.countNamed("AddNorthButton"))
        assertEquals(1, controls.countNamed("SaveImageButton"))
        assertEquals(1, controls.countNamed("StartMeasurementButton"))
        assertFalse(controls.containsNamed("LoadPhotoButton"))
    }

    @Test
    fun northPlacedShowsNorthControls() {
        val model = BottomBarModel.default()
        val session = sessionWithNorth()

        val controls = model.visibleRows(session).flatten()

        assertEquals(4, controls.countNamed("MoveToCornerButton"))
        assertEquals(0, controls.countNamed("ToggleOrientationButton"))
        assertEquals(0, controls.countNamed("AddNorthButton"))
        assertEquals(1, controls.countNamed("SelectControl"))
        assertEquals(1, controls.countNamed("BatchButton"))
        assertEquals(1, controls.countNamed("SliderControl"))
        assertEquals(1, controls.countNamed("SaveImageButton"))
        assertEquals(0, controls.countNamed("StartMeasurementButton"))
        assertFalse(controls.containsNamed("LoadPhotoButton"))
    }

    @Test
    fun northPlacedWithRulerShowsStartMeasurementButton() {
        val model = BottomBarModel.default()
        val session = sessionWithRulerAndNorth()

        val controls = model.visibleRows(session).flatten()

        assertEquals(1, controls.countNamed("StartMeasurementButton"))
        assertEquals(0, controls.countNamed("AddNorthButton"))
        assertFalse(controls.containsNamed("LoadPhotoButton"))
    }

    @Test
    fun measuringShowsPointAndStyleControls() {
        val model = BottomBarModel.default()
        val session = measuringSession()

        val controls = model.visibleRows(session).flatten()

        assertEquals(0, controls.countNamed("StartMeasurementButton"))
        assertEquals(1, controls.countNamed("SaveMeasurementPointButton"))
        assertEquals(1, controls.countNamed("SliderControl"))
        assertEquals(1, controls.countNamed("SelectControl"))
        assertEquals(0, controls.countNamed("DeleteMeasurementButton"))
        assertEquals(1, controls.countNamed("BatchButton"))
        assertEquals(1, controls.countNamed("AddNorthButton"))
        assertEquals(1, controls.countNamed("SaveImageButton"))

        session.setMeasurementPoint(Point(3f, 4f))
        assertTrue(session.commitMeasurementPoint(1))

        val afterFirstPoint = model.visibleRows(session).flatten()
        assertEquals(1, afterFirstPoint.countNamed("SaveMeasurementPointButton"))
        assertEquals(1, afterFirstPoint.countNamed("DeleteMeasurementButton"))
    }

    @Test
    fun measuringWithNorthShowsBothColorSelectorsAndReturnButton() {
        val model = BottomBarModel.default()
        val northSession = measuringSession(withNorth = true)
        northSession.setMeasurementPoint(Point(1f, 2f))

        val controls = model.visibleRows(northSession).flatten()
        val addNorth = controls.first { it::class.simpleName == "AddNorthButton" }

        assertEquals(2, controls.countNamed("SelectControl"))
        assertTrue(controls.containsNamed("DeleteMeasurementButton"))
        assertEquals("Вернуться к указателю", (addNorth as ButtonControl).displayTitle(northSession))
    }
}
