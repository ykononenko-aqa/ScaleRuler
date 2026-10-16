package com.ykononenko.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.ykononenko.state.AppState

/**
 * Подключает обработку ввода к Canvas в зависимости от текущего состояния.
 *
 * В состояниях [AppState.AWAITING_POINT_1] и [AppState.AWAITING_POINT_2]
 * обрабатываются одиночные тапы (постановка точек), в состоянии
 * [AppState.RULER_PLACED] — перетаскивание (перемещение линейки).
 *
 * @param appState текущее состояние сценария
 * @param onClick обработчик тапа в координатах Canvas
 * @param onDrag обработчик перетаскивания; принимает смещение в координатах Canvas
 * @return модификатор с подключённым вводом
 */
fun androidx.compose.ui.Modifier.canvasInput(
    appState: AppState,
    onClick: (Offset) -> Unit,
    onDrag: (Offset) -> Unit
) = this.pointerInput(appState) {
    when (appState) {
        AppState.AWAITING_POINT_1, AppState.AWAITING_POINT_2 -> {
            detectTapGestures { tapOffset ->
                onClick(tapOffset)
            }
        }
        AppState.RULER_PLACED -> {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent()
                    val dc = down.changes.firstOrNull() ?: continue
                    if (!dc.pressed) continue
                    dc.consume()
                    var prev = dc.position
                    do {
                        val ev = awaitPointerEvent()
                        val ch = ev.changes.firstOrNull() ?: break
                        val d = Offset(ch.position.x - prev.x, ch.position.y - prev.y)
                        prev = ch.position
                        ch.consume()
                        onDrag(d)
                    } while (ch.pressed)
                }
            }
        }
        else -> {}
    }
}
