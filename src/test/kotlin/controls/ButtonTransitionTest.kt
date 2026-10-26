package com.ykononenko.controls

import androidx.compose.ui.geometry.Offset
import com.ykononenko.FakeImageFileService
import com.ykononenko.awaitingPoint1Session
import com.ykononenko.photoLoadedSession
import com.ykononenko.press
import com.ykononenko.sessionWithNorth
import com.ykononenko.sessionWithPoints
import com.ykononenko.sessionWithRuler
import com.ykononenko.testImage
import com.ykononenko.controls.buttons.BatchButton
import com.ykononenko.controls.buttons.BuildRulerButton
import com.ykononenko.controls.buttons.ConfirmAddNorthButton
import com.ykononenko.controls.buttons.MoveToCornerButton
import com.ykononenko.controls.buttons.SaveImageButton
import com.ykononenko.controls.buttons.SavePointButton
import com.ykononenko.controls.buttons.StartPointPlacementButton
import com.ykononenko.controls.buttons.ToggleOrientationButton
import com.ykononenko.model.Corner
import com.ykononenko.state.AppState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ButtonTransitionTest {

    @Test
    fun loadPhotoButtonTransitionsWhenImageOpened() {
        val service = FakeImageFileService()
        val session = EditorSession(service)
        val image = testImage()
        service.imageToOpen = image

        session.press("LoadPhotoButton")

        assertEquals(AppState.PHOTO_LOADED, session.appState)
        assertTrue(session.image === image)
    }

    @Test
    fun loadPhotoButtonKeepsStateWhenOpenCancelled() {
        val service = FakeImageFileService(imageToOpen = null)
        val session = EditorSession(service)

        session.press("LoadPhotoButton")

        assertEquals(AppState.IDLE, session.appState)
        assertNull(session.image)
    }

    @Test
    fun startPointPlacementEntersAwaitingFirstPoint() {
        val session = photoLoadedSession()

        StartPointPlacementButton().action(session)

        assertEquals(AppState.AWAITING_POINT_1, session.appState)
    }

    @Test
    fun startPointPlacementDoesNothingWithoutImage() {
        val session = EditorSession(FakeImageFileService())

        StartPointPlacementButton().action(session)

        assertEquals(AppState.IDLE, session.appState)
        assertNull(session.image)
    }

    @Test
    fun saveFirstPointRequiresPendingPoint() {
        val session = awaitingPoint1Session()

        SavePointButton(1).action(session)

        assertEquals(AppState.AWAITING_POINT_1, session.appState)
        assertNull(session.points.point1)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun saveFirstPointCommitsAndAdvances() {
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(11f, 22f))

        SavePointButton(1).action(session)

        assertEquals(AppState.AWAITING_POINT_2, session.appState)
        assertEquals(Offset(11f, 22f), session.points.point1)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun saveSecondPointRequiresPendingPoint() {
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(1f, 1f))
        SavePointButton(1).action(session)
        assertEquals(AppState.AWAITING_POINT_2, session.appState)

        SavePointButton(2).action(session)

        assertEquals(AppState.AWAITING_POINT_2, session.appState)
        assertNull(session.points.point2)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun saveSecondPointCommitsAndCompletesPoints() {
        val session = awaitingPoint1Session()
        session.setPendingPoint(Offset(10f, 20f))
        SavePointButton(1).action(session)
        session.setPendingPoint(Offset(80f, 70f))

        SavePointButton(2).action(session)

        assertEquals(AppState.POINTS_SET, session.appState)
        assertEquals(Offset(80f, 70f), session.points.point2)
        assertNull(session.points.pendingPoint)
    }

    @Test
    fun buildRulerButtonTransitionsOnSuccess() {
        val session = sessionWithPoints()
        session.updateMmText("50")

        BuildRulerButton().action(session)

        assertEquals(AppState.RULER_PLACED, session.appState)
        assertNotNull(session.ruler)
    }

    @Test
    fun buildRulerButtonStaysOnFailure() {
        val session = sessionWithPoints()

        BuildRulerButton().action(session)

        assertEquals(AppState.POINTS_SET, session.appState)
        assertNull(session.ruler)
    }

    @Test
    fun saveImageButtonCallsServiceAndKeepsState() {
        val service = FakeImageFileService()
        val session = sessionWithRuler(service)
        val state = session.appState
        val ruler = session.ruler

        SaveImageButton().action(session)

        assertEquals(1, service.saveCalls)
        assertEquals(ruler, service.savedRuler)
        assertEquals(state, session.appState)
    }

    @Test
    fun saveImageButtonKeepsStateWhenCancelled() {
        val service = FakeImageFileService(saveResult = false)
        val session = sessionWithRuler(service)
        val state = session.appState

        SaveImageButton().action(session)

        assertEquals(1, service.saveCalls)
        assertEquals(state, session.appState)
    }

    @Test
    fun saveImageButtonFromNorthPlacedSavesArrowAndKeepsState() {
        val service = FakeImageFileService()
        val session = sessionWithNorth(service)
        val state = session.appState
        val arrow = session.northArrow

        SaveImageButton().action(session)

        assertEquals(1, service.saveCalls)
        assertEquals(arrow, service.savedNorthArrow)
        assertNull(service.savedRuler)
        assertEquals(state, session.appState)
    }

    @Test
    fun addNorthButtonOpensWarningFromPhotoLoaded() {
        val session = photoLoadedSession()
        val state = session.appState

        session.press("AddNorthButton")

        assertTrue(session.showNorthWarning)
        assertEquals(state, session.appState)
    }

    @Test
    fun addNorthButtonOpensWarningFromRulerPlaced() {
        val session = sessionWithRuler()
        val state = session.appState

        session.press("AddNorthButton")

        assertTrue(session.showNorthWarning)
        assertEquals(state, session.appState)
    }

    @Test
    fun confirmAddNorthFromPhotoLoadedCreatesArrow() {
        val session = photoLoadedSession()
        session.requestAddNorth()
        assertTrue(session.showNorthWarning)

        ConfirmAddNorthButton().action(session)

        assertEquals(AppState.NORTH_PLACED, session.appState)
        assertNotNull(session.northArrow)
        assertNull(session.ruler)
        assertFalse(session.showNorthWarning)
    }

    @Test
    fun confirmAddNorthFromRulerPlacedKeepsRuler() {
        val session = sessionWithRuler()
        val ruler = session.ruler
        session.requestAddNorth()

        ConfirmAddNorthButton().action(session)

        assertEquals(AppState.NORTH_PLACED, session.appState)
        assertEquals(ruler, session.ruler)
        assertNotNull(session.northArrow)
        assertFalse(session.showNorthWarning)
    }

    @Test
    fun toggleOrientationButtonChangesOrientationAndKeepsState() {
        val session = sessionWithRuler()
        val before = assertNotNull(session.ruler).orientation

        ToggleOrientationButton().action(session)

        val after = assertNotNull(session.ruler).orientation
        assertTrue(before != after)
        assertEquals(AppState.RULER_PLACED, session.appState)
    }

    @Test
    fun moveToCornerButtonMovesRulerWhenNoNorthArrow() {
        val session = sessionWithRuler()
        val before = assertNotNull(session.ruler)

        MoveToCornerButton(Corner.TOP_LEFT).action(session)

        val after = assertNotNull(session.ruler)
        assertTrue(before.x != after.x || before.y != after.y)
        assertEquals(AppState.RULER_PLACED, session.appState)
        assertNull(session.northArrow)
    }

    @Test
    fun moveToCornerButtonMovesNorthArrowWhenPresent() {
        val session = sessionWithNorth()
        val before = assertNotNull(session.northArrow)

        MoveToCornerButton(Corner.TOP_LEFT).action(session)

        val after = assertNotNull(session.northArrow)
        assertTrue(before.centerX != after.centerX || before.centerY != after.centerY)
        assertEquals(AppState.NORTH_PLACED, session.appState)
        assertNull(session.ruler)
    }

    @Test
    fun batchButtonOpensDialogAndKeepsState() {
        val session = sessionWithRuler()
        val state = session.appState

        BatchButton().action(session)

        assertTrue(session.showBatchDialog)
        assertEquals(state, session.appState)
    }
}
