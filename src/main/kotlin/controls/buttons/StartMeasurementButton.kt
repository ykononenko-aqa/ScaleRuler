package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Переводит приложение в режим размерных линий. */
class StartMeasurementButton(
    size: DpSize = DpSize(240.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 10
) : StateButton(
    title = "Перейти в режим измерений",
    from = setOf(AppState.RULER_PLACED, AppState.NORTH_PLACED),
    to = AppState.MEASURING,
    size = size,
    row = row,
    order = order
) {

    override fun visibleWhen(session: EditorSession): Boolean = session.ruler != null

    override fun perform(session: EditorSession): Boolean {
        if (session.ruler == null) return false
        if (session.suppressWarnings) return true
        session.requestMeasurementMode()
        return false
    }
}
