package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Подтверждает добавление указателя на север. */
class ConfirmAddNorthButton(
    size: DpSize = DpSize(100.dp, 40.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 0
) : StateButton(
    title = "Добавить",
    from = setOf(AppState.PHOTO_LOADED, AppState.RULER_PLACED),
    to = AppState.NORTH_PLACED,
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean = session.createNorthArrow()
}
