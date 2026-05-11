package com.ykononenko.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import org.jetbrains.skia.Canvas as SkiaCanvas
import com.ykononenko.model.RulerData
import com.ykononenko.ui.ImageLayout
import com.ykononenko.ui.imageToCanvas

object RulerDrawer {

    const val RULER_WIDTH_PX = 200f
    const val RULER_HEIGHT_PX = 50f

    fun drawOnCanvas(scope: DrawScope, ruler: RulerData, layout: ImageLayout) {
        val canvasPos = imageToCanvas(Offset(ruler.x, ruler.y), layout)

        // Фон
        scope.drawRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = canvasPos,
            size = Size(RULER_WIDTH_PX, RULER_HEIGHT_PX)
        )
        // Рамка
        scope.drawRect(
            color = Color.Black,
            topLeft = canvasPos,
            size = Size(RULER_WIDTH_PX, RULER_HEIGHT_PX),
            style = Stroke(width = 1f)
        )

        // Деления
        val stepMm = calculateStepMm(ruler.scalePxPerMm * layout.scale)
        val stepPx = stepMm * ruler.scalePxPerMm * layout.scale

        var x = 0f
        var mmValue = 0f
        while (x <= RULER_WIDTH_PX + 1f) {
            val tickX = canvasPos.x + x
            val isMajor = mmValue.toInt() % 10 == 0
            val tickHeight = if (isMajor) 14f else 7f

            scope.drawLine(
                color = Color.Black,
                start = Offset(tickX, canvasPos.y),
                end = Offset(tickX, canvasPos.y + tickHeight),
                strokeWidth = if (isMajor) 1.5f else 1f
            )
            x += stepPx
            mmValue += stepMm
        }
    }

    fun drawOnSkiaCanvas(canvas: SkiaCanvas, ruler: RulerData) {
        val rw = RULER_WIDTH_PX
        val rh = RULER_HEIGHT_PX
        val rx = ruler.x
        val ry = ruler.y

        // Фон
        val bgPaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(255, 255, 255)
            alpha = 230
        }
        canvas.drawRect(org.jetbrains.skia.Rect(rx, ry, rx + rw, ry + rh), bgPaint)

        // Рамка
        val borderPaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
            mode = org.jetbrains.skia.PaintMode.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(org.jetbrains.skia.Rect(rx, ry, rx + rw, ry + rh), borderPaint)

        // Деления и подписи
        var stepMm = 1f
        var stepPx = stepMm * ruler.scalePxPerMm
        while (stepPx < 5f) { stepMm++; stepPx = stepMm * ruler.scalePxPerMm }

        val linePaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
            strokeWidth = 1f
        }
        val font = org.jetbrains.skia.Font(null, 10f)
        val textPaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
            isAntiAlias = true
        }

        var x = 0f
        var mmVal = 0f
        while (x <= rw + 1f) {
            val tx = rx + x
            val isMajor = mmVal.toInt() % 10 == 0
            val tickH = if (isMajor) 14f else 7f
            canvas.drawLine(tx, ry, tx, ry + tickH, linePaint)
            if (isMajor) {
                canvas.drawString("${mmVal.toInt()}", tx - 8f, ry + rh + 14f, font, textPaint)
            }
            x += stepPx
            mmVal += stepMm
        }
        canvas.drawString("мм", rx + rw - 22f, ry + rh + 14f, font, textPaint)
    }

    private fun calculateStepMm(scalePxPerMm: Float): Float {
        var step = 1f
        while (step * scalePxPerMm < 5f) step++
        return step
    }
}