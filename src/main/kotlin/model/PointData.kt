package com.ykononenko.model

import androidx.compose.ui.geometry.Offset

data class AppPoints(
    val tempMarker: Offset? = null,       // относительно Canvas (временно)
    val point1: Offset? = null,           // относительно исходного изображения
    val point2: Offset? = null            // относительно исходного изображения
)