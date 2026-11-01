package com.ykononenko

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.MeasurementDeleteWarning
import com.ykononenko.controls.MeasurementModeWarning
import com.ykononenko.controls.NorthArrowWarning
import com.ykononenko.controls.buttons.ConfirmAddNorthButton
import com.ykononenko.controls.buttons.ConfirmDeleteMeasurementButton
import com.ykononenko.controls.buttons.ConfirmStartMeasurementButton
import com.ykononenko.controls.buttons.LoadPhotoButton
import com.ykononenko.ui.BatchDialog
import com.ykononenko.ui.BottomBar
import com.ykononenko.ui.MainArea
import com.ykononenko.ui.MeasurementDeleteWarningDialog
import com.ykononenko.ui.MeasurementModeWarningDialog
import com.ykononenko.ui.NorthWarningDialog
import com.ykononenko.ui.SwingImageFileService
import com.ykononenko.ui.TopBar

/**
 * Корневой composable приложения.
 *
 * Создаёт [EditorSession] и передаёт его панелям и области Canvas. Вся
 * бизнес-логика находится в действиях кнопок и методах сессии.
 *
 * @param onExit обработчик выхода из приложения
 */
@Composable
fun App(onExit: () -> Unit = {}) {
    val session = remember { EditorSession(SwingImageFileService()) }
    val loadPhotoButton = remember { LoadPhotoButton() }
    val confirmAddNorthButton = remember { ConfirmAddNorthButton() }
    val confirmStartMeasurementButton = remember { ConfirmStartMeasurementButton() }
    val confirmDeleteMeasurementButton = remember { ConfirmDeleteMeasurementButton() }

    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                onLoadPhoto = { loadPhotoButton.action(session) },
                onExit = onExit,
                suppressWarnings = session.suppressWarnings,
                onSuppressWarningsChange = session::updateSuppressWarnings
            )

            MainArea(
                session = session,
                modifier = Modifier.weight(1f)
            )

            BottomBar(session = session)
        }

        if (NorthArrowWarning.shouldShow(session)) {
            NorthWarningDialog(
                rulerPlaced = session.ruler != null,
                onConfirm = { confirmAddNorthButton.action(session) },
                onDismiss = { session.dismissNorthWarning() }
            )
        }

        if (MeasurementModeWarning.shouldShow(session)) {
            MeasurementModeWarningDialog(
                onConfirm = { confirmStartMeasurementButton.action(session) },
                onDismiss = { session.dismissMeasurementModeWarning() }
            )
        }

        if (MeasurementDeleteWarning.shouldShow(session)) {
            MeasurementDeleteWarningDialog(
                inProgress = session.measurements.pendingPoint != null ||
                    session.measurements.point1 != null,
                onConfirm = { confirmDeleteMeasurementButton.action(session) },
                onDismiss = { session.dismissDeleteMeasurementWarning() }
            )
        }

        if (session.showBatchDialog && (session.ruler != null || session.northArrow != null)) {
            BatchDialog(
                ruler = session.ruler,
                northArrow = session.northArrow,
                measurements = session.measurements.items,
                unit = session.unit,
                onDismiss = { session.dismissBatch() }
            )
        }
    }
}
