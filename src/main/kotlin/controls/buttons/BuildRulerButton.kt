package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Рассчитывает масштаб и создаёт линейку по введённым параметрам. */
class BuildRulerButton(
    size: DpSize = DpSize(220.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 40
) : StateButton(
    title = "Рассчитать и вывести линейку",
    from = setOf(AppState.POINTS_SET),
    to = AppState.RULER_PLACED,
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean =
        session.buildRulerFromInputs()
}
