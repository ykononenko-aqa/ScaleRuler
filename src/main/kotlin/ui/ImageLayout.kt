package com.ykononenko.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap

data class ImageLayout(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val displayWidth: Float,
    val displayHeight: Float
)

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

fun canvasToImage(canvasOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        (canvasOffset.x - layout.offsetX) / layout.scale,
        (canvasOffset.y - layout.offsetY) / layout.scale
    )
}

fun imageToCanvas(imageOffset: Offset, layout: ImageLayout): Offset {
    return Offset(
        imageOffset.x * layout.scale + layout.offsetX,
        imageOffset.y * layout.scale + layout.offsetY
    )
}