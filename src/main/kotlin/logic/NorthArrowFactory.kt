package com.ykononenko.logic

import com.ykononenko.model.Corner
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.NorthArrowStyle
import com.ykononenko.utils.IMAGE_MARGIN_PX

/**
 * Фабрика указателя на север: создание, позиционирование и изменение размера
 * [NorthArrowData] относительно изображения.
 *
 * Указатель всегда вписывается так, чтобы внешняя невидимая окружность
 * целиком оставалась в пределах изображения.
 */
object NorthArrowFactory {

    /**
     * Создаёт указатель в правом верхнем углу изображения.
     *
     * @param imageWidth ширина изображения в пикселях
     * @param imageHeight высота изображения в пикселях
     * @param style стиль отрисовки указателя
     * @return новый указатель в пиксельных координатах изображения
     */
    fun create(
        imageWidth: Int,
        imageHeight: Int,
        style: NorthArrowStyle = NorthArrowStyle.BLACK
    ): NorthArrowData {
        val north = NorthArrowData(centerX = 0f, centerY = 0f, style = style)
        return moveToCorner(north, imageWidth, imageHeight, Corner.TOP_RIGHT)
    }

    /**
     * Перемещает указатель в заданный угол изображения с отступом [IMAGE_MARGIN_PX].
     *
     * @param north текущий указатель (сохраняет размер и угол)
     * @param imageWidth ширина изображения в пикселях
     * @param imageHeight высота изображения в пикселях
     * @param corner целевой угол
     * @return копия указателя с новым центром
     */
    fun moveToCorner(
        north: NorthArrowData,
        imageWidth: Int,
        imageHeight: Int,
        corner: Corner
    ): NorthArrowData {
        val radius = north.radiusPx(imageWidth, imageHeight)

        val x = when (corner) {
            Corner.TOP_LEFT, Corner.BOTTOM_LEFT -> radius + IMAGE_MARGIN_PX
            Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT -> imageWidth - radius - IMAGE_MARGIN_PX
        }
        val y = when (corner) {
            Corner.TOP_LEFT, Corner.TOP_RIGHT -> radius + IMAGE_MARGIN_PX
            Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT -> imageHeight - radius - IMAGE_MARGIN_PX
        }

        return north.copy(centerX = x, centerY = y)
    }

    /**
     * Изменяет размер указателя.
     *
     * Размер ограничивается только допустимым диапазоном; положение центра не
     * корректируется — пользователь может расположить указатель как угодно.
     *
     * @param north текущий указатель
     * @param fraction новый радиус как доля короткой стороны изображения
     * @return копия указателя с ограниченным размером
     */
    fun setRadiusFraction(
        north: NorthArrowData,
        fraction: Float
    ): NorthArrowData = north.copy(
        radiusFraction = fraction.coerceIn(
            NorthArrowData.MIN_RADIUS_FRACTION,
            NorthArrowData.MAX_RADIUS_FRACTION
        )
    )

    /**
     * Смещает центр указателя без ограничения границами изображения.
     *
     * @param north текущий указатель
     * @param deltaX смещение по X в пикселях изображения
     * @param deltaY смещение по Y в пикселях изображения
     * @return копия указателя с новым центром
     */
    fun move(
        north: NorthArrowData,
        deltaX: Float,
        deltaY: Float
    ): NorthArrowData = north.copy(
        centerX = north.centerX + deltaX,
        centerY = north.centerY + deltaY
    )

    /**
     * Подгоняет положение указателя так, чтобы внешняя невидимая окружность
     * не выходила за пределы изображения.
     *
     * Используется при изменении размера и пакетной обработке.
     *
     * @param north указатель, задающий размер
     * @param imageWidth ширина целевого изображения в пикселях
     * @param imageHeight высота целевого изображения в пикселях
     * @return копия указателя с центром, вписанным в границы изображения
     */
    fun fitToImage(
        north: NorthArrowData,
        imageWidth: Int,
        imageHeight: Int
    ): NorthArrowData {
        val radius = north.radiusPx(imageWidth, imageHeight)

        val minX = radius + IMAGE_MARGIN_PX
        val maxX = (imageWidth - radius - IMAGE_MARGIN_PX).coerceAtLeast(minX)
        val minY = radius + IMAGE_MARGIN_PX
        val maxY = (imageHeight - radius - IMAGE_MARGIN_PX).coerceAtLeast(minY)

        return north.copy(
            centerX = north.centerX.coerceIn(minX, maxX),
            centerY = north.centerY.coerceIn(minY, maxY)
        )
    }
}
