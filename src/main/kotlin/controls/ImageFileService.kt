package com.ykononenko.controls

import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.Measurement
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.RulerData

/**
 * Абстракция над диалогами загрузки и сохранения изображений.
 *
 * Позволяет тестировать действия приложения без открытия системных окон.
 */
interface ImageFileService {

    /**
     * Открывает диалог выбора изображения и загружает выбранный файл.
     *
     * @return загруженное изображение или `null`, если пользователь отменил
     *   выбор либо файл не удалось прочитать
     */
    fun openImage(): ImageBitmap?

    /**
     * Открывает диалог сохранения и записывает изображение вместе с линейкой,
     * указателем на север и размерными линиями в PNG.
     *
     * @return `true`, если файл успешно сохранён; иначе `false`
     */
    fun saveImage(
        image: ImageBitmap,
        ruler: RulerData?,
        northArrow: NorthArrowData?,
        measurements: List<Measurement>,
        measurementFontSize: Float,
        unit: LengthUnit
    ): Boolean
}
