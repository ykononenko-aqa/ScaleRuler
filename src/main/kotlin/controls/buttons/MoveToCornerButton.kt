package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.model.Corner
import com.ykononenko.state.AppState

/**
 * Перемещает указатель (если он есть) или линейку в выбранный угол.
 *
 * @param corner целевой угол изображения
 */
class MoveToCornerButton(
    private val corner: Corner,
    row: ControlRow = ControlRow.TOOLS,
    order: Int = 0
) : StateButton(
    title = corner.label,
    from = setOf(AppState.RULER_PLACED, AppState.NORTH_PLACED),
    size = DpSize(24.dp, 24.dp),
    row = row,
    order = order
) {

    override fun visibleWhen(session: EditorSession): Boolean =
        session.ruler != null || session.northArrow != null

    override fun perform(session: EditorSession): Boolean {
        session.moveSelectionToCorner(corner)
        return true
    }
}
