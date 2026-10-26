package com.ykononenko.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import com.ykononenko.controls.EditorSession
import com.ykononenko.logic.NorthArrowRenderer
import com.ykononenko.logic.RulerRenderers
import com.ykononenko.model.AppPoints

/**
 * Центральная область приложения: изображение, расставленные точки, линейка и
 * указатель на север.
 *
 * Здесь обрабатываются клики по Canvas, перетаскивание линейки и
 * перемещение/поворот указателя. Все координаты хранятся в пикселях
 * изображения и конвертируются через [calculateImageLayout] / [canvasToImage] /
 * [imageToCanvas].
 *
 * @param session состояние приложения
 * @param modifier модификатор внешней компоновки
 */
@Composable
fun MainArea(
    session: EditorSession,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .onSizeChanged {
                session.updateCanvasSize(Size(it.width.toFloat(), it.height.toFloat()))
            },
        contentAlignment = Alignment.Center
    ) {
        val image = session.image ?: return@Box
        if (session.canvasSize.width <= 0f || session.canvasSize.height <= 0f) return@Box
        val layout = calculateImageLayout(image, session.canvasSize)

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .canvasInput(session = session, layout = layout)
            ) {
                drawImageScaled(image, layout)
                drawPoints(session.points, layout)
                session.ruler?.let {
                    val pos = imageToCanvas(Offset(it.x, it.y), layout)
                    drawIntoCanvas { canvas ->
                        RulerRenderers.of(it.style).draw(
                            canvas.nativeCanvas,
                            it,
                            layout.scale,
                            pos.x,
                            pos.y,
                            session.unit
                        )
                    }
                }
                session.northArrow?.let {
                    drawIntoCanvas { canvas ->
                        NorthArrowRenderer.draw(
                            canvas = canvas.nativeCanvas,
                            north = it,
                            imageWidth = image.width,
                            imageHeight = image.height,
                            scale = layout.scale,
                            offsetX = layout.offsetX,
                            offsetY = layout.offsetY
                        )
                    }
                }
            }
        }
    }
}

/**
 * Рисует изображение с учётом рассчитанной геометрии отображения.
 */
private fun DrawScope.drawImageScaled(
    image: androidx.compose.ui.graphics.ImageBitmap,
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

/**
 * Рисует маркеры расставленных точек.
 */
private fun DrawScope.drawPoints(points: AppPoints, layout: ImageLayout) {
    points.point1?.let { drawMarker(imageToCanvas(it, layout)) }
    points.point2?.let { drawMarker(imageToCanvas(it, layout)) }
    points.pendingPoint?.let { drawMarker(imageToCanvas(it, layout)) }
}

/**
 * Рисует маркер одной точки в виде перекрестия.
 */
private fun DrawScope.drawMarker(center: Offset) {
    drawCircle(color = Color.Green.copy(alpha = 0.3f), radius = 12f, center = center)
    drawCircle(color = Color.Green, radius = 12f, center = center, style = Stroke(width = 2f))
    drawLine(Color.Green, Offset(center.x - 6, center.y), Offset(center.x + 6, center.y), strokeWidth = 2f)
    drawLine(Color.Green, Offset(center.x, center.y - 6), Offset(center.x, center.y + 6), strokeWidth = 2f)
}
