package com.ykononenko.controls

/** Предупреждение перед добавлением указателя на север. */
object NorthArrowWarning : Warning() {

    override fun isTriggered(session: EditorSession): Boolean =
        session.showNorthWarning
}
