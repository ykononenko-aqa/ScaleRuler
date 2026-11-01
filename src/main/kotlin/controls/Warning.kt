package com.ykononenko.controls

/**
 * Информационное предупреждение приложения.
 *
 * Общая настройка [EditorSession.suppressWarnings] отключает все предупреждения.
 * Конкретное условие показа задаёт [isTriggered].
 */
abstract class Warning {

    /** Нужно ли показывать предупреждение в текущем состоянии. */
    fun shouldShow(session: EditorSession): Boolean =
        !session.suppressWarnings && isTriggered(session)

    /** Условие возникновения конкретного предупреждения. */
    protected abstract fun isTriggered(session: EditorSession): Boolean
}
