package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.model.Corner
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
     * @return новая линейка в пиксельных координатах изображения
     */
    fun create(image: ImageBitmap, scalePxPerMm: Float): RulerData {
        val ruler = RulerData(
            x = 0f,
            y = 0f,
            scalePxPerMm = scalePxPerMm
        )

        return ruler.copy(
            x = image.width - ruler.boxWidthPx - MARGIN,
            y = image.height - ruler.boxHeightPx - MARGIN
        )
    }

    /**
     * Переворачивает ориентацию линейки (горизонтальная ↔ вертикальная).
     *
     * После смены ориентации габарит линейки меняется, поэтому положение
     * вписывается в границы изображения через [fitToImage].
     *
     * @param ruler текущая линейка
     * @param image изображение, задающее границы
     * @return копия линейки с противоположной ориентацией
     */
    fun toggleOrientation(ruler: RulerData, image: ImageBitmap): RulerData {
        val flipped = when (ruler.orientation) {
            RulerOrientation.HORIZONTAL -> RulerOrientation.VERTICAL
            RulerOrientation.VERTICAL -> RulerOrientation.HORIZONTAL
        }
        return fitToImage(ruler.copy(orientation = flipped), image.width, image.height)
    }

    /**
     * Перемещает линейку в заданный угол изображения с отступом [MARGIN].
     *
     * @param ruler текущая линейка (сохраняет масштаб и размеры)
     * @param image изображение, задающее границы
     * @param corner целевой угол
     * @return копия линейки с новыми координатами
     */
    fun moveToCorner(ruler: RulerData, image: ImageBitmap, corner: Corner): RulerData {
        val x = when (corner) {
            Corner.TOP_LEFT, Corner.BOTTOM_LEFT -> MARGIN
            Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT -> image.width - ruler.boxWidthPx - MARGIN
        }
        val y = when (corner) {
            Corner.TOP_LEFT, Corner.TOP_RIGHT -> MARGIN
            Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT -> image.height - ruler.boxHeightPx - MARGIN
        }

        return ruler.copy(x = x, y = y)
    }

    /**
     * Подгоняет положение линейки так, чтобы она не выходила за пределы изображения.
     *
     * Используется при пакетной обработке, когда целевые фото могут иметь другие
     * размеры, чем исходное.
     *
     * @param ruler линейка, задающая масштаб и размеры
     * @param imageWidth ширина целевого изображения в пикселях
     * @param imageHeight высота целевого изображения в пикселях
     * @return копия линейки с координатами, вписанными в границы изображения
     */
    fun fitToImage(ruler: RulerData, imageWidth: Int, imageHeight: Int): RulerData {
        val x = when {
            ruler.x + ruler.boxWidthPx + MARGIN > imageWidth ->
                (imageWidth - ruler.boxWidthPx - MARGIN).coerceAtLeast(MARGIN)
            ruler.x < MARGIN -> MARGIN
            else -> ruler.x
        }

        val y = when {
            ruler.y + ruler.boxHeightPx + MARGIN > imageHeight ->
                (imageHeight - ruler.boxHeightPx - MARGIN).coerceAtLeast(MARGIN)
            ruler.y < MARGIN -> MARGIN
            else -> ruler.y
        }

        return ruler.copy(x = x, y = y)
    }

}
