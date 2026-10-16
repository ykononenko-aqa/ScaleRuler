package com.ykononenko.model

import androidx.compose.ui.geometry.Offset

/**
 * Набор точек, расставляемых пользователем для калибровки масштаба.
 *
 * @property tempMarker временная точка в координатах Canvas (до подтверждения)
 * @property point1 первая точка в координатах исходного изображения
 * @property point2 вторая точка в координатах исходного изображения
 */
data class AppPoints(
    val tempMarker: Offset? = null,
    val point1: Offset? = null,
    val point2: Offset? = null
)
