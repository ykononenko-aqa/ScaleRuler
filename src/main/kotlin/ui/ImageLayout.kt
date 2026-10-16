package com.ykononenko.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Геометрия отображения изображения внутри Canvas.
 *
 * @property scale коэффициент перевода пикселей изображения в пиксели Canvas
 * @property offsetX смещение изображения по X на Canvas
 * @property offsetY смещение изображения по Y на Canvas
 * @property displayWidth ширина изображения на экране в пикселях Canvas
 * @property displayHeight высота изображения на экране в пикселях Canvas
 */
data class ImageLayout(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val displayWidth: Float,
    val displayHeight: Float
)

/**
 * Вписывает изображение в область Canvas с сохранением пропорций и центрирует его.
 *
 * @param image отображаемое изображение
 * @param canvasSize размер области Canvas
 * @return рассчитанная геометрия отображения
 */
fun calculateImageLayout(image: ImageBitmap, canvasSize: Size): ImageLayout {
    val imageWidth = image.width.toFloat()
    val imageHeight = image.height.toFloat()
    val scale = minOf(canvasSize.width / imageWidth, canvasSize.height / imageHeight)
    val displayWidth = imageWidth * scale
    val displayHeight = imageHeight * scale
    val offsetX = (canvasSize.width - displayWidth) / 2f
    val offsetY = (canvasSize.height - displayHeight) / 2f
    return ImageLayout(scale, offsetX, offsetY, displayWidth, displayHeight)
}

/**
 * Переводит координаты Canvas в координаты изображения.
 *
 * @param canvasOffset точка в координатах Canvas
 * @param layout текущая геометрия отображения
 * @return точка в координатах исходного изображения
 */
fun canvasToImage(canvasOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        (canvasOffset.x - layout.offsetX) / layout.scale,
        (canvasOffset.y - layout.offsetY) / layout.scale
    )
}

/**
 * Переводит координаты изображения в координаты Canvas.
 *
 * @param imageOffset точка в координатах изображения
 * @param layout текущая геометрия отображения
 * @return точка в координатах Canvas
 */
fun imageToCanvas(imageOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        imageOffset.x * layout.scale + layout.offsetX,
        imageOffset.y * layout.scale + layout.offsetY
    )
}
