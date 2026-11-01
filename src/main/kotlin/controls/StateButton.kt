package com.ykononenko.controls

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.state.AppState

/**
 * Кнопка, которая выполняется в определённых состояниях и может выполнять
 * переход в новое состояние.
 *
 * Переход хранится в самой кнопке: [from] — допустимые состояния,
 * [to] — целевое состояние (или `null`, если перехода нет).
 *
 * @param enforceFrom требует ли кнопка нахождения в одном из [from]; для
 *   загрузки фото из верхней панели проверка отключается
 */
abstract class StateButton(
    title: String,
    private val from: Set<AppState>,
    private val to: AppState? = null,
    size: DpSize = DpSize(160.dp, 48.dp),
    row: ControlRow = ControlRow.PRIMARY,
    order: Int = 0,
    private val enforceFrom: Boolean = true
) : ButtonControl(title, size, row, order) {

    final override fun isVisible(session: EditorSession): Boolean =
        session.appState in from && visibleWhen(session)

    /** Дополнительное условие видимости. */
    protected open fun visibleWhen(session: EditorSession): Boolean = true

    final override fun action(session: EditorSession) {
        if (enforceFrom && session.appState !in from) return
        if (!perform(session)) return
        to?.let(session::transitionTo)
    }

    /**
     * Выполняет действие кнопки.
     *
     * @return `true`, если действие успешно и переход [to] нужно выполнить
     */
    protected abstract fun perform(session: EditorSession): Boolean
}
