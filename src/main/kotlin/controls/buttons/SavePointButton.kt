package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/**
 * Фиксирует ожидающую точку.
 *
 * @param number номер точки: `1` или `2`
 */
class SavePointButton(
    private val number: Int,
    size: DpSize = DpSize(180.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 30
) : StateButton(
    title = if (number == 1) "Сохранить первую точку" else "Сохранить вторую точку",
    from = setOf(
        if (number == 1) AppState.AWAITING_POINT_1 else AppState.AWAITING_POINT_2
    ),
    to = if (number == 1) AppState.AWAITING_POINT_2 else AppState.POINTS_SET,
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean =
        session.commitPendingPoint(number)
}
