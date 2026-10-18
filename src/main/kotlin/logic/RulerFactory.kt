package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import com.ykononenko.model.RulerOrientation

/**
 * Фабрика линейки: создание и позиционирование [RulerData] относительно изображения.
 */
object RulerFactory {

    /** Отступ линейки от края изображения в пикселях. */
    private const val MARGIN = 10f

    /**
     * Создаёт линейку у правого нижнего края изображения по заданному масштабу.
     *
     * @param image изображение, относительно которого размещается линейка
     * @param scalePxPerMm масштаб — пикселей изображения на миллиметр
     * @param cellCount количество ячеек линейки
     * @param scalePerCell сколько единиц измерения приходится на одну ячейку
     * @param unit единица измерения для подписей
     * @return новая линейка в пиксельных координатах изображения
     */
    fun create(
        image: ImageBitmap,
        scalePxPerMm: Float,
        cellCount: Int = RulerData.DEFAULT_CELL_COUNT,
        scalePerCell: Float = RulerData.DEFAULT_SCALE_PER_CELL,
        unit: LengthUnit = LengthUnit.MM
    ): RulerData {
        val ruler = RulerData(
            x = 0f,
            y = 0f,
            scalePxPerMm = scalePxPerMm,
            cellCount = cellCount,
            scalePerCell = scalePerCell
        )

        return moveToCorner(ruler, image, Corner.BOTTOM_RIGHT, unit)
    }

    /**
     * Переворачивает ориентацию линейки (горизонтальная ↔ вертикальная).
     *
     * После смены ориентации габарит линейки меняется, поэтому положение
     * вписывается в границы изображения через [fitToImage].
     *
     * @param ruler текущая линейка
     * @param image изображение, задающее границы
     * @param unit единица измерения для подписей
     * @return копия линейки с противоположной ориентацией
     */
    fun toggleOrientation(ruler: RulerData, image: ImageBitmap, unit: LengthUnit): RulerData {
        val flipped = when (ruler.orientation) {
            RulerOrientation.HORIZONTAL -> RulerOrientation.VERTICAL
            RulerOrientation.VERTICAL -> RulerOrientation.HORIZONTAL
        }
        return fitToImage(ruler.copy(orientation = flipped), image.width, image.height, unit)
    }

    /**
     * Перемещает линейку в заданный угол изображения с отступом [MARGIN].
     *
     * Учитывает выступ подписей за габарит линейки, чтобы вынесенная подпись не
     * обрезалась краем изображения.
     *
     * @param ruler текущая линейка (сохраняет масштаб и размеры)
     * @param image изображение, задающее границы
     * @param corner целевой угол
     * @param unit единица измерения для подписей
     * @return копия линейки с новыми координатами
     */
    fun moveToCorner(
        ruler: RulerData,
        image: ImageBitmap,
        corner: Corner,
        unit: LengthUnit
    ): RulerData {
        val insets = calculateLabelInsets(ruler, unit)

        val x = when (corner) {
            Corner.TOP_LEFT, Corner.BOTTOM_LEFT -> MARGIN + insets.left
            Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT ->
                image.width - ruler.boxWidthPx - insets.right - MARGIN
        }
        val y = when (corner) {
            Corner.TOP_LEFT, Corner.TOP_RIGHT -> MARGIN + insets.top
            Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT ->
                image.height - ruler.boxHeightPx - insets.bottom - MARGIN
        }

        return ruler.copy(x = x, y = y)
    }

    /**
     * Подгоняет положение линейки так, чтобы она не выходила за пределы изображения.
     *
     * Используется при смене ориентации и пакетной обработке, когда целевые фото
     * могут иметь другие размеры, чем исходное. Учитывает выступ подписей.
     *
     * @param ruler линейка, задающая масштаб и размеры
     * @param imageWidth ширина целевого изображения в пикселях
     * @param imageHeight высота целевого изображения в пикселях
     * @param unit единица измерения для подписей
     * @return копия линейки с координатами, вписанными в границы изображения
     */
    fun fitToImage(
        ruler: RulerData,
        imageWidth: Int,
        imageHeight: Int,
        unit: LengthUnit
    ): RulerData {
        val insets = calculateLabelInsets(ruler, unit)

        val minX = MARGIN + insets.left
        val maxX = (imageWidth - ruler.boxWidthPx - insets.right - MARGIN)
            .coerceAtLeast(minX)
        val x = ruler.x.coerceIn(minX, maxX)

        val minY = MARGIN + insets.top
        val maxY = (imageHeight - ruler.boxHeightPx - insets.bottom - MARGIN)
            .coerceAtLeast(minY)
        val y = ruler.y.coerceIn(minY, maxY)

        return ruler.copy(x = x, y = y)
    }

    /**
     * Рассчитывает отступы под вынесенные за габарит подписи.
     *
     * Вдоль длины линейки выступ [LabelOverflow.startPx] уходит к началу, а
     * [LabelOverflow.endPx] — к концу. Для горизонтальной линейки это левый и
     * правый отступы, для вертикальной — нижний и верхний соответственно.
     */
    private fun calculateLabelInsets(ruler: RulerData, unit: LengthUnit): LabelInsets {
        val overflow = RulerRenderers.of(ruler.style).labelOverflow(ruler, unit)
        return if (ruler.orientation == RulerOrientation.HORIZONTAL) {
            LabelInsets(left = overflow.startPx, top = 0f, right = overflow.endPx, bottom = 0f)
        } else {
            LabelInsets(left = 0f, top = overflow.endPx, right = 0f, bottom = overflow.startPx)
        }
    }

    /** Отступы под вынесенные подписи в пикселях изображения. */
    private data class LabelInsets(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )
}
