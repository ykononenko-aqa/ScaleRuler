package com.ykononenko.logic

import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import com.ykononenko.model.RulerOrientation
import com.ykononenko.model.RulerStyle
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

/**
 * Отрисовщик линейки в конкретном стиле.
 *
 * Реализации отвечают только за тело линейки ([BaseRulerRenderer.drawBody]);
 * трансформации, подписи и подбор шрифта выполняет общая база. Используется и
 * для показа в окне (`ui/MainArea.kt`, `scale = layout.scale`), и для экспорта
 * (`ImageConverter.kt`, `scale = 1f`).
 */
interface RulerRenderer {

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
    )
}

/**
 * Общая база рендереров линейки.
 *
 * Задаёт шаблон отрисовки: настроить трансформацию, нарисовать тело линейки
 * ([drawBody]), сбросить трансформацию и нарисовать подписи. Содержит
 * разделяемые вспомогательные методы — расчёт шага клеток и отрисовку подписей.
 */
abstract class BaseRulerRenderer : RulerRenderer {

    /**
     * Жирный шрифт для подписей, определяется один раз при первом обращении.
     *
     * Может быть `null`, если подходящий системный шрифт не найден; тогда Skia
     * использует шрифт по умолчанию.
     */
    private val labelTypeface: Typeface? by lazy { resolveLabelTypeface() }

    /**
     * Цвет подписей линейки. По умолчанию чёрный; реализации могут вернуть цвет
     * самой линейки, чтобы подписи совпадали с ней.
     */
    protected open val labelColor: Int
        get() = Color.makeRGB(0, 0, 0)

    /**
     * Цвет подписи последнего деления. По умолчанию совпадает с [labelColor];
     * стили с чередующимися клетками переопределяют его, чтобы подпись была
     * различима на фоне последней клетки.
     */
    protected open fun lastDivisionLabelColor(ruler: RulerData): Int = labelColor

    final override fun draw(
        canvas: SkiaCanvas,
        ruler: RulerData,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
        unit: LengthUnit
    ) {
        canvas.save()
        applyTransform(canvas, ruler, scale, offsetX, offsetY)
        drawBody(canvas, ruler, scale)
        canvas.restore()
        drawLabels(canvas, ruler, unit, scale, offsetX, offsetY)
    }

    /**
     * Рисует тело линейки внутри трансформации `translate`/`scale`/`rotate`,
     * поэтому координаты задаются в пикселях изображения.
     *
     * @param canvas целевой канвас
     * @param ruler геометрия линейки
     * @param scale текущий коэффициент масштаба (для компенсации толщин)
     */
    protected abstract fun drawBody(canvas: SkiaCanvas, ruler: RulerData, scale: Float)

    /**
     * Настраивает систему координат так, чтобы ось X шла вдоль длины линейки,
     * а ось Y — поперёк. Для вертикальной ориентации начало координат
     * смещается в левый нижний угол габарита и система поворачивается на -90°.
     */
    private fun applyTransform(
        canvas: SkiaCanvas,
        ruler: RulerData,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        if (ruler.orientation == RulerOrientation.HORIZONTAL) {
            canvas.translate(offsetX, offsetY)
            canvas.scale(scale, scale)
        } else {
            canvas.translate(offsetX, offsetY + ruler.boxHeightPx * scale)
            canvas.scale(scale, scale)
            canvas.rotate(-90f)
        }
    }

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
     * Рисует подписи первого и последнего деления линейки.
     *
     * Первое деление подписывается только числом ([RulerData.scalePerCell]),
     * последнее — числом с единицей измерения ([RulerData.lengthMm]). Если
     * деление одно ([RulerData.cellCount] равен 1), подписывается только оно.
     *
     * Вызывается после сброса трансформации, поэтому размер шрифта и координаты
     * вычисляются в пикселях канваса (экрана), а не изображения — иначе на
     * уменьшенном превью подписи становились неразличимо мелкими.
     *
     * Для вертикальной линейки подписи поворачиваются на 90° и читаются снизу
     * вверх: начало координат смещается в левый нижний угол габарита, а система
     * координат поворачивается так же, как при отрисовке тела.
     */
    private fun drawLabels(
        canvas: SkiaCanvas,
        ruler: RulerData,
        unit: LengthUnit,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        canvas.save()
        if (ruler.orientation == RulerOrientation.VERTICAL) {
            canvas.translate(offsetX, offsetY + ruler.boxHeightPx * scale)
            canvas.rotate(-90f)
            drawLabelsInFrame(canvas, ruler, unit, scale, 0f, 0f)
        } else {
            drawLabelsInFrame(canvas, ruler, unit, scale, offsetX, offsetY)
        }
        canvas.restore()
    }

