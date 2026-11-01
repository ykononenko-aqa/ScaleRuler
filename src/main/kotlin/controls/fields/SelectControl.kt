package com.ykononenko.controls.fields

import androidx.compose.ui.unit.DpSize
import com.ykononenko.controls.Control
import com.ykononenko.controls.ControlRow
import com.ykononenko.controls.EditorSession

/**
 * Выпадающий список нижней панели.
 *
 * @param T тип выбираемого значения
 */
class SelectControl<T>(
    override val title: String,
    override val size: DpSize,
    override val row: ControlRow,
    override val order: Int,
    private val visible: (EditorSession) -> Boolean,
    val options: List<T>,
    val optionLabel: (T) -> String,
    val selected: (EditorSession) -> T,
    val onSelect: (EditorSession, T) -> Unit,
    val buttonLabel: (EditorSession) -> String = { title }
) : Control {

    override fun isVisible(session: EditorSession): Boolean = visible(session)

    /** Возвращает выбранное значение без знания конкретного типа. */
    fun selectedOption(session: EditorSession): Any? = selected(session)

    /** Возвращает подпись варианта без знания конкретного типа. */
    fun labelOf(option: Any): String {
        @Suppress("UNCHECKED_CAST")
        return optionLabel(option as T)
    }

    /** Выбирает вариант без знания конкретного типа. */
    fun select(session: EditorSession, option: Any) {
        @Suppress("UNCHECKED_CAST")
        onSelect(session, option as T)
    }
}
