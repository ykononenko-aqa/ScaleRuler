package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.model.Corner
import com.ykononenko.model.RulerData

object RulerFactory {

    private const val MARGIN = 10f  // отступ от края изображения

    fun create(image: ImageBitmap, scalePxPerMm: Float): RulerData {
        val lengthPx = RulerData.DEFAULT_LENGTH_MM * scalePxPerMm
        val heightPx = RulerData.DEFAULT_HEIGHT_MM * scalePxPerMm

        return RulerData(
            x = image.width - lengthPx - MARGIN,
            y = image.height - heightPx - MARGIN,
            scalePxPerMm = scalePxPerMm
        )
    }

    fun moveToCorner(ruler: RulerData, image: ImageBitmap, corner: Corner): RulerData {
        val lengthPx = ruler.lengthMm * ruler.scalePxPerMm
        val heightPx = ruler.heightMm * ruler.scalePxPerMm

        val x = when (corner) {
            Corner.TOP_LEFT, Corner.BOTTOM_LEFT -> MARGIN
            Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT -> image.width - lengthPx - MARGIN
        }
        val y = when (corner) {
            Corner.TOP_LEFT, Corner.TOP_RIGHT -> MARGIN
            Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT -> image.height - heightPx - MARGIN
        }

        return ruler.copy(x = x, y = y)
    }

    fun fitToImage(ruler: RulerData, imageWidth: Int, imageHeight: Int): RulerData {
        val lengthPx = ruler.lengthMm * ruler.scalePxPerMm
        val heightPx = ruler.heightMm * ruler.scalePxPerMm

        val x = when {
            ruler.x + lengthPx + MARGIN > imageWidth -> (imageWidth - lengthPx - MARGIN).coerceAtLeast(MARGIN)
            ruler.x < MARGIN -> MARGIN
            else -> ruler.x
        }

        val y = when {
            ruler.y + heightPx + MARGIN > imageHeight -> (imageHeight - heightPx - MARGIN).coerceAtLeast(
                MARGIN
            )
            ruler.y < MARGIN -> MARGIN
            else -> ruler.y
        }

        return ruler.copy(x = x, y = y)
    }

}