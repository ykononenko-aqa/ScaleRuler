package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Открывает диалог пакетной обработки. */
class BatchButton(
    size: DpSize = DpSize(180.dp, 48.dp),
    row: ControlRow = ControlRow.TOOLS,
    order: Int = 40
) : StateButton(
    title = "Пакетная обработка",
    from = setOf(AppState.RULER_PLACED, AppState.NORTH_PLACED),
    size = size,
    row = row,
    order = order
) {

    override fun visibleWhen(session: EditorSession): Boolean =
        session.ruler != null || session.northArrow != null

    override fun perform(session: EditorSession): Boolean {
        session.requestBatch()
        return true
    }
}
