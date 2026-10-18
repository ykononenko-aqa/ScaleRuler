package com.ykononenko.logic

import com.ykononenko.model.RulerData
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect

/**
 * Рендерер линейки в стиле чередующихся чёрно-белых клеток.
 *
 * Рисует тело линейки одним Skia-проходом; подписи и трансформации выполняет
 * [BaseRulerRenderer]. Не помещающиеся в ячейку подписи выносятся за габарит
 * линейки; их цвет задаётся в конструкторе (чёрный или белый), потому что фон
 * фотографии за линейкой неизвестен.
 *
 * @param outsideColor цвет подписей, вынесенных за габарит линейки
 */
class BlocksRulerRenderer(private val outsideColor: Int) : BaseRulerRenderer() {

    /** Подписи, не поместившиеся в ячейку, выносятся за габарит линейки. */
    override val movesOverflowingOutside: Boolean
        get() = true

    /** Цвет вынесенных за линейку подписей задаётся в конструкторе. */
    override fun outsideLabelColor(): Int = outsideColor

    /**
     * Последняя клетка чёрная при чётном количестве клеток — тогда подпись
     * последнего деления рисуется белым, чтобы быть различимой на её фоне.
     */
    override fun lastDivisionLabelColor(ruler: RulerData): Int =
        if ((ruler.cellCount - 1) % 2 != 0) Color.makeRGB(255, 255, 255) else labelColor

    /**
     * Прямоугольник одной клетки линейки в пикселях изображения.
     *
     * @property left координата X левого края
     * @property top координата Y верхнего края
     * @property width ширина клетки
     * @property height высота клетки
     * @property isBlack `true` — клетка чёрная, иначе белая
     */
    private data class Block(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val isBlack: Boolean
    )

    override fun drawBody(canvas: SkiaCanvas, ruler: RulerData, scale: Float) {
        val blackPaint = Paint().apply { color = Color.makeRGB(0, 0, 0) }
        val whitePaint = Paint().apply { color = Color.makeRGB(255, 255, 255) }
        val borderPaint = Paint().apply {
            color = Color.makeRGB(0, 0, 0)
            mode = PaintMode.STROKE
            strokeWidth = 0.5f / scale
        }

        for (block in calculateBlocks(ruler)) {
            val rect = Rect(block.left, block.top, block.left + block.width, block.top + block.height)
            val paint = if (block.isBlack) blackPaint else whitePaint
            canvas.drawRect(rect, paint)
            canvas.drawRect(rect, borderPaint)
        }
    }

    /**
     * Разбивает линейку на чередующиеся чёрно-белые клетки.
     *
     * @param ruler геометрия линейки в пикселях изображения
     * @return список клеток начиная с левого края линейки
     */
    private fun calculateBlocks(ruler: RulerData): List<Block> {
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val stepPx = ruler.scalePerCell * ruler.scalePxPerMm

        val blocksCount = ruler.cellCount

        return (0 until blocksCount).map { i ->
            Block(
                left = i * stepPx,
                top = 0f,
                width = stepPx,
                height = rh,
                isBlack = i % 2 != 0
            )
        }
    }
}
