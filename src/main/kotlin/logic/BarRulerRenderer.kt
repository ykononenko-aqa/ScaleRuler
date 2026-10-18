package com.ykononenko.logic

import com.ykononenko.model.RulerData
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect

/**
 * Рендерер линейки в стиле полосы снизу с зарубками.
 *
 * Рисует горизонтальную полосу по нижнему краю габарита и вертикальные зарубки
 * на каждом шаге. Первая и последняя зарубки — во всю высоту линейки для
 * ориентира, остальные — короче. Цвет полосы и зарубок задаётся в конструкторе
 * (чёрный или белый), поэтому один класс покрывает оба новых стиля.
 *
 * Не помещающиеся в ячейку подписи выносятся за габарит линейки тем же цветом,
 * что и полоса.
 *
 * @param color цвет полосы и зарубок в формате Skia (см. `Color.makeRGB`)
 */
class BarRulerRenderer(private val color: Int) : BaseRulerRenderer() {

    /** Подписи рисуются тем же цветом, что полоса и зарубки. */
    override val labelColor: Int
        get() = color

    /** Подписи, не поместившиеся в ячейку, выносятся за габарит линейки. */
    protected override val movesOverflowingOutside: Boolean
        get() = true

    override fun drawBody(canvas: SkiaCanvas, ruler: RulerData, scale: Float) {
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val lengthPx = ruler.lengthMm * ruler.scalePxPerMm
        val thickness = maxOf(rh * STRIP_THICKNESS_FRACTION, 1f / scale)
        val stepPx = ruler.scalePerCell * ruler.scalePxPerMm
        val steps = ruler.cellCount.coerceAtLeast(1)

        val paint = Paint().apply { this.color = this@BarRulerRenderer.color }

        // Полоса по нижнему краю
        canvas.drawRect(Rect(0f, rh - thickness, lengthPx, rh), paint)

        // Зарубки: короткие по шагам, длинные — на начале и конце
        for (i in 0..steps) {
            val x = i * stepPx
            val height = if (i == 0 || i == steps) rh else rh * MINOR_TICK_FRACTION
            canvas.drawRect(Rect(x - thickness / 2f, rh - height, x + thickness / 2f, rh), paint)
        }
    }

    private companion object {

        /** Толщина полосы и зарубок как доля высоты габарита линейки. */
        const val STRIP_THICKNESS_FRACTION = 0.12f

        /** Высота короткой зарубки как доля высоты габарита линейки. */
        const val MINOR_TICK_FRACTION = 0.5f
    }
}
