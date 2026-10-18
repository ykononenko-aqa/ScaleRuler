package com.ykononenko

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.logic.*
import com.ykononenko.model.*
import com.ykononenko.state.AppState
import com.ykononenko.ui.*

/**
 * Корневой composable приложения.
 *
 * Единая точка сборки: всё изменяемое состояние хранится здесь через
 * `remember { mutableStateOf(...) }` и передаётся вниз в панели и область Canvas.
 * Здесь же обрабатываются действия пользователя и переходы между состояниями
 * [AppState].
 *
 * @param onExit обработчик выхода из приложения
 */
@Composable
fun App(onExit: () -> Unit = {}) {
    var appState by remember { mutableStateOf(AppState.IDLE) }
    var mmText by remember { mutableStateOf("") }
    var cellCountText by remember { mutableStateOf("5") }
    var scaleText by remember { mutableStateOf("1") }
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
                        cellCountText = "5"
                        scaleText = "1"
                        selectedUnit = LengthUnit.MM
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
                unit = selectedUnit,
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
                cellCountText = cellCountText,
                onCellCountTextChange = { cellCountText = it },
                scaleText = scaleText,
                onScaleTextChange = { scaleText = it },
                buttonText = buttonText(appState),
                selectedUnit = selectedUnit,
                onUnitChange = { selectedUnit = it },
                orientation = ruler?.orientation ?: RulerOrientation.HORIZONTAL,
                onToggleOrientation = {
                    val img = loadedImage ?: return@BottomBar
                    val r = ruler ?: return@BottomBar
                    ruler = RulerFactory.toggleOrientation(r, img)
                },
                style = ruler?.style ?: RulerStyle.BLOCKS,
                onStyleChange = { selected ->
                    ruler = ruler?.copy(style = selected)
                },
                onMoveToCorner = { corner ->
                    val img = loadedImage ?: return@BottomBar
                    val r = ruler ?: return@BottomBar
                    ruler = RulerFactory.moveToCorner(r, img, corner)
                },
                onBatchClick = { showBatchDialog = true },
                onButtonClick = {
                    when (appState) {
                        AppState.IDLE -> {
                            loadImageFromFile()?.let { img ->
                                loadedImage = img
                                appState = AppState.PHOTO_LOADED
                                mmText = ""
                                cellCountText = "5"
                                scaleText = "1"
                                selectedUnit = LengthUnit.MM
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
                            val cellCount = cellCountText.toIntOrNull()
                            val scalePerCell = scaleText.toFloatOrNull()
                            val p1 = points.point1
                            val p2 = points.point2
                            val img = loadedImage
                            if (mm != null && mm > 0 &&
                                cellCount != null && cellCount >= 1 &&
                                scalePerCell != null && scalePerCell > 0 &&
                                p1 != null && p2 != null && img != null
                            ) {
                                val scalePxPerMm = distanceBetween(p1, p2) / mm
                                ruler = RulerFactory.create(img, scalePxPerMm, cellCount, scalePerCell)
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
                }
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
