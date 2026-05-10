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
                    val image = loadImageFromFile()
                    if (image != null) {
                        loadedImage = image
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
                onCanvasClick = { canvasOffset ->
                    if (appState == AppState.AWAITING_POINT_1 || appState == AppState.AWAITING_POINT_2) {
                        points = points.copy(tempMarker = canvasOffset)
                    }
                },
                onRulerDrag = { delta ->
                    val currentRuler = ruler ?: return@MainArea
                    val img = loadedImage ?: return@MainArea
                    val layout = calculateImageLayout(img, canvasSize)
                    val deltaInImage = Offset(
                        delta.x / layout.scale,
                        delta.y / layout.scale
                    )
                    ruler = currentRuler.copy(
                        x = currentRuler.x + deltaInImage.x,
                        y = currentRuler.y + deltaInImage.y
                    )
                },
                onCanvasSizeChanged = { size -> canvasSize = size },
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
                            val image = loadImageFromFile()
                            if (image != null) {
                                loadedImage = image
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
                                val imagePoint = canvasToImage(marker, layout)
                                points = points.copy(point1 = imagePoint, tempMarker = null)
                                appState = AppState.AWAITING_POINT_2
                            }
                        }
                        AppState.AWAITING_POINT_2 -> {
                            val marker = points.tempMarker
                            val img = loadedImage
                            if (marker != null && img != null) {
                                val layout = calculateImageLayout(img, canvasSize)
                                val imagePoint = canvasToImage(marker, layout)
                                points = points.copy(point2 = imagePoint, tempMarker = null)
                                appState = AppState.POINTS_SET
                            }
                        }
                        AppState.POINTS_SET -> {
                            val mm = mmText.toFloatOrNull()
                            val p1 = points.point1
                            val p2 = points.point2
                            val img = loadedImage
                            if (mm != null && mm > 0 && p1 != null && p2 != null && img != null) {
                                val distancePx = distanceBetween(p1, p2)
                                val scalePxPerMm = distancePx / mm
                                val rulerX = img.width.toFloat() / 2f  // центр изображения по X
                                val rulerY = img.height.toFloat() / 2f  // центр изображения по Y
                                ruler = RulerData(
                                    x = rulerX,
                                    y = rulerY,
                                    scalePxPerMm = scalePxPerMm
                                )
                                appState = AppState.RULER_PLACED
                            }
                        }
                        AppState.RULER_PLACED -> {
                            val img = loadedImage ?: return@BottomBar
                            val r = ruler ?: return@BottomBar

                            val w = img.width
                            val h = img.height
                            val result = java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB)
                            val g = result.createGraphics()

                            // Конвертируем ImageBitmap через readPixels в IntArray
                            val pixels = IntArray(w * h)
                            img.readPixels(pixels)

                            for (y in 0 until h) {
                                for (x in 0 until w) {
                                    val i = y * w + x
                                    val color = pixels[i]
                                    val a = (color shr 24) and 0xFF
                                    val r_val = (color shr 16) and 0xFF
                                    val g_val = (color shr 8) and 0xFF
                                    val b_val = color and 0xFF
                                    val argb = (a shl 24) or (r_val shl 16) or (g_val shl 8) or b_val
                                    result.setRGB(x, y, argb)
                                }
                            }

                            // Рисуем линейку
                            val rulerW = 200f
                            val rulerH = 50f
                            g.color = java.awt.Color(255, 255, 255, 230)
                            g.fillRect(r.x.toInt(), r.y.toInt(), rulerW.toInt(), rulerH.toInt())
                            g.color = java.awt.Color.BLACK
                            g.drawRect(r.x.toInt(), r.y.toInt(), rulerW.toInt(), rulerH.toInt())

                            var stepMm = 1f
                            var stepPx = stepMm * r.scalePxPerMm
                            while (stepPx < 5f) { stepMm++; stepPx = stepMm * r.scalePxPerMm }
                            var x = 0f
                            var mmVal = 0f
                            g.font = java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)
                            while (x <= rulerW + 1f) {
                                val tx = r.x + x
                                val maj = mmVal.toInt() % 10 == 0
                                g.drawLine(tx.toInt(), r.y.toInt(), tx.toInt(), (r.y + if (maj) 14f else 7f).toInt())
                                if (maj) g.drawString("${mmVal.toInt()}", tx.toInt() - 8, (r.y + rulerH + 14f).toInt())
                                x += stepPx; mmVal += stepMm
                            }
                            g.drawString("мм", (r.x + rulerW - 22f).toInt(), (r.y + rulerH + 14f).toInt())
                            g.dispose()

                            // Диалог сохранения
                            val chooser = javax.swing.JFileChooser().apply {
                                fileFilter = javax.swing.filechooser.FileNameExtensionFilter("PNG (*.png)", "png")
                                selectedFile = java.io.File("result.png")
                            }
                            if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                                val file = chooser.selectedFile
                                val out = if (file.extension.isBlank()) java.io.File("${file.absolutePath}.png") else file
                                javax.imageio.ImageIO.write(result, "png", out)
                            }
                        }
                        else -> appState = nextState(appState)
                    }
                }
            )
        }
    }
}