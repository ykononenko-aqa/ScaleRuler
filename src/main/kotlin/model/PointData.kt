package com.ykononenko.model

import androidx.compose.ui.geometry.Offset

/**
 * Набор точек, расставляемых пользователем для калибровки масштаба.
 *
 * @property pendingPoint временная точка в координатах исходного изображения
 *   (до подтверждения)
 * @property point1 первая точка в координатах исходного изображения
 * @property point2 вторая точка в координатах исходного изображения
 */
data class AppPoints(
    val pendingPoint: Offset? = null,
    val point1: Offset? = null,
    val point2: Offset? = null
)
