package com.ykononenko

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ykononenko.controls.ButtonControl
import com.ykononenko.controls.Control
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.ImageFileService
import com.ykononenko.controls.buttons.BuildRulerButton
import com.ykononenko.controls.buttons.ConfirmAddNorthButton
import com.ykononenko.controls.buttons.SavePointButton
import com.ykononenko.controls.buttons.StartPointPlacementButton
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Surface

fun testImage(width: Int = 200, height: Int = 100): ImageBitmap {
    val surface = Surface.makeRasterN32Premul(width, height)
    return try {
        surface.makeImageSnapshot().toComposeImageBitmap()
    } finally {
        surface.close()
    }
}

class FakeImageFileService(
    var imageToOpen: ImageBitmap? = null,
    var saveResult: Boolean = true
) : ImageFileService {

    var saveCalls: Int = 0
        private set

    var savedImage: ImageBitmap? = null
        private set

    var savedRuler: RulerData? = null
        private set

    var savedNorthArrow: NorthArrowData? = null
        private set

    var savedUnit: LengthUnit? = null
        private set

    override fun openImage(): ImageBitmap? = imageToOpen

    override fun saveImage(
        image: ImageBitmap,
        ruler: RulerData?,
        northArrow: NorthArrowData?,
        unit: LengthUnit
    ): Boolean {
        saveCalls += 1
        savedImage = image
        savedRuler = ruler
        savedNorthArrow = northArrow
        savedUnit = unit
        return saveResult
    }
}

fun EditorSession.controlByName(name: String): Control =
    bottomBar.controls.firstOrNull { it::class.simpleName == name }
        ?: error("Control $name is not present in the bottom bar model")

fun EditorSession.press(name: String) {
    val candidates = bottomBar.controls.filter {
        it::class.simpleName == name && it.isVisible(this)
    }
    val control = candidates.firstOrNull() ?: controlByName(name)
    (control as ButtonControl).action(this)
}

fun photoLoadedSession(
    service: FakeImageFileService = FakeImageFileService()
): EditorSession {
    val session = EditorSession(service)
    service.imageToOpen = testImage()
    session.press("LoadPhotoButton")
    return session
}

fun awaitingPoint1Session(
    service: FakeImageFileService = FakeImageFileService()
): EditorSession {
    val session = photoLoadedSession(service)
    StartPointPlacementButton().action(session)
    return session
}

fun sessionWithPoints(
    service: FakeImageFileService = FakeImageFileService()
): EditorSession {
    val session = awaitingPoint1Session(service)
    session.setPendingPoint(Offset(20f, 30f))
    SavePointButton(1).action(session)
    session.setPendingPoint(Offset(120f, 90f))
    SavePointButton(2).action(session)
    return session
}

fun sessionWithRuler(
    service: FakeImageFileService = FakeImageFileService()
): EditorSession {
    val session = sessionWithPoints(service)
    session.updateMmText("50")
    BuildRulerButton().action(session)
    return session
}

fun sessionWithNorth(
    service: FakeImageFileService = FakeImageFileService()
): EditorSession {
    val session = photoLoadedSession(service)
    session.requestAddNorth()
    ConfirmAddNorthButton().action(session)
    return session
}

