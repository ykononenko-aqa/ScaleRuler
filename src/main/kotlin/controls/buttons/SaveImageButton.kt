package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Сохраняет изображение с линейкой и указателем, не меняя состояние. */
class SaveImageButton(
    size: DpSize = DpSize(180.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 50
) : StateButton(
    title = "Сохранить изображение",
    from = setOf(AppState.RULER_PLACED, AppState.NORTH_PLACED, AppState.MEASURING),
    size = size,
    row = row,
    order = order
) {

    override fun perform(session: EditorSession): Boolean = session.saveImage()
}
