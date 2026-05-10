package com.ykononenko.model

data class RulerData(
    val x: Float,              // относительно исходного изображения
    val y: Float,              // относительно исходного изображения
    val scalePxPerMm: Float    // масштаб (пикселей изображения на 1 мм)
)