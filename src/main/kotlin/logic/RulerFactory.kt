package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.model.Corner
import com.ykononenko.model.RulerData

object RulerFactory {

    private const val MARGIN = 10f  // отступ от края изображения

    fun create(image: ImageBitmap, scalePxPerMm: Float): RulerData {
        val lengthPx = RulerData.DEFAULT_LENGTH_MM * scalePxPerMm
        val heightPx = RulerData.DEFAULT_HEIGHT_MM * scalePxPerMm
        val fontSize = labelFontSize(heightPx)
        val bottomMargin = fontSize + 4f

        return RulerData(
            x = image.width - lengthPx - MARGIN,
            y = image.height - heightPx - bottomMargin - MARGIN,
            scalePxPerMm = scalePxPerMm
        )
    }

    fun moveToCorner(ruler: RulerData, image: ImageBitmap, corner: Corner): RulerData {
        val lengthPx = ruler.lengthMm * ruler.scalePxPerMm
        val heightPx = ruler.heightMm * ruler.scalePxPerMm
        val fontSize = labelFontSize(heightPx)
        val bottomMargin = fontSize + 4f

        val x = when (corner) {
            Corner.TOP_LEFT, Corner.BOTTOM_LEFT -> MARGIN
            Corner.TOP_RIGHT, Corner.BOTTOM_RIGHT -> image.width - lengthPx - MARGIN
        }
        val y = when (corner) {
            Corner.TOP_LEFT, Corner.TOP_RIGHT -> MARGIN
            Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT -> image.height - heightPx - bottomMargin - MARGIN
        }

        return ruler.copy(x = x, y = y)
    }

    private fun labelFontSize(heightPx: Float): Float {
        return (heightPx * 0.6f).coerceIn(10f, 36f)
    }
}