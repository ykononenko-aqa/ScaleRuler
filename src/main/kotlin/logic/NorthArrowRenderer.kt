package com.ykononenko.logic

import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.NorthArrowStyle
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Path
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

/**
 * Отрисовщик указателя на север.
 *
 * Рисует стрелку с наконечником, контур центрального круга без заливки и букву
 * «С» у острия. Используется и для показа в окне (`ui/MainArea.kt`,
 * `scale = layout.scale`), и для экспорта (`ImageConverter.kt`, `scale = 1f`).
 *
 * Толщины линий и размер шрифта пропорциональны радиусу, поэтому указатель
 * одинаково выглядит при любом масштабе.
 */
object NorthArrowRenderer {

    /**
     * Жирный шрифт для буквы «С», определяется один раз при первом обращении.
     */
    private val labelTypeface: Typeface? by lazy { resolveLabelTypeface() }

    /**
     * Рисует указатель на заданном Skia-канвасе.
     *
     * @param canvas целевой канвас
     * @param north геометрия указателя в пикселях изображения
     * @param imageWidth ширина изображения в пикселях
     * @param imageHeight высота изображения в пикселях
     * @param scale коэффициент перевода пикселей изображения в пиксели канваса
     *   (`layout.scale` для экрана, `1f` для экспорта)
     * @param offsetX смещение изображения по X на канвасе в пикселях канваса
     * @param offsetY смещение изображения по Y на канвасе в пикселях канваса
     */
    fun draw(
        canvas: SkiaCanvas,
        north: NorthArrowData,
        imageWidth: Int,
        imageHeight: Int,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val radius = north.radiusPx(imageWidth, imageHeight)
        if (radius <= 0f) return

        canvas.save()
        canvas.translate(offsetX + north.centerX * scale, offsetY + north.centerY * scale)
        canvas.scale(scale, scale)
        canvas.rotate(north.angleRad * 180f / Math.PI.toFloat())
        drawPointer(canvas, radius, colorOf(north.style))
        canvas.restore()
    }

    /**
     * Рисует элементы указателя в локальной системе координат: центр в `(0, 0)`,
     * север — вдоль отрицательной оси Y.
     *
     * @param canvas целевой канвас
     * @param radius радиус внешней невидимой окружности в пикселях изображения
     * @param color цвет всех элементов указателя
     */
    private fun drawPointer(canvas: SkiaCanvas, radius: Float, color: Int) {
        val stroke = Paint().apply {
            this.color = color
            mode = PaintMode.STROKE
            strokeWidth = radius * STROKE_FRACTION
        }

        // Центральный круг-ручка: только контур, без заливки
        val handleRadius = radius * NorthArrowData.CENTER_CIRCLE_FRACTION
        canvas.drawCircle(0f, 0f, handleRadius, stroke)

        // Древко стрелки от круга до основания наконечника
        val shaftEnd = -radius * ARROW_BASE_FRACTION
        canvas.drawLine(0f, -handleRadius, 0f, shaftEnd, stroke)

        // Наконечник стрелки
        val tip = -radius * ARROW_TIP_FRACTION
        val halfBase = radius * ARROW_BASE_HALF_FRACTION
        val head = Path().apply {
            moveTo(0f, tip)
            lineTo(-halfBase, shaftEnd)
            lineTo(halfBase, shaftEnd)
            closePath()
        }
        canvas.drawPath(head, Paint().apply { this.color = color })

        drawLetter(canvas, radius, color)
    }

    /**
     * Рисует букву «С» у острия стрелки; она вращается вместе с указателем.
     *
     * @param canvas целевой канвас
     * @param radius радиус указателя в пикселях изображения
     * @param color цвет буквы
     */
    private fun drawLetter(canvas: SkiaCanvas, radius: Float, color: Int) {
        val fontSize = radius * LETTER_FONT_FRACTION
        val font = Font(labelTypeface, fontSize).apply { isEmboldened = true }
        val line = TextLine.make(LETTER, font)
        try {
            val centerY = -radius * LETTER_CENTER_FRACTION
            val baselineY = centerY + fontSize * LETTER_BASELINE_FRACTION
            canvas.drawTextLine(
                line,
                -line.width / 2f,
                baselineY,
                Paint().apply { this.color = color }
            )
        } finally {
            line.close()
        }
    }

    /**
     * Подбирает системный жирный шрифт для буквы «С» так же, как для подписей
     * линейки.
     *
     * @return найденный жирный [Typeface] или `null`, если ничего не найдено
     */
    private fun resolveLabelTypeface(): Typeface? {
        val candidates = arrayOf("Segoe UI", "Arial", "Helvetica", "DejaVu Sans", "Liberation Sans")
        for (family in candidates) {
            FontMgr.default.matchFamilyStyle(family, FontStyle.BOLD)?.let { return it }
        }
        FontMgr.default.legacyMakeTypeface("sans-serif", FontStyle.BOLD)?.let { return it }
        return FontMgr.default.matchFamilyStyle(null, FontStyle.BOLD)
    }

    /**
     * Возвращает Skia-цвет для стиля указателя.
     *
     * @param style стиль указателя
     * @return цвет в формате Skia
     */
    private fun colorOf(style: NorthArrowStyle): Int = when (style) {
        NorthArrowStyle.BLACK -> Color.makeRGB(0, 0, 0)
        NorthArrowStyle.WHITE -> Color.makeRGB(255, 255, 255)
    }

    /** Буква, отмечающая север. */
    private const val LETTER = "С"

    /** Толщина линий указателя как доля радиуса. */
    private const val STROKE_FRACTION = 0.04f

    /** Основание наконечника как доля радиуса. */
    private const val ARROW_BASE_FRACTION = 0.52f

    /** Остриё наконечника как доля радиуса. */
    private const val ARROW_TIP_FRACTION = 0.74f

    /** Половина ширины основания наконечника как доля радиуса. */
    private const val ARROW_BASE_HALF_FRACTION = 0.085f

    /** Размер шрифта буквы как доля радиуса. */
    private const val LETTER_FONT_FRACTION = 0.24f

    /** Смещение центра буквы от центра указателя как доля радиуса. */
    private const val LETTER_CENTER_FRACTION = 0.86f

    /** Смещение базовой линии буквы от её центра как доля размера шрифта. */
    private const val LETTER_BASELINE_FRACTION = 0.35f
}
