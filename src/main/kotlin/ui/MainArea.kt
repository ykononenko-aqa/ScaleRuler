package com.ykononenko.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import com.ykononenko.model.AppPoints
import com.ykononenko.model.RulerData
import com.ykononenko.state.AppState

fun canvasToImage(canvasOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        (canvasOffset.x - layout.offsetX) / layout.scale,
        (canvasOffset.y - layout.offsetY) / layout.scale
    )
}

fun imageToCanvas(imageOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        imageOffset.x * layout.scale + layout.offsetX,
        imageOffset.y * layout.scale + layout.offsetY
    )
}

@Composable
fun MainArea(
    appState: AppState,
    image: ImageBitmap?,
    points: AppPoints,
    ruler: RulerData?,
    onCanvasClick: (Offset) -> Unit,
    onRulerDrag: (Offset) -> Unit,
    onCanvasSizeChanged: (Size) -> Unit,
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0)),
        contentAlignment = Alignment.Center
    ) {
        if (image != null && appState != AppState.IDLE) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(appState, ruler, image) {
                        when (appState) {
                            AppState.AWAITING_POINT_1, AppState.AWAITING_POINT_2 -> {
                                detectTapGestures { tapOffset ->
                                    onCanvasClick(tapOffset)
                                }
                            }
                            AppState.RULER_PLACED -> {
                                // Упрощённое перетаскивание — без проверки попадания
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitPointerEvent()
                                        val downChange = down.changes.firstOrNull() ?: continue
                                        if (!downChange.pressed) continue
                                        downChange.consume()

                                        var prevPos = downChange.position
                                        do {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull() ?: break
                                            val delta = Offset(
                                                change.position.x - prevPos.x,
                                                change.position.y - prevPos.y
                                            )
                                            prevPos = change.position
                                            change.consume()
                                            onRulerDrag(delta)
                                        } while (change.pressed)
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
            ) {
                onCanvasSizeChanged(size)
                val layout = calculateImageLayout(image, size)
                drawImageScaled(image, layout)
                drawPoints(points, layout)
                ruler?.let { drawRuler(it, layout) }
            }
        } else {
            Text(message, fontSize = 16.sp, color = Color.DarkGray)
        }
    }
}

data class ImageLayout(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val displayWidth: Float,
    val displayHeight: Float
)

fun calculateImageLayout(image: ImageBitmap, canvasSize: Size): ImageLayout {
    val imageWidth = image.width.toFloat()
    val imageHeight = image.height.toFloat()
    val scale = minOf(canvasSize.width / imageWidth, canvasSize.height / imageHeight)
    val displayWidth = imageWidth * scale
    val displayHeight = imageHeight * scale
    val offsetX = (canvasSize.width - displayWidth) / 2f
    val offsetY = (canvasSize.height - displayHeight) / 2f
    return ImageLayout(scale, offsetX, offsetY, displayWidth, displayHeight)
}

private fun DrawScope.drawImageScaled(image: ImageBitmap, layout: ImageLayout) {
    translate(left = layout.offsetX, top = layout.offsetY) {
        scale(layout.scale, layout.scale, pivot = Offset.Zero) {
            drawImage(
                image = image,
                dstOffset = androidx.compose.ui.unit.IntOffset.Zero,
                dstSize = androidx.compose.ui.unit.IntSize(image.width, image.height)
            )
        }
    }
}

private fun DrawScope.drawPoints(points: AppPoints, layout: ImageLayout) {
    points.point1?.let { drawMarker(imageToCanvas(it, layout)) }
    points.point2?.let { drawMarker(imageToCanvas(it, layout)) }
    points.tempMarker?.let { drawMarker(it) }
}

private fun DrawScope.drawMarker(center: Offset) {
    drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = 12f, center = center)
    drawCircle(color = Color.Green, radius = 12f, center = center, style = Stroke(width = 2f))
    drawLine(color = Color.Green, start = Offset(center.x - 6, center.y), end = Offset(center.x + 6, center.y), strokeWidth = 2f)
    drawLine(color = Color.Green, start = Offset(center.x, center.y - 6), end = Offset(center.x, center.y + 6), strokeWidth = 2f)
}

private fun DrawScope.drawRuler(ruler: RulerData, layout: ImageLayout) {
    val canvasPos = imageToCanvas(Offset(ruler.x, ruler.y), layout)
    val rulerWidthPx = 200f
    val rulerHeightPx = 50f

    // Фон
    drawRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = canvasPos,
        size = Size(rulerWidthPx, rulerHeightPx)
    )
    drawRect(
        color = Color.Black,
        topLeft = canvasPos,
        size = Size(rulerWidthPx, rulerHeightPx),
        style = Stroke(width = 1f)
    )

    // Подбираем шаг делений в пикселях, чтобы было не меньше 5px
    var stepMm = 1f
    var stepPx = stepMm * ruler.scalePxPerMm * layout.scale
    while (stepPx < 5f) {
        stepMm += 1f
        stepPx = stepMm * ruler.scalePxPerMm * layout.scale
    }

    var x = 0f
    var mmValue = 0f
    while (x <= rulerWidthPx + 1f) {
        val tickX = canvasPos.x + x
        val isMajor = (mmValue.toInt() % 10 == 0)
        val tickHeight = if (isMajor) 14f else 7f

        drawLine(
            color = Color.Black,
            start = Offset(tickX, canvasPos.y),
            end = Offset(tickX, canvasPos.y + tickHeight),
            strokeWidth = if (isMajor) 1.5f else 1f
        )

        x += stepPx
        mmValue += stepMm
    }
}