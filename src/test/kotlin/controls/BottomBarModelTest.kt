package com.ykononenko.controls

import androidx.compose.ui.geometry.Offset
import com.ykononenko.FakeImageFileService
import com.ykononenko.awaitingPoint1Session
import com.ykononenko.photoLoadedSession
import com.ykononenko.sessionWithNorth
import com.ykononenko.sessionWithPoints
import com.ykononenko.sessionWithRuler
import com.ykononenko.controls.buttons.SavePointButton
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
        val session = EditorSession(FakeImageFileService())

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

        session.setPendingPoint(Offset(5f, 5f))
        (saveControls.single() as ButtonControl).action(session)

        assertEquals(AppState.AWAITING_POINT_2, session.appState)
    }

    @Test
    fun awaitingPoint2ShowsSaveSecondPointButton() {
        val model = BottomBarModel.default()
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(5f, 5f))
        SavePointButton(1).action(session)
        assertEquals(AppState.AWAITING_POINT_2, session.appState)

        val saveControls = model.visibleRows(session).flatten()
            .filter { it::class.simpleName == "SavePointButton" }

        assertEquals(1, saveControls.size)

        session.setPendingPoint(Offset(15f, 15f))
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
        assertFalse(controls.containsNamed("LoadPhotoButton"))
    }
}
