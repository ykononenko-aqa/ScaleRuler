package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Удаляет незавершённую точку или последнюю размерную линию. */
class DeleteMeasurementButton(
    size: DpSize = DpSize(140.dp, 48.dp),
    row: ControlRow = ControlRow.TOOLS,
    order: Int = 30
) : StateButton(
    title = "Удалить",
    from = setOf(AppState.MEASURING),
    size = size,
    row = row,
    order = order
) {

    override fun visibleWhen(session: EditorSession): Boolean =
        session.hasMeasurementToDelete

    override fun perform(session: EditorSession): Boolean {
        if (!session.hasMeasurementToDelete) return false
        if (session.suppressWarnings) {
            session.removeLastMeasurement()
            return true
        }
        session.requestDeleteMeasurement()
        return false
    }
}
