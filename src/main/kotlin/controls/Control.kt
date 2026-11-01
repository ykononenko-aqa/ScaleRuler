package com.ykononenko.controls

import androidx.compose.ui.unit.DpSize

/**
 * Строка нижней панели, в которой размещается элемент управления.
 */
enum class ControlRow {

    /** Строка параметров масштаба и линейки. */
    SETTINGS,

    /** Строка инструментов работы с линейкой и указателем. */
    TOOLS,

    /** Нижняя строка с основной кнопкой. */
    PRIMARY
}

/**
 * Элемент управления нижней панели.
 *
 * Размер и порядок задаются моделью, а фактические координаты вычисляет
 * Compose при раскладке.
 */
interface Control {

    /** Подпись кнопки или поля. */
    val title: String

    /** Предпочтительный размер элемента. */
    val size: DpSize

    /** Строка панели. */
    val row: ControlRow

    /** Порядок внутри строки. */
    val order: Int

    /** Виден ли элемент в текущем состоянии. */
    fun isVisible(session: EditorSession): Boolean

    /** Доступен ли элемент в текущем состоянии. */
    fun isEnabled(session: EditorSession): Boolean = true
}
