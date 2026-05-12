package com.ykononenko

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.logic.*
import com.ykononenko.model.AppPoints
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import com.ykononenko.state.AppState
import com.ykononenko.ui.*

@Composable
fun App(onExit: () -> Unit = {}) {
    var appState by remember { mutableStateOf(AppState.IDLE) }
    var mmText by remember { mutableStateOf("") }
    var loadedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var points by remember { mutableStateOf(AppPoints()) }
    var ruler by remember { mutableStateOf<RulerData?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var selectedUnit by remember { mutableStateOf(LengthUnit.MM) }
    var showBatchDialog by remember { mutableStateOf(false) }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                onLoadPhoto = {
                    loadImageFromFile()?.let { img ->
                        loadedImage = img
                        appState = AppState.PHOTO_LOADED
                        mmText = ""
                        selectedUnit = LengthUnit.MM  // ← сброс
                        points = AppPoints()
                        ruler = null
                    }
                },
                onExit = onExit
            )

            MainArea(
                appState = appState,
                image = loadedImage,
                points = points,
                ruler = ruler,
                onCanvasClick = { offset ->
                    if (appState == AppState.AWAITING_POINT_1 || appState == AppState.AWAITING_POINT_2) {
                        points = points.copy(tempMarker = offset)
                    }
                },
                onRulerDrag = { delta ->
                    val r = ruler ?: return@MainArea
                    val img = loadedImage ?: return@MainArea
                    val layout = calculateImageLayout(img, canvasSize)
                    ruler = r.copy(
                        x = r.x + delta.x / layout.scale,
                        y = r.y + delta.y / layout.scale
                    )
                },
                onCanvasSizeChanged = { canvasSize = it },
                modifier = Modifier.weight(1f)
            )

            BottomBar(
                appState = appState,
                mmText = mmText,
                onMmTextChange = { mmText = it },
                buttonText = buttonText(appState),
                selectedUnit = selectedUnit,
                onUnitChange = { selectedUnit = it },
                onButtonClick = {
                    when (appState) {
                        AppState.IDLE -> {
                            loadImageFromFile()?.let { img ->
                                loadedImage = img
                                appState = AppState.PHOTO_LOADED
                                mmText = ""
                                selectedUnit = LengthUnit.MM  // ← сброс
                                points = AppPoints()
                                ruler = null
                            }
                        }
                        AppState.AWAITING_POINT_1 -> {
                            val marker = points.tempMarker
                            val img = loadedImage
                            if (marker != null && img != null) {
                                val layout = calculateImageLayout(img, canvasSize)
                                points = points.copy(point1 = canvasToImage(marker, layout), tempMarker = null)
                                appState = AppState.AWAITING_POINT_2
                            }
                        }
                        AppState.AWAITING_POINT_2 -> {
                            val marker = points.tempMarker
                            val img = loadedImage
                            if (marker != null && img != null) {
                                val layout = calculateImageLayout(img, canvasSize)
                                points = points.copy(point2 = canvasToImage(marker, layout), tempMarker = null)
                                appState = AppState.POINTS_SET
                            }
                        }
                        AppState.POINTS_SET -> {
                            val mm = mmText.toFloatOrNull()
                            val p1 = points.point1
                            val p2 = points.point2
                            val img = loadedImage
                            if (mm != null && mm > 0 && p1 != null && p2 != null && img != null) {
                                val scalePxPerMm = distanceBetween(p1, p2) / mm
                                ruler = RulerFactory.create(img, scalePxPerMm)
                                points = AppPoints()
                                appState = AppState.RULER_PLACED
                            }
                        }
                        AppState.RULER_PLACED -> {
                            val img = loadedImage ?: return@BottomBar
                            saveImageWithDialog(img, ruler, selectedUnit)
                        }
                        else -> appState = nextState(appState)
                    }
                },
                onMoveToCorner = { corner ->
                    val img = loadedImage ?: return@BottomBar
                    val r = ruler ?: return@BottomBar
                    ruler = RulerFactory.moveToCorner(r, img, corner)
                },
                onBatchClick = { showBatchDialog = true },
            )
        }
        if (showBatchDialog && ruler != null) {
            BatchDialog(
                ruler = ruler!!,
                unit = selectedUnit,
                onDismiss = { showBatchDialog = false }
            )
        }
    }
}