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
 * Выступающие за габарит линейки части подписей в пикселях изображения.
 *
 * @property startPx выступ перед началом линейки (влево для горизонтальной
 *   ориентации, вниз — для вертикальной)
 * @property endPx выступ за концом линейки (вправо для горизонтальной
 *   ориентации, вверх — для вертикальной)
 */
data class LabelOverflow(val startPx: Float, val endPx: Float) {

    companion object {

        /** Отсутствие выступа. */
        val NONE = LabelOverflow(0f, 0f)
    }
}

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

    /**
     * Возвращает выступ подписей за габарит линейки в пикселях изображения
     * (масштаб 1:1). Нужен для позиционирования линейки по углам, чтобы
     * вынесенные подписи не обрезались краем изображения.
     *
     * @param ruler геометрия линейки
     * @param unit единица измерения для текста подписей
     * @return выступ перед началом и за концом линейки
     */
    fun labelOverflow(ruler: RulerData, unit: LengthUnit): LabelOverflow
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

    /**
     * Признак того, что не помещающиеся в ячейку подписи выносятся за габарит
     * линейки. По умолчанию `false` — подпись остаётся внутри и наезжает на
     * соседние клетки.
     */
    protected open val movesOverflowingOutside: Boolean
        get() = false

    /**
     * Цвет подписи, вынесенной за габарит линейки (поверх фотографии, где фон
     * неизвестен). По умолчанию совпадает с [labelColor].
     */
    protected open fun outsideLabelColor(): Int = labelColor

    final override fun labelOverflow(ruler: RulerData, unit: LengthUnit): LabelOverflow {
        if (!movesOverflowingOutside) return LabelOverflow.NONE

        val rh = ruler.heightMm * ruler.scalePxPerMm
        val font = Font(labelTypeface, labelFontSize(rh)).apply { isEmboldened = true }
        val availablePx = if (ruler.cellCount <= 1) {
            ruler.lengthMm * ruler.scalePxPerMm
        } else {
            ruler.scalePerCell * ruler.scalePxPerMm
        }
        val fitLimit = availablePx * LABEL_FIT_FRACTION

        val start = if (ruler.cellCount <= 1 || measureWidth(firstLabelText(ruler), font) <= fitLimit) {
            0f
        } else {
            measureWidth(firstLabelText(ruler), font)
        }
        val lastWidth = measureWidth(lastLabelText(ruler, unit), font)
        val end = if (lastWidth > fitLimit) lastWidth else 0f

        return LabelOverflow(start, end)
    }

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
        val fontSize = labelFontSize(rh)
        val stepPx = ruler.scalePerCell * ruler.scalePxPerMm * scale

        val font = Font(labelTypeface, fontSize).apply { isEmboldened = true }
        val baselineY = originY + rh - 2f
        val insidePaint = Paint().apply { color = labelColor }
        val lastInsidePaint = Paint().apply { color = lastDivisionLabelColor(ruler) }
        val outsidePaint = Paint().apply { color = outsideLabelColor() }

        if (ruler.cellCount <= 1) {
            // Единственное деление — подписываем только его, с единицей измерения
            val text = lastLabelText(ruler, unit)
            val lastEdgeX = originX + stepPx
            val availablePx = ruler.lengthMm * ruler.scalePxPerMm * scale
            if (movesOverflowingOutside && measureWidth(text, font) > availablePx * LABEL_FIT_FRACTION) {
                drawLabel(canvas, text, lastEdgeX, baselineY, font, outsidePaint, alignLeft = true)
            } else {
                drawLabel(canvas, text, lastEdgeX, baselineY, font, lastInsidePaint)
            }
            return
        }

        val oneShift = fontSize * LABEL_ONE_LEFT_SHIFT_FRACTION

        // Первое деление — только число
        val firstText = firstLabelText(ruler)
        if (movesOverflowingOutside && measureWidth(firstText, font) > stepPx * LABEL_FIT_FRACTION) {
            // Не помещается — выносим за левый конец линейки
            drawLabel(canvas, firstText, originX, baselineY, font, outsidePaint)
        } else {
            // Внутри первой ячейки, прижато к правому краю и чуть отодвинуто влево
            drawLabel(canvas, firstText, originX + stepPx - oneShift, baselineY, font, insidePaint)
        }

        // Последнее деление — число с единицей измерения
        val lastText = lastLabelText(ruler, unit)
        val lastEdgeX = originX + ruler.cellCount * stepPx
        if (movesOverflowingOutside && measureWidth(lastText, font) > stepPx * LABEL_FIT_FRACTION) {
            // Не помещается — выносим за правый конец линейки
            drawLabel(canvas, lastText, lastEdgeX, baselineY, font, outsidePaint, alignLeft = true)
        } else {
            // Внутри последней ячейки, прижато к правому краю
            drawLabel(canvas, lastText, lastEdgeX, baselineY, font, lastInsidePaint)
        }
    }

    /**
     * Возвращает размер шрифта подписи по высоте линейки на канвасе.
     */
    private fun labelFontSize(rulerHeightPx: Float): Float =
        (rulerHeightPx * LABEL_FONT_HEIGHT_FRACTION).coerceIn(LABEL_MIN_FONT_PX, LABEL_MAX_FONT_PX)

    /** Текст подписи первого деления — только число, без единицы. */
    private fun firstLabelText(ruler: RulerData): String = formatValue(ruler.scalePerCell)

    /** Текст подписи последнего деления — число с единицей измерения. */
    private fun lastLabelText(ruler: RulerData, unit: LengthUnit): String =
        "${formatValue(ruler.lengthMm)} ${unit.label}"

    /** Ширина строки при заданном шрифте. */
    private fun measureWidth(text: String, font: Font): Float {
        val line = TextLine.make(text, font)
        try {
            return line.width
        } finally {
            line.close()
        }
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
     * Рисует одну строку текста с привязкой к [anchorX].
     *
     * При [alignLeft] текст начинается от [anchorX] и идёт вправо (вынесен за
     * правый конец линейки), иначе прижимается правым краем к [anchorX].
     *
     * @param canvas целевой канвас
     * @param text текст подписи
     * @param anchorX координата привязки в пикселях канваса
     * @param baselineY координата базовой линии
     * @param font шрифт подписи
     * @param paint кисть подписи
     * @param alignLeft `true` — выравнивание по левому краю
     */
    private fun drawLabel(
        canvas: SkiaCanvas,
        text: String,
        anchorX: Float,
        baselineY: Float,
        font: Font,
        paint: Paint,
        alignLeft: Boolean = false
    ) {
        val line = TextLine.make(text, font)
        try {
            val x = if (alignLeft) anchorX + LABEL_GAP else anchorX - line.width - LABEL_GAP
            canvas.drawTextLine(line, x, baselineY, paint)
        } finally {
            line.close()
        }
    }

    private companion object {

        /** Сдвиг подписи «1» влево как доля размера шрифта. */
        const val LABEL_ONE_LEFT_SHIFT_FRACTION = 0.2f

        /** Доля ширины ячейки, которую может занимать подпись. */
        const val LABEL_FIT_FRACTION = 0.9f

        /** Размер шрифта подписи как доля высоты линейки. */
        const val LABEL_FONT_HEIGHT_FRACTION = 0.6f

        /** Минимальный размер шрифта подписи в пикселях канваса. */
        const val LABEL_MIN_FONT_PX = 10f

        /** Максимальный размер шрифта подписи в пикселях канваса. */
        const val LABEL_MAX_FONT_PX = 36f

        /** Зазор между текстом и границей деления в пикселях канваса. */
        const val LABEL_GAP = 1f
    }
}

/**
 * Фабрика рендереров линейки: сопоставляет [RulerStyle] готовому рендереру.
 *
 * Экземпляры создаются один раз, чтобы не аллоцировать объекты на каждый кадр.
 */
object RulerRenderers {

    /** Рендерер чередующихся чёрно-белых клеток с чёрными подписями за линейкой. */
    private val blocks = BlocksRulerRenderer(Color.makeRGB(0, 0, 0))

    /** Рендерер чередующихся чёрно-белых клеток с белыми подписями за линейкой. */
    private val blocksWhiteLabels = BlocksRulerRenderer(Color.makeRGB(255, 255, 255))

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
        RulerStyle.BLOCKS_WHITE_LABELS -> blocksWhiteLabels
        RulerStyle.BAR_BLACK -> barBlack
        RulerStyle.BAR_WHITE -> barWhite
    }
}
