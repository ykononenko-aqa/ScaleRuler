package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/** Переводит приложение в режим расстановки калибровочных точек. */
class StartPointPlacementButton : StateButton(
    title = "Перейти в режим расстановки точек",
    from = setOf(AppState.PHOTO_LOADED),
    to = AppState.AWAITING_POINT_1,
    size = DpSize(240.dp, 48.dp),
    order = 0
) {

    override fun perform(session: EditorSession): Boolean = session.image != null
}
