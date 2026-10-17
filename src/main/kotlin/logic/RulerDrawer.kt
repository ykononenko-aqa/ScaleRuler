package com.ykononenko.logic

import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.Rect
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

/**
 * Единый отрисовщик линейки.
 *
 * Рисует линейку одним Skia-проходом: сначала чередующиеся чёрно-белые клетки,
 * затем подписи. Используется и для показа в окне (`ui/MainArea.kt`,
 * `scale = layout.scale`), и для экспорта (`ImageConverter.kt`, `scale = 1f`) —
 * менять внешний вид линейки нужно только здесь.
 */
object RulerDrawer {

    /**
     * Жирный шрифт для подписей, определяется один раз при первом обращении.
     *
     * Может быть `null`, если подходящий системный шрифт не найден; тогда Skia
     * использует шрифт по умолчанию.
     */
    private val labelTypeface: Typeface? by lazy { resolveLabelTypeface() }

    /**
     * Подбирает системный жирный шрифт для подписей линейки.
     *
     * Перебирает типичные для целевых платформ семейства в порядке приоритета,
     * затем пробует обобщённое имя `sans-serif` и, в последнюю очередь, шрифт по
     * умолчанию. Нужен потому, что неявный шрифт (`Font(null, size)`) на некоторых
     * системах оказывается пустым и не рисует ни одного глифа.
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
     * Рисует линейку на заданном Skia-канвасе.
     *
     * @param canvas целевой канвас
     * @param ruler геометрия линейки в пикселях изображения
     * @param scale коэффициент перевода пикселей изображения в пиксели канваса
     *   (`layout.scale` для экрана, `1f` для экспорта)
     * @param offsetX смещение линейки по X на канвасе в пикселях канваса
     * @param offsetY смещение линейки по Y на канвасе в пикселях канваса
     * @param unit единица измерения для текста подписей
     */
    fun draw(
        canvas: SkiaCanvas,
        ruler: RulerData,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        unit: LengthUnit
    ) {
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        drawBlocks(canvas, ruler, scale)
        canvas.restore()
        drawLabels(canvas, ruler, unit, scale, offsetX, offsetY)
    }

    /**
     * Подбирает шаг клеток линейки в миллиметрах так, чтобы клетка была не менее
     * 3 пикселей.
     *
     * @param scalePxPerMm масштаб — количество пикселей на миллиметр
     * @param totalMm полная длина линейки в миллиметрах
     * @return шаг клетки в миллиметрах, не больше [totalMm]
     */
    fun calculateStepMm(scalePxPerMm: Float, totalMm: Float): Float {
        var step = 1f
        while (step * scalePxPerMm < 3f && step < totalMm) step++
        return minOf(step, totalMm)
    }

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

    /**
     * Разбивает линейку на чередующиеся чёрно-белые клетки.
     *
     * @param ruler геометрия линейки в пикселях изображения
     * @return список клеток начиная с левого края линейки
     */
    private fun calculateBlocks(ruler: RulerData): List<Block> {
        val rh = ruler.heightMm * ruler.scalePxPerMm
        val stepMm = calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm

        val blocksCount = (ruler.lengthMm / stepMm).toInt()

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

    /**
     * Рисует клетки линейки. Вызывается внутри трансформации
     * `translate`/`scale`, поэтому координаты клеток заданы в пикселях изображения.
     *
     * @param canvas целевой канвас
     * @param ruler геометрия линейки
     * @param scale текущий коэффициент масштаба (для компенсации толщины обводки)
     */
    private fun drawBlocks(canvas: SkiaCanvas, ruler: RulerData, scale: Float) {
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
     * Рисует подписи линейки («1» и «5 <единица>»).
     *
     * Вызывается после сброса трансформации, поэтому размер шрифта и координаты
     * вычисляются в пикселях канваса (экрана), а не изображения — иначе на
     * уменьшенном превью подписи становились неразличимо мелкими.
     *
     * @param canvas целевой канвас
     * @param ruler геометрия линейки
     * @param unit единица измерения для текста
     * @param scale коэффициент перевода пикселей изображения в пиксели канваса
     * @param offsetX смещение линейки по X на канвасе
     * @param offsetY смещение линейки по Y на канвасе
     */
    private fun drawLabels(
        canvas: SkiaCanvas,
        ruler: RulerData,
        unit: LengthUnit,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val rh = ruler.heightMm * ruler.scalePxPerMm * scale
        val fontSize = (rh * 0.6f).coerceIn(10f, 36f)
        val stepMm = calculateStepMm(ruler.scalePxPerMm, ruler.lengthMm)
        val stepPx = stepMm * ruler.scalePxPerMm * scale

        val font = Font(labelTypeface, fontSize).apply { isEmboldened = true }
        val paint = Paint().apply { color = Color.makeRGB(0, 0, 0) }
        val baselineY = offsetY + rh - 2f

        // "1" — внутри первой ячейки, прижата к правому краю
        drawLabel(canvas, "1", offsetX + stepPx, baselineY, font, paint)

        // "5 см" — внутри пятой ячейки, прижата к правому краю
        drawLabel(canvas, "5 ${unit.label}", offsetX + 5f * stepPx, baselineY, font, paint)
    }

    /**
     * Рисует одну строку текста, прижатую правым краем к [rightEdgeX].
     *
     * Текст формируется через [TextLine] и рисуется по базовой линии [baselineY].
     *
     * @param canvas целевой канвас
     * @param text отображаемая строка
     * @param rightEdgeX координата X правого края текста
     * @param baselineY координата Y базовой линии текста
     * @param font шрифт текста
     * @param paint кисть (цвет) текста
     */
    private fun drawLabel(
        canvas: SkiaCanvas,
        text: String,
        rightEdgeX: Float,
        baselineY: Float,
        font: Font,
        paint: Paint
    ) {
        val line = TextLine.make(text, font)
        try {
            canvas.drawTextLine(line, rightEdgeX - line.width - 1f, baselineY, paint)
        } finally {
            line.close()
        }
    }
}
