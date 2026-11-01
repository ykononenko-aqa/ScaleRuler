package com.ykononenko.controls.fields

import androidx.compose.ui.unit.DpSize
import com.ykononenko.controls.Control
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession

/**
 * Ползунок нижней панели.
 */
class SliderControl(
    override val title: String,
    override val size: DpSize,
    override val row: ControlRow,
    override val order: Int,
    private val visible: (EditorSession) -> Boolean,
    val value: (EditorSession) -> Float,
    val range: ClosedFloatingPointRange<Float>,
    val onValueChange: (EditorSession, Float) -> Unit,
    val valueLabel: (Float) -> String
) : Control {

    override fun isVisible(session: EditorSession): Boolean = visible(session)
}
