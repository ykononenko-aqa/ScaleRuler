package com.ykononenko.controls.fields

import androidx.compose.ui.unit.DpSize
import com.ykononenko.controls.Control
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession

/**
 * Текстовое поле нижней панели.
 *
 * Значение хранится в [EditorSession], элемент лишь описывает, как его читать,
 * фильтровать и изменять.
 */
class TextFieldControl(
    override val title: String,
    override val size: DpSize,
    override val row: ControlRow,
    override val order: Int,
    private val visible: (EditorSession) -> Boolean,
    val value: (EditorSession) -> String,
    val filter: (String) -> Boolean,
    val onValueChange: (EditorSession, String) -> Unit
) : Control {

    override fun isVisible(session: EditorSession): Boolean = visible(session)
}
