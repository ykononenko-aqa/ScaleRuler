package com.ykononenko.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.ykononenko.controls.EditorSession
import com.ykononenko.model.NorthArrowData
import com.ykononenko.state.AppState
import kotlin.math.PI
import kotlin.math.atan2

/**
 * Режим обработки жеста в состояниях редактирования.
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
 * обрабатываются одиночные тапы. Клик сразу переводится в координаты
 * изображения и сохраняется как временная точка.
 *
 * В состоянии [AppState.RULER_PLACED] без указателя перетаскивание перемещает
 * линейку. При наличии указателя, а также в [AppState.NORTH_PLACED],
 * перетаскивание перемещает или поворачивает указатель.
 */
@Composable
fun Modifier.canvasInput(
    session: EditorSession,
    layout: ImageLayout?
): Modifier {
    val currentLayout by rememberUpdatedState(layout)

    return this.pointerInput(session.appState, session.northArrow != null) {
        when (session.appState) {
            AppState.AWAITING_POINT_1, AppState.AWAITING_POINT_2 -> {
                detectTapGestures { tapOffset ->
                    val current = currentLayout ?: return@detectTapGestures
                    session.setPendingPoint(canvasToImage(tapOffset, current))
                }
            }

            AppState.RULER_PLACED, AppState.NORTH_PLACED -> {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent()
                        val dc = down.changes.firstOrNull() ?: continue
                        if (!dc.pressed) continue
                        dc.consume()

                        val current = currentLayout
                        val arrow = session.northArrow
                        val image = session.image
                        val mode = resolveMode(
                            arrow = arrow,
                            layout = current,
                            canvasPosition = dc.position,
                            imageWidth = image?.width ?: 0,
                            imageHeight = image?.height ?: 0
                        )
                        if (mode == DragMode.NORTH_ROTATE && arrow != null && current != null) {
                            session.rotateNorth(
                                angleTo(arrow, canvasToImage(dc.position, current))
                            )
                        }

                        var prev = dc.position
                        do {
                            val ev = awaitPointerEvent()
                            val ch = ev.changes.firstOrNull() ?: break
                            val deltaCanvas = Offset(ch.position.x - prev.x, ch.position.y - prev.y)
                            prev = ch.position
                            ch.consume()

                            val current = currentLayout
                            when (mode) {
                                DragMode.RULER -> {
                                    if (current != null && current.scale > 0f) {
                                        session.moveRulerBy(
                                            Offset(
                                                deltaCanvas.x / current.scale,
                                                deltaCanvas.y / current.scale
                                            )
                                        )
                                    }
                                }

                                DragMode.NORTH_MOVE -> {
                                    if (current != null && current.scale > 0f) {
                                        session.moveNorthBy(
                                            Offset(
                                                deltaCanvas.x / current.scale,
                                                deltaCanvas.y / current.scale
                                            )
                                        )
                                    }
                                }

                                DragMode.NORTH_ROTATE -> {
                                    val a = session.northArrow
                                    if (current != null && a != null) {
                                        session.rotateNorth(
                                            angleTo(a, canvasToImage(ch.position, current))
                                        )
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
 */
private fun angleTo(arrow: NorthArrowData, imagePosition: Offset): Float {
    val dx = imagePosition.x - arrow.centerX
    val dy = imagePosition.y - arrow.centerY
    val angle = atan2(dx.toDouble(), -dy.toDouble()).toFloat()
    return if (angle < 0f) angle + (2f * PI).toFloat() else angle
}
