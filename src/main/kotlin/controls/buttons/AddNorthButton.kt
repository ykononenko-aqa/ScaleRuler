package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/**
 * Открывает предупреждение перед добавлением указателя на север.
 *
 * @param from состояния, в которых кнопка отображается
 */
class AddNorthButton(
    from: Set<AppState>,
    size: DpSize = DpSize(180.dp, 48.dp),
    row: ControlRow = ControlRow.TOOLS,
    order: Int = 50
) : StateButton(
    title = "Добавить указатель",
    from = from,
    to = AppState.NORTH_PLACED,
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean {
        if (session.suppressWarnings) {
            return session.createNorthArrow()
        }
        session.requestAddNorth()
        return false
    }
}
