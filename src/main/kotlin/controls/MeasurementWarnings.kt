package com.ykononenko.controls

/** Предупреждение перед входом в режим размерных линий. */
object MeasurementModeWarning : Warning() {

    override fun isTriggered(session: EditorSession): Boolean =
        session.showMeasurementModeWarning
}

/** Предупреждение перед удалением измерения. */
object MeasurementDeleteWarning : Warning() {

    override fun isTriggered(session: EditorSession): Boolean =
        session.showMeasurementDeleteWarning
}
