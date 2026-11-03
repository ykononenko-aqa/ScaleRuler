package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/**
 * Фиксирует первую или вторую точку размерной линии.
 *
 * @param number номер точки: `1` или `2`
 */
class SaveMeasurementPointButton(
    private val number: Int,
    size: DpSize = DpSize(180.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 30
) : StateButton(
    title = if (number == 1) "Сохранить первую точку" else "Сохранить вторую точку",
    from = setOf(AppState.MEASURING),
    size = size,
    row = row,
    order = order
) {

    override fun visibleWhen(session: EditorSession): Boolean =
        if (number == 1) {
            session.measurements.point1 == null
        } else {
            session.measurements.point1 != null
        }

    override fun perform(session: EditorSession): Boolean =
        session.commitMeasurementPoint(number)
}
