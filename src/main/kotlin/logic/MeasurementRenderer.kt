package com.ykononenko.logic

import com.ykononenko.model.LengthUnit
import com.ykononenko.model.Measurement
import com.ykononenko.model.MeasurementColor
import com.ykononenko.model.Point
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.TextLine
import java.util.Locale

/**
 * Отрисовщик размерных линий, точек и подписей.
 *
 * Используется и для показа в окне (`ui/MainArea.kt`, `scale = layout.scale`),
 * и для экспорта (`ImageConverter.kt`, `scale = 1f`). Толщина линии, радиус
 * точек и размер шрифта заданы в пикселях канваса и не зависят от масштаба
 * изображения.
 */
object MeasurementRenderer {

    /** Радиус маркера точки в пикселях канваса. */
    private const val POINT_RADIUS_PX = 6f

    /** Полудлина контрастного крестика внутри точки. */
    private const val POINT_CROSS_HALF_PX = 3f

    /** Толщина контрастного крестика в пикселях канваса. */
    private const val POINT_CROSS_WIDTH_PX = 1.5f

    /** Толщина размерной линии в пикселях канваса. */
    private const val LINE_WIDTH_PX = 2f

    /** Зазор между линией и базовой линией подписи в пикселях канваса. */
    private const val LABEL_GAP_PX = 6f

    /**
     * Рисует завершённые размерные линии с точками и подписями.
     *
     * @param canvas целевой канвас
     * @param measurements завершённые линии в координатах изображения
     * @param ruler линейка, задающая масштаб подписей
     * @param unit единица измерения для текста подписи
     * @param scale коэффициент перевода пикселей изображения в пиксели канваса
     * @param offsetX смещение изображения по X на канвасе
     * @param offsetY смещение изображения по Y на канвасе
     */
    fun draw(
        canvas: SkiaCanvas,
        measurements: List<Measurement>,
        ruler: RulerData,
        unit: LengthUnit,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        if (measurements.isEmpty() || ruler.scalePxPerMm <= 0f) return

        val stroke = Paint().apply {
            mode = PaintMode.STROKE
            strokeWidth = LINE_WIDTH_PX
            isAntiAlias = true
        }
        val fill = Paint().apply { isAntiAlias = true }
        val cross = Paint().apply {
            mode = PaintMode.STROKE
            strokeWidth = POINT_CROSS_WIDTH_PX
            isAntiAlias = true
        }

        measurements.forEach { measurement ->
            val x1 = offsetX + measurement.point1.x * scale
            val y1 = offsetY + measurement.point1.y * scale
            val x2 = offsetX + measurement.point2.x * scale
            val y2 = offsetY + measurement.point2.y * scale
            val color = colorOf(measurement.color)

            stroke.color = color
            fill.color = color
            cross.color = contrastColorOf(measurement.color)

            canvas.drawLine(x1, y1, x2, y2, stroke)
            drawPoint(canvas, x1, y1, fill, cross)
            drawPoint(canvas, x2, y2, fill, cross)

            val fontSize = measurement.fontSizePx
            if (fontSize > 0f) {
                val font = Font(LabelTypeface.bold, fontSize).apply { isEmboldened = true }
                drawLabel(canvas, measurement, ruler, unit, font, fill, x1, y1, x2, y2)
            }
        }
    }

    /**
     * Рисует временную точку до сохранения первой точки линии.
     *
     * @param canvas целевой канвас
     * @param point точка в координатах изображения
     * @param color цвет маркера
     * @param scale коэффициент перевода пикселей изображения в пиксели канваса
     * @param offsetX смещение изображения по X на канвасе
     * @param offsetY смещение изображения по Y на канвасе
     */
    fun drawPending(
        canvas: SkiaCanvas,
        point: Point,
        color: MeasurementColor,
        scale: Float,
        offsetX: Float,
        offsetY: Float
    ) {
        val fill = Paint().apply {
            this.color = colorOf(color)
            isAntiAlias = true
        }
        val cross = Paint().apply {
            this.color = contrastColorOf(color)
            mode = PaintMode.STROKE
            strokeWidth = POINT_CROSS_WIDTH_PX
            isAntiAlias = true
        }
        drawPoint(
            canvas = canvas,
            x = offsetX + point.x * scale,
            y = offsetY + point.y * scale,
            fill = fill,
            cross = cross
        )
    }

    /**
     * Рисует точку с контрастным крестиком, чтобы она была заметна на любом фоне.
     */
    private fun drawPoint(
        canvas: SkiaCanvas,
        x: Float,
        y: Float,
        fill: Paint,
        cross: Paint
    ) {
        canvas.drawCircle(x, y, POINT_RADIUS_PX, fill)
        canvas.drawLine(
            x - POINT_CROSS_HALF_PX,
            y,
            x + POINT_CROSS_HALF_PX,
            y,
            cross
        )
        canvas.drawLine(
            x,
            y - POINT_CROSS_HALF_PX,
            x,
            y + POINT_CROSS_HALF_PX,
            cross
        )
    }

    /**
     * Рисует подпись длины над серединой линии.
     */
    private fun drawLabel(
        canvas: SkiaCanvas,
        measurement: Measurement,
        ruler: RulerData,
        unit: LengthUnit,
        font: Font,
        paint: Paint,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float
    ) {
        val length = distanceBetween(measurement.point1, measurement.point2) /
            ruler.scalePxPerMm
        val text = String.format(Locale.US, "%.2f %s", length, unit.label)
        if (text.isEmpty()) return

        val line = TextLine.make(text, font)
        try {
            val centerX = (x1 + x2) / 2f
            val baselineY = (y1 + y2) / 2f - LABEL_GAP_PX
            canvas.drawTextLine(line, centerX - line.width / 2f, baselineY, paint)
        } finally {
            line.close()
        }
    }

    /** Возвращает Skia-цвет для цвета размерной линии. */
    private fun colorOf(color: MeasurementColor): Int = when (color) {
        MeasurementColor.BLACK -> Color.makeRGB(0, 0, 0)
        MeasurementColor.WHITE -> Color.makeRGB(255, 255, 255)
    }

    /** Возвращает контрастный цвет крестика внутри точки. */
    private fun contrastColorOf(color: MeasurementColor): Int = when (color) {
        MeasurementColor.BLACK -> Color.makeRGB(255, 255, 255)
        MeasurementColor.WHITE -> Color.makeRGB(0, 0, 0)
    }
}
