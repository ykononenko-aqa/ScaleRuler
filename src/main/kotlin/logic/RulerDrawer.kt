package com.ykononenko.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.ykononenko.model.RulerData
import com.ykononenko.model.RulerLabelData
import com.ykononenko.model.RulerLabelsData
import com.ykononenko.ui.imageToCanvas
import org.jetbrains.skia.Canvas as SkiaCanvas

object RulerDrawer {

    fun drawOnCanvas(scope: DrawScope, ruler: RulerData, layout: com.ykononenko.ui.ImageLayout) {
        val canvasPos = imageToCanvas(Offset(ruler.x, ruler.y), layout)
        val rulerWidthPx = ruler.lengthMm * ruler.scalePxPerMm * layout.scale
        val rulerHeightPx = ruler.heightMm * ruler.scalePxPerMm * layout.scale

        val stepMm = calculateStepMm(ruler.scalePxPerMm * layout.scale, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm * layout.scale

        // Закрашенные блоки
        var x = 0f
        var blockIndex = 0

        while (x < rulerWidthPx) {
            val blockLeft = canvasPos.x + x
            val isBlack = blockIndex % 2 != 0

            scope.drawRect(
                color = if (isBlack) Color.Black else Color.White,
                topLeft = Offset(blockLeft, canvasPos.y),
                size = Size(stepPx, rulerHeightPx)
            )
            scope.drawRect(
                color = Color.Black,
                topLeft = Offset(blockLeft, canvasPos.y),
                size = Size(stepPx, rulerHeightPx),
                style = Stroke(width = 0.5f)
            )

            x += stepPx
            blockIndex++
        }
    }

    fun drawOnSkiaCanvas(canvas: SkiaCanvas, ruler: RulerData) {
        val rw = ruler.lengthMm * ruler.scalePxPerMm
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val rx = ruler.x
        val ry = ruler.y

        val stepMm = calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm

        val blackPaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
        }
        val whitePaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(255, 255, 255)
        }
        val borderPaint = org.jetbrains.skia.Paint().apply {
            color = org.jetbrains.skia.Color.makeRGB(0, 0, 0)
            mode = org.jetbrains.skia.PaintMode.STROKE
            strokeWidth = 0.5f
        }

        var x = 0f
        var blockIndex = 0

        while (x < rw) {
            val blockLeft = rx + x
            val paint = if (blockIndex % 2 != 0) blackPaint else whitePaint

            canvas.drawRect(
                org.jetbrains.skia.Rect(blockLeft, ry, blockLeft + stepPx, ry + rh),
                paint
            )
            canvas.drawRect(
                org.jetbrains.skia.Rect(blockLeft, ry, blockLeft + stepPx, ry + rh),
                borderPaint
            )

            x += stepPx
            blockIndex++
        }
    }

    fun calculateLabels(ruler: RulerData): RulerLabelsData {
        val rw = ruler.lengthMm * ruler.scalePxPerMm
        val rh = ruler.heightMm * ruler.scalePxPerMm

        val stepMm = calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm
        val majorStep = if (ruler.lengthMm <= 10f) 1f else 10f

        val labels = mutableListOf<RulerLabelData>()
        var x = 0f
        var mmVal = 0f
        val fontSize = (rh * 0.5f).coerceIn(8f, 48f)

        while (x < rw) {
            if (mmVal.toInt() % majorStep.toInt() == 0) {
                labels.add(
                    RulerLabelData(
                        text = "${mmVal.toInt()}",
                        x = ruler.x + x,
                        y = ruler.y + rh
                    )
                )
            }
            x += stepPx
            mmVal += stepMm
        }

        val unitLabel = RulerLabelData(
            text = "мм",
            x = ruler.x + rw - 4f,
            y = ruler.y + rh  // было: rh + fontSize
        )

        return RulerLabelsData(labels, unitLabel)
    }

    fun drawLabelsOnAWT(g: java.awt.Graphics2D, labelsData: RulerLabelsData, ruler: RulerData) {
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val fontSize = (rh * 0.6f).coerceIn(10f, 36f)

        g.font = java.awt.Font("SansSerif", java.awt.Font.BOLD, fontSize.toInt())
        g.color = java.awt.Color.BLACK

        val labels = labelsData.labels
        if (labels.size >= 2) {
            val stepPx = ruler.scalePxPerMm * calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)

            // "1" — в конце первой ячейки
            val oneX = labels[0].x + stepPx
            g.drawString("1", oneX - fontSize / 2f, labels[0].y - rh + fontSize - 2f)

            // "5" — в конце пятой ячейки
            val fiveX = labels[4].x + stepPx
            g.drawString("5", fiveX - fontSize / 2f, labels[0].y - rh + fontSize - 2f)

            // "мм" — после пятой ячейки
            g.font = java.awt.Font("SansSerif", java.awt.Font.PLAIN, (fontSize * 0.7f).toInt())
            g.drawString("мм", fiveX + 2f, labels[0].y - rh + fontSize - 2f)
        }
    }

    fun calculateStepMm(scalePxPerMm: Float, totalMm: Float): Float {
        var step = 1f
        while (step * scalePxPerMm < 3f && step < totalMm) step++
        return minOf(step, totalMm)
    }
}