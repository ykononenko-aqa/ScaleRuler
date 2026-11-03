package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Подтверждает удаление измерения. */
class ConfirmDeleteMeasurementButton(
    size: DpSize = DpSize(100.dp, 40.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 0
) : StateButton(
    title = "Удалить",
    from = setOf(AppState.MEASURING),
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean {
        if (!session.hasMeasurementToDelete) {
            session.dismissDeleteMeasurementWarning()
            return false
        }
        session.confirmDeleteMeasurement()
        return true
    }
}
