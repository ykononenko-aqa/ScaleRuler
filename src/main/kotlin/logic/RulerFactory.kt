package com.ykononenko.logic

import com.ykononenko.model.RulerData
import androidx.compose.ui.graphics.ImageBitmap

object RulerFactory {

    fun create(
        image: ImageBitmap,
        scalePxPerMm: Float
    ): RulerData {
        val lengthPx = RulerData.DEFAULT_LENGTH_MM * scalePxPerMm
        val heightPx = RulerData.DEFAULT_HEIGHT_MM * scalePxPerMm
        val fontSize = (heightPx * 0.6f).coerceIn(10f, 36f)
        val mmLabelWidth = fontSize * 2f
        val margin = 10f

        return RulerData(
            x = image.width - lengthPx - margin - mmLabelWidth,
            y = image.height - heightPx - fontSize - 4f - margin,
            scalePxPerMm = scalePxPerMm
        )
    }
}