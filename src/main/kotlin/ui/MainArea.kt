package com.ykononenko.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import com.ykononenko.logic.RulerDrawer
import com.ykononenko.model.AppPoints
import com.ykononenko.model.RulerData
import com.ykononenko.state.AppState

@Composable
fun MainArea(
    appState: AppState,
    image: ImageBitmap?,
    points: AppPoints,
    ruler: RulerData?,
    onCanvasClick: (Offset) -> Unit,
    onRulerDrag: (Offset) -> Unit,
    onCanvasSizeChanged: (Size) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentLayout by remember { mutableStateOf<ImageLayout?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .onSizeChanged { /* для перерасчёта */ },
        contentAlignment = Alignment.Center
    ) {
        if (image != null && appState != AppState.IDLE) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Слой Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .canvasInput(
                            appState = appState,
                            onClick = { onCanvasClick(it) },
                            onDrag = { onRulerDrag(it) }
                        )
                ) {
                    onCanvasSizeChanged(size)
                    val layout = calculateImageLayout(image, size)
                    currentLayout = layout
                    drawImageScaled(image, layout)
                    drawPoints(points, layout)
                    ruler?.let { RulerDrawer.drawOnCanvas(this, it, layout) }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawImageScaled(
    image: ImageBitmap,
    layout: ImageLayout
) {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPoints(
    points: AppPoints,
    layout: ImageLayout
) {
    points.point1?.let { drawMarker(imageToCanvas(it, layout)) }
    points.point2?.let { drawMarker(imageToCanvas(it, layout)) }
    points.tempMarker?.let { drawMarker(it) }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMarker(center: Offset) {
    drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = 12f, center = center)
    drawCircle(color = Color.Green, radius = 12f, center = center, style = Stroke(width = 2f))
    drawLine(Color.Green, Offset(center.x - 6, center.y), Offset(center.x + 6, center.y), strokeWidth = 2f)
    drawLine(Color.Green, Offset(center.x, center.y - 6), Offset(center.x, center.y + 6), strokeWidth = 2f)
}