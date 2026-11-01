package com.ykononenko.model

/**
 * Точка в координатах исходного изображения.
 *
 * @property x координата X в пикселях изображения
 * @property y координата Y в пикселях изображения
 */
data class Point(
    val x: Float,
    val y: Float
)

/**
 * Набор точек, расставляемых пользователем для калибровки масштаба.
 *
 * @property pendingPoint временная точка в координатах исходного изображения
 *   (до подтверждения)
 * @property point1 первая точка в координатах исходного изображения
 * @property point2 вторая точка в координатах исходного изображения
 */
data class AppPoints(
    val pendingPoint: Point? = null,
    val point1: Point? = null,
    val point2: Point? = null
)
