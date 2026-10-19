package com.ykononenko.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.ykononenko.model.NorthArrowData
import com.ykononenko.state.AppState
import kotlin.math.PI
import kotlin.math.atan2

/**
 * Режим обработки жеста в состоянии [AppState.RULER_PLACED].
 */
private enum class DragMode {

    /** Перетаскивание линейки. */
    RULER,

    /** Перемещение указателя на север за центральный круг. */
    NORTH_MOVE,

    /** Поворот указателя нажатием во внешней невидимой окружности. */
    NORTH_ROTATE,

    /** Жест вне активных элементов — ничего не делает. */
    NONE
}

/**
 * Подключает обработку ввода к Canvas в зависимости от текущего состояния.
 *
 * В состояниях [AppState.AWAITING_POINT_1] и [AppState.AWAITING_POINT_2]
 * обрабатываются одиночные тапы (постановка точек). В состоянии
 * [AppState.RULER_PLACED] без указателя на север перетаскивание перемещает
 * линейку. В состоянии [AppState.NORTH_PLACED] (а также в [AppState.RULER_PLACED]
 * при наличии указателя) перетаскивание перемещает или поворачивает указатель:
 * нажатие внутри центрального круга перемещает, внутри внешней невидимой
 * окружности — поворачивает.
 *
 * @param appState текущее состояние сценария
 * @param northArrow указатель на север или `null`
 * @param imageWidth ширина изображения в пикселях
 * @param imageHeight высота изображения в пикселях
 * @param layout текущая геометрия отображения или `null`
 * @param onClick обработчик тапа в координатах Canvas
 * @param onRulerDrag обработчик перетаскивания линейки; смещение в координатах Canvas
 * @param onNorthMove обработчик перемещения указателя; смещение в пикселях изображения
 * @param onNorthRotate обработчик поворота указателя; угол в радианах, `0` — вверх
 * @return модификатор с подключённым вводом
 */
@Composable
fun Modifier.canvasInput(
    appState: AppState,
    northArrow: NorthArrowData?,
    imageWidth: Int,
    imageHeight: Int,
    layout: ImageLayout?,
    onClick: (Offset) -> Unit,
    onRulerDrag: (Offset) -> Unit,
    onNorthMove: (Offset) -> Unit,
    onNorthRotate: (Float) -> Unit
): Modifier {
    val currentNorth by rememberUpdatedState(northArrow)
    val currentLayout by rememberUpdatedState(layout)
    val currentImageWidth by rememberUpdatedState(imageWidth)
    val currentImageHeight by rememberUpdatedState(imageHeight)

    return this.pointerInput(appState, northArrow != null) {
        when (appState) {
            AppState.AWAITING_POINT_1, AppState.AWAITING_POINT_2 -> {
                detectTapGestures { tapOffset ->
                    onClick(tapOffset)
                }
            }
            AppState.RULER_PLACED, AppState.NORTH_PLACED -> {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent()
                        val dc = down.changes.firstOrNull() ?: continue
                        if (!dc.pressed) continue
                        dc.consume()

                        val currentLayoutValue = currentLayout
                        val arrow = currentNorth
                        val mode = resolveMode(
                            arrow = arrow,
                            layout = currentLayoutValue,
                            canvasPosition = dc.position,
                            imageWidth = currentImageWidth,
                            imageHeight = currentImageHeight
                        )
                        if (mode == DragMode.NORTH_ROTATE && arrow != null && currentLayoutValue != null) {
                            onNorthRotate(angleTo(arrow, canvasToImage(dc.position, currentLayoutValue)))
                        }

                        var prev = dc.position
                        do {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull() ?: break
                            val d = Offset(ch.position.x - prev.x, ch.position.y - prev.y)
                            prev = ch.position
                            ch.consume()

                            when (mode) {
                                DragMode.RULER -> onRulerDrag(d)
                                DragMode.NORTH_MOVE -> {
                                    val l = currentLayout
                                    if (l != null) {
                                        onNorthMove(Offset(d.x / l.scale, d.y / l.scale))
                                    }
                                }
                                DragMode.NORTH_ROTATE -> {
                                    val l = currentLayout
                                    val a = currentNorth
                                    if (l != null && a != null) {
                                        onNorthRotate(angleTo(a, canvasToImage(ch.position, l)))
                                    }
                                }
                                DragMode.NONE -> {}
                            }
                        } while (ch.pressed)
                    }
                }
            }
            else -> {}
        }
    }
}

/**
 * Определяет режим жеста по точке нажатия.
 *
 * @param arrow указатель на север или `null`
 * @param layout геометрия отображения или `null`
 * @param canvasPosition точка нажатия в координатах Canvas
 * @param imageWidth ширина изображения в пикселях
 * @param imageHeight высота изображения в пикселях
 * @return выбранный режим
 */
private fun resolveMode(
    arrow: NorthArrowData?,
    layout: ImageLayout?,
    canvasPosition: Offset,
    imageWidth: Int,
    imageHeight: Int
): DragMode {
    if (arrow == null || layout == null) return DragMode.RULER

    val center = Offset(arrow.centerX, arrow.centerY)
    val imagePosition = canvasToImage(canvasPosition, layout)
    val radius = arrow.radiusPx(imageWidth, imageHeight)
    val centerRadius = radius * NorthArrowData.CENTER_CIRCLE_FRACTION
    val distance = (imagePosition - center).getDistance()

    return when {
        distance <= centerRadius -> DragMode.NORTH_MOVE
        distance <= radius -> DragMode.NORTH_ROTATE
        else -> DragMode.NONE
    }
}

/**
 * Вычисляет угол указателя на точку: `0` — вверх (север), по часовой стрелке.
 *
 * @param arrow указатель, задающий центр
 * @param imagePosition точка в координатах изображения
 * @return угол в радианах в диапазоне `[0, 2π)`
 */
private fun angleTo(arrow: NorthArrowData, imagePosition: Offset): Float {
    val dx = imagePosition.x - arrow.centerX
    val dy = imagePosition.y - arrow.centerY
    val angle = atan2(dx.toDouble(), -dy.toDouble()).toFloat()
    return if (angle < 0f) angle + (2f * PI).toFloat() else angle
}
