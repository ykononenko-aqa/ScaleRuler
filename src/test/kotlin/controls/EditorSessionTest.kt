package com.ykononenko.controls

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.ykononenko.FakeImageFileService
import com.ykononenko.awaitingPoint1Session
import com.ykononenko.photoLoadedSession
import com.ykononenko.press
import com.ykononenko.sessionWithNorth
import com.ykononenko.sessionWithPoints
import com.ykononenko.sessionWithRuler
import com.ykononenko.state.AppState
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.NorthArrowStyle
import com.ykononenko.model.RulerStyle
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EditorSessionTest {

    @Test
    fun initialStateHasDefaults() {
        val session = EditorSession(FakeImageFileService())

        assertEquals(AppState.IDLE, session.appState)
        assertNull(session.image)
        assertNull(session.ruler)
        assertNull(session.northArrow)
        assertEquals(LengthUnit.MM, session.unit)
        assertEquals("5", session.cellCountText)
        assertEquals("1", session.scaleText)
        assertEquals("", session.mmText)
        assertNull(session.points.pendingPoint)
        assertNull(session.points.point1)
        assertNull(session.points.point2)
        assertFalse(session.showNorthWarning)
        assertFalse(session.showBatchDialog)
    }

    @Test
    fun loadPhotoTransitionsAndResetsFields() {
        val service = FakeImageFileService()
        val session = EditorSession(service)
        session.updateMmText("12")
        session.updateCellCountText("8")
        session.updateScaleText("2")

        val image = com.ykononenko.testImage()
        service.imageToOpen = image
        session.press("LoadPhotoButton")

        assertEquals(AppState.PHOTO_LOADED, session.appState)
        assertTrue(session.image === image)
        assertNull(session.ruler)
        assertNull(session.northArrow)
        assertEquals("", session.mmText)
        assertEquals("5", session.cellCountText)
        assertEquals("1", session.scaleText)
    }

    @Test
    fun loadPhotoCancelledLeavesStateAndImageUnchanged() {
        val service = FakeImageFileService(imageToOpen = null)
        val session = EditorSession(service)

        session.press("LoadPhotoButton")

        assertEquals(AppState.IDLE, session.appState)
        assertNull(session.image)
    }

    @Test
    fun resetForNewPhotoClearsEditorDataButKeepsState() {
        val session = sessionWithRuler()
        assertEquals(AppState.RULER_PLACED, session.appState)
        session.selectUnit(LengthUnit.CM)
        session.setPendingPoint(Offset(1f, 2f))
        session.updateMmText("77")
        session.updateCellCountText("9")
        session.updateScaleText("3")

        val newImage = com.ykononenko.testImage(300, 150)
        session.resetForNewPhoto(newImage)

        assertTrue(session.image === newImage)
        assertEquals(AppState.RULER_PLACED, session.appState)
        assertNull(session.ruler)
        assertNull(session.northArrow)
        assertNull(session.points.pendingPoint)
        assertNull(session.points.point1)
        assertNull(session.points.point2)
        assertEquals("", session.mmText)
        assertEquals("5", session.cellCountText)
        assertEquals("1", session.scaleText)
        assertEquals(LengthUnit.MM, session.unit)
    }

    @Test
    fun setPendingPointStoresImageCoordinates() {
        val session = awaitingPoint1Session()

        session.setPendingPoint(Offset(42f, 24f))

        assertEquals(Offset(42f, 24f), session.points.pendingPoint)
    }

    @Test
    fun commitPendingPointRequiresPendingPoint() {
        val session = awaitingPoint1Session()

        assertFalse(session.commitPendingPoint(1))
        assertNull(session.points.point1)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun commitPendingPointCommitsFirstPoint() {
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(10f, 20f))

        assertTrue(session.commitPendingPoint(1))

        assertEquals(Offset(10f, 20f), session.points.point1)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun commitPendingPointCommitsSecondPoint() {
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(10f, 20f))
        assertTrue(session.commitPendingPoint(1))
        session.setPendingPoint(Offset(80f, 70f))

        assertTrue(session.commitPendingPoint(2))

        assertEquals(Offset(80f, 70f), session.points.point2)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun buildRulerFromInputsCreatesRulerAndClearsPoints() {
        val session = sessionWithPoints()
        assertEquals(AppState.POINTS_SET, session.appState)
        session.updateMmText("50")

        assertTrue(session.buildRulerFromInputs())

        assertNotNull(session.ruler)
        assertEquals(AppState.POINTS_SET, session.appState)
        assertNull(session.points.pendingPoint)
        assertNull(session.points.point1)
        assertNull(session.points.point2)
    }

    @Test
    fun buildRulerFromInputsRequiresPoints() {
        val session = photoLoadedSession()
        session.updateMmText("50")

        assertFalse(session.buildRulerFromInputs())

        assertNull(session.ruler)
    }

    @Test
    fun buildRulerFromInputsRejectsInvalidValuesAndKeepsPoints() {
        val session = sessionWithPoints()

        session.updateMmText("0")
        assertFalse(session.buildRulerFromInputs())
        assertNull(session.ruler)
        assertNotNull(session.points.point1)

        session.updateMmText("50")
        session.updateCellCountText("0")
        assertFalse(session.buildRulerFromInputs())
        assertNull(session.ruler)
        assertNotNull(session.points.point1)

        session.updateCellCountText("5")
        session.updateScaleText("0")
        assertFalse(session.buildRulerFromInputs())
        assertNull(session.ruler)
        assertNotNull(session.points.point1)

        session.updateScaleText("1")
        session.updateMmText("")
        assertFalse(session.buildRulerFromInputs())
        assertNull(session.ruler)
        assertNotNull(session.points.point1)
    }

    @Test
    fun createNorthArrowHidesWarningAndKeepsState() {
        val session = photoLoadedSession()
        session.requestAddNorth()
        assertTrue(session.showNorthWarning)

        assertTrue(session.createNorthArrow())

        assertNotNull(session.northArrow)
        assertFalse(session.showNorthWarning)
        assertEquals(AppState.PHOTO_LOADED, session.appState)
        assertNull(session.ruler)
    }

    @Test
    fun createNorthArrowKeepsExistingRuler() {
        val session = sessionWithRuler()
        val ruler = assertNotNull(session.ruler)

        assertTrue(session.createNorthArrow())

        assertNotNull(session.northArrow)
        assertEquals(ruler, session.ruler)
        assertEquals(AppState.RULER_PLACED, session.appState)
    }

    @Test
    fun saveImageCallsServiceOnceAndKeepsState() {
        val service = FakeImageFileService()
        val session = sessionWithRuler(service)
        val state = session.appState
        val ruler = session.ruler

        assertTrue(session.saveImage())

        assertEquals(1, service.saveCalls)
        assertTrue(service.savedImage === session.image)
        assertEquals(ruler, service.savedRuler)
        assertNull(service.savedNorthArrow)
        assertEquals(session.unit, service.savedUnit)
        assertEquals(state, session.appState)
    }

    @Test
    fun saveImageCancelledKeepsState() {
        val service = FakeImageFileService(saveResult = false)
        val session = sessionWithRuler(service)
        val state = session.appState

        assertFalse(session.saveImage())

        assertEquals(1, service.saveCalls)
        assertEquals(state, session.appState)
    }

    @Test
    fun moveRulerByChangesCoordinatesAndKeepsState() {
        val session = sessionWithRuler()
        val before = assertNotNull(session.ruler)
        val state = session.appState

        session.moveRulerBy(Offset(5f, 7f))

        val after = assertNotNull(session.ruler)
        assertEquals(before.x + 5f, after.x)
        assertEquals(before.y + 7f, after.y)
        assertEquals(state, session.appState)
    }

    @Test
    fun moveNorthByChangesCoordinatesAndKeepsState() {
        val session = sessionWithNorth()
        val before = assertNotNull(session.northArrow)
        val state = session.appState

        session.moveNorthBy(Offset(6f, -4f))

        val after = assertNotNull(session.northArrow)
        assertEquals(before.centerX + 6f, after.centerX)
        assertEquals(before.centerY - 4f, after.centerY)
        assertEquals(state, session.appState)
    }

    @Test
    fun rotateNorthChangesAngleAndKeepsState() {
        val session = sessionWithNorth()
        val before = assertNotNull(session.northArrow)
        val state = session.appState

        session.rotateNorth(0.5f)

        val after = assertNotNull(session.northArrow)
        assertTrue(abs(after.angleRad - before.angleRad) > 1e-5f)
        assertEquals(state, session.appState)
    }

    @Test
    fun moveSelectionToCornerMovesNorthArrowWhenPresent() {
        val session = sessionWithNorth()
        val before = assertNotNull(session.northArrow)

        session.moveSelectionToCorner(Corner.TOP_LEFT)

        val after = assertNotNull(session.northArrow)
        assertTrue(before.centerX != after.centerX || before.centerY != after.centerY)
        assertEquals(AppState.NORTH_PLACED, session.appState)
        assertNull(session.ruler)
    }

    @Test
    fun moveSelectionToCornerMovesRulerWhenNoNorthArrow() {
        val session = sessionWithRuler()
        val before = assertNotNull(session.ruler)

        session.moveSelectionToCorner(Corner.TOP_LEFT)

        val after = assertNotNull(session.ruler)
        assertTrue(before.x != after.x || before.y != after.y)
        assertEquals(AppState.RULER_PLACED, session.appState)
        assertNull(session.northArrow)
    }

    @Test
    fun toggleRulerOrientationChangesOrientationAndKeepsState() {
        val session = sessionWithRuler()
        val before = assertNotNull(session.ruler).orientation

        session.toggleRulerOrientation()

        val after = assertNotNull(session.ruler).orientation
        assertTrue(before != after)
        assertEquals(AppState.RULER_PLACED, session.appState)
    }

    @Test
    fun selectUnitUpdatesUnit() {
        val session = sessionWithRuler()

        session.selectUnit(LengthUnit.CM)

        assertEquals(LengthUnit.CM, session.unit)
    }

    @Test
    fun setRulerStyleUpdatesRulerStyle() {
        val session = sessionWithRuler()

        session.setRulerStyle(RulerStyle.BAR_BLACK)

        assertEquals(RulerStyle.BAR_BLACK, assertNotNull(session.ruler).style)
    }

    @Test
    fun selectNorthStyleUpdatesNorthStyle() {
        val session = sessionWithNorth()

        session.selectNorthStyle(NorthArrowStyle.WHITE)

        assertEquals(NorthArrowStyle.WHITE, assertNotNull(session.northArrow).style)
    }

    @Test
    fun setNorthRadiusUpdatesRadiusFraction() {
        val session = sessionWithNorth()

        session.setNorthRadius(0.25f)

        assertEquals(0.25f, assertNotNull(session.northArrow).radiusFraction)
    }

    @Test
    fun textAndCanvasInputsAreStored() {
        val session = sessionWithRuler()

        session.updateMmText("42")
        session.updateCellCountText("6")
        session.updateScaleText("2.5")
        session.updateCanvasSize(Size(640f, 480f))

        assertEquals("42", session.mmText)
        assertEquals("6", session.cellCountText)
        assertEquals("2.5", session.scaleText)
        assertEquals(Size(640f, 480f), session.canvasSize)
    }

    @Test
    fun requestAndDismissNorthWarning() {
        val session = photoLoadedSession()
        val state = session.appState

        session.requestAddNorth()
        assertTrue(session.showNorthWarning)
        assertEquals(state, session.appState)

        session.dismissNorthWarning()
        assertFalse(session.showNorthWarning)
        assertEquals(state, session.appState)
    }

    @Test
    fun requestAndDismissBatchDialog() {
        val session = photoLoadedSession()
        val state = session.appState

        session.requestBatch()
        assertTrue(session.showBatchDialog)
        assertEquals(state, session.appState)

        session.dismissBatch()
        assertFalse(session.showBatchDialog)
        assertEquals(state, session.appState)
    }
}
