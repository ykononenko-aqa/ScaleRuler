package com.ykononenko

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.logic.*
import com.ykononenko.model.AppPoints
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

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {

            TopBar(
                onLoadPhoto = {
                    loadImageFromFile()?.let { img ->
                        loadedImage = img
                        appState = AppState.PHOTO_LOADED
                        mmText = ""
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
                message = statusMessage(appState),
                modifier = Modifier.weight(1f)
            )

            BottomBar(
                appState = appState,
                mmText = mmText,
                onMmTextChange = { mmText = it },
                buttonText = buttonText(appState),
                onButtonClick = {
                    when (appState) {
                        AppState.IDLE -> {
                            loadImageFromFile()?.let { img ->
                                loadedImage = img
                                appState = AppState.PHOTO_LOADED
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
                                ruler = RulerData(
                                    x = img.width / 2f,
                                    y = img.height / 2f,
                                    scalePxPerMm = scalePxPerMm
                                )
                                points = AppPoints()  // ← очищаем все точки
                                appState = AppState.RULER_PLACED
                            }
                        }
                        AppState.RULER_PLACED -> {
                            val img = loadedImage ?: return@BottomBar
                            saveImageWithDialog(img, ruler)
                        }
                        else -> appState = nextState(appState)
                    }
                }
            )
        }
    }
}