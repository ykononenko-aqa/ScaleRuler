package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Подтверждает вход в режим размерных линий. */
class ConfirmStartMeasurementButton(
    size: DpSize = DpSize(100.dp, 40.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 0
) : StateButton(
    title = "Перейти",
    from = setOf(AppState.RULER_PLACED, AppState.NORTH_PLACED),
    to = AppState.MEASURING,
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean {
        if (session.ruler == null) return false
        session.dismissMeasurementModeWarning()
        return true
    }
}
