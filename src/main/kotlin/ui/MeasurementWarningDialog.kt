package com.ykononenko.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

/**
 * Предупреждение перед входом в режим размерных линий.
 *
 * @param onConfirm подтверждение входа
 * @param onDismiss отказ от входа
 */
@Composable
fun MeasurementModeWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Перейти в режим измерений?") },
        text = {
            Text(
                "В этом режиме можно расставлять пары точек. Между ними появятся " +
                    "линии с подписями длины; они сохранятся вместе с изображением."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Перейти")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Предупреждение перед удалением измерения.
 *
 * @param inProgress `true`, если удаляется незавершённая точка, а не готовая линия
 * @param onConfirm подтверждение удаления
 * @param onDismiss отказ от удаления
 */
@Composable
fun MeasurementDeleteWarningDialog(
    inProgress: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (inProgress) {
        "Удалить незавершённую точку? Она не будет добавлена на изображение."
    } else {
        "Удалить последнюю размерную линию с подписью? Это действие нельзя отменить."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить?") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
