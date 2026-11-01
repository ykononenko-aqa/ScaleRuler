package com.ykononenko.controls

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * Общий родитель всех кнопок приложения.
 *
 * Конкретная кнопка переопределяет [action] и выполняет действие, которое
 * инициируется нажатием.
 */
abstract class ButtonControl(
    override val title: String,
    override val size: DpSize = DpSize(160.dp, 48.dp),
    override val row: ControlRow = ControlRow.PRIMARY,
    override val order: Int = 0
) : Control {

    /** Подпись кнопки с учётом текущего состояния. */
    open fun displayTitle(session: EditorSession): String = title

    /** Выполняет действие кнопки. */
    abstract fun action(session: EditorSession)
}
