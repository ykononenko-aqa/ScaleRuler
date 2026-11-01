package com.ykononenko.logic

import com.ykononenko.model.Point
import kotlin.math.sqrt

/**
 * Вычисляет евклидово расстояние между двумя точками.
 *
 * @param p1 первая точка
 * @param p2 вторая точка
 * @return расстояние между точками в тех же единицах, что и их координаты
 */
fun distanceBetween(p1: Point, p2: Point): Float {
    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    return sqrt(dx * dx + dy * dy)
}
