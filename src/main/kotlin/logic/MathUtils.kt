package com.ykononenko.logic

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

fun distanceBetween(p1: Offset, p2: Offset): Float {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    return sqrt(dx * dx + dy * dy)
}