    /**
     * Рисует подписи в локальной системе координат линейки.
     *
     * Ось X локальной системы направлена вдоль длины линейки (для вертикальной
     * ориентации — вверх), ось Y — поперёк. Начало системы задаётся [originX] и
     * [originY] в пикселях канваса.
     */
    private fun drawLabelsInFrame(
        canvas: SkiaCanvas,
        ruler: RulerData,
        unit: LengthUnit,
        scale: Float,
        originX: Float,
        originY: Float
    ) {
        val rh = ruler.heightMm * ruler.scalePxPerMm * scale
        val fontSize = (rh * 0.6f).coerceIn(10f, 36f)
        val stepPx = ruler.scalePerCell * ruler.scalePxPerMm * scale

        val font = Font(labelTypeface, fontSize).apply { isEmboldened = true }
        val paint = Paint().apply { color = labelColor }
        val lastPaint = Paint().apply { color = lastDivisionLabelColor(ruler) }
        val baselineY = originY + rh - 2f
        val oneShift = fontSize * LABEL_ONE_LEFT_SHIFT_FRACTION

        if (ruler.cellCount <= 1) {
            // Единственное деление — подписываем только его, с единицей измерения
            drawLabel(canvas, "${formatValue(ruler.lengthMm)} ${unit.label}", originX + stepPx, baselineY, font, lastPaint)
            return
        }

        // Первое деление — только число, прижато к правому краю и чуть отодвинуто влево
        drawLabel(canvas, formatValue(ruler.scalePerCell), originX + stepPx - oneShift, baselineY, font, paint)

        // Последнее деление — число с единицей измерения, прижато к правому краю
        val lastEdgeX = originX + ruler.cellCount * stepPx
        drawLabel(canvas, "${formatValue(ruler.lengthMm)} ${unit.label}", lastEdgeX, baselineY, font, lastPaint)
    }

    /**
     * Форматирует значение подписи: целое — без дробной части, дробное — без
     * лишних нулей в конце.
     */
    private fun formatValue(value: Float): String {
        return if (value == value.toLong().toFloat()) {
            value.toLong().toString()
        } else {
            value.toString().trimEnd('0').trimEnd('.')
        }
    }

    /**
     * Рисует одну строку текста, прижатую правым краем к [rightEdgeX].
     *
     * Текст формируется через [TextLine] и рисуется по базовой линии [baselineY].
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

    private companion object {

        /** Сдвиг подписи «1» влево как доля размера шрифта. */
        const val LABEL_ONE_LEFT_SHIFT_FRACTION = 0.2f
    }
}

/**
 * Фабрика рендереров линейки: сопоставляет [RulerStyle] готовому рендереру.
 *
 * Экземпляры создаются один раз, чтобы не аллоцировать объекты на каждый кадр.
 */
object RulerRenderers {

    /** Рендерер чередующихся чёрно-белых клеток. */
    private val blocks = BlocksRulerRenderer

    /** Рендерер полосы снизу с чёрными зарубками. */
    private val barBlack = BarRulerRenderer(Color.makeRGB(0, 0, 0))

    /** Рендерер полосы снизу с белыми зарубками. */
    private val barWhite = BarRulerRenderer(Color.makeRGB(255, 255, 255))

    /**
     * Возвращает рендерер для заданного стиля.
     *
     * @param style стиль отрисовки линейки
     * @return соответствующий рендерер
     */
    fun of(style: RulerStyle): RulerRenderer = when (style) {
        RulerStyle.BLOCKS -> blocks
        RulerStyle.BAR_BLACK -> barBlack
        RulerStyle.BAR_WHITE -> barWhite
    }
}
