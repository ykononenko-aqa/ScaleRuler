package com.ykononenko.logic

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import com.ykononenko.ui.imageToCanvas
import org.jetbrains.skia.Canvas as SkiaCanvas

object RulerDrawer {

    fun drawOnCanvas(scope: DrawScope, ruler: RulerData, layout: com.ykononenko.ui.ImageLayout) {
        val canvasPos = imageToCanvas(Offset(ruler.x, ruler.y), layout)
        val blocks = calculateBlocks(ruler, layout.scale, canvasPos.x, canvasPos.y)

        for (block in blocks) {
            scope.drawRect(
                color = if (block.isBlack) Color.Black else Color.White,
                topLeft = Offset(block.left, block.top),
                size = Size(block.width, block.height)
            )
            scope.drawRect(
                color = Color.Black,
                topLeft = Offset(block.left, block.top),
                size = Size(block.width, block.height),
                style = Stroke(width = 0.5f)
            )
        }
    }

    fun drawOnSkiaCanvas(canvas: SkiaCanvas, ruler: RulerData) {
        val blocks = calculateBlocks(ruler, 1f, ruler.x, ruler.y)

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

        for (block in blocks) {
            val paint = if (block.isBlack) blackPaint else whitePaint
            val rect = org.jetbrains.skia.Rect(block.left, block.top, block.left + block.width, block.top + block.height)
            canvas.drawRect(rect, paint)
            canvas.drawRect(rect, borderPaint)
        }
    }

    fun drawLabelsOnAWT(g: java.awt.Graphics2D, ruler: RulerData, unit: LengthUnit) {
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val fontSize = (rh * 0.6f).coerceIn(10f, 36f)
        val stepMm = calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm
        val unitLabel = unit.label
        val textY = ruler.y + rh - 2f  // внутри линейки, у нижнего края

        g.font = java.awt.Font("SansSerif", java.awt.Font.BOLD, fontSize.toInt())
        g.color = java.awt.Color.BLACK

        val metrics = g.fontMetrics

        // "1" — внутри первой ячейки, прижата к правому краю
        val oneText = "1"
        val oneWidth = metrics.stringWidth(oneText)
        val oneX = ruler.x + stepPx - oneWidth - 1f
        g.drawString(oneText, oneX, textY)

        // "5 см" — внутри пятой ячейки, прижата к правому краю
        val fiveText = "5 $unitLabel"
        val fiveWidth = metrics.stringWidth(fiveText)
        val fiveX = ruler.x + 5f * stepPx - fiveWidth - 1f
        g.drawString(fiveText, fiveX, textY)
    }

    fun calculateStepMm(scalePxPerMm: Float, totalMm: Float): Float {
        var step = 1f
        while (step * scalePxPerMm < 3f && step < totalMm) step++
        return minOf(step, totalMm)
    }

    private data class Block(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val isBlack: Boolean
    )

    private fun calculateBlocks(
        ruler: RulerData,
        scale: Float,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): List<Block> {
        val rh = ruler.heightMm * ruler.scalePxPerMm * scale

        val stepMm = calculateStepMm(ruler.scalePxPerMm * scale, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm * scale

        val blocks = mutableListOf<Block>()
        val blocksCount = (ruler.lengthMm / stepMm).toInt()

        for (i in 0 until blocksCount) {
            val x = i * stepPx
            blocks.add(
                Block(
                    left = offsetX + x,
                    top = offsetY,
                    width = stepPx,
                    height = rh,
                    isBlack = i % 2 != 0
                )
            )
        }
        return blocks
    }
}