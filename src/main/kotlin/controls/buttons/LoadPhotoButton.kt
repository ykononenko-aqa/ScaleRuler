package com.ykononenko.controls.buttons

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.StateButton
import com.ykononenko.state.AppState

/**
 * Загружает новое фото и сбрасывает предыдущий результат.
 *
 * Используется и в нижней панели, и в верхней: из верхней панели загрузка
 * доступна в любом состоянии.
 */
class LoadPhotoButton(
    size: DpSize = DpSize(160.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 10
) : StateButton(
    title = "Загрузить фото",
    from = setOf(AppState.IDLE),
    to = AppState.PHOTO_LOADED,
    size = size,
    row = row,
    order = order,
    enforceFrom = false
) {

    override fun perform(session: EditorSession): Boolean {
        val image = session.fileService.openImage() ?: return false
        session.resetForNewPhoto(image)
        return true
    }
}
