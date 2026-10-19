package com.ykononenko.ui

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

/**
 * Предупреждающий диалог перед добавлением указателя на север.
 *
 * Текст зависит от того, размещена ли уже линейка. Если линейка есть, сообщает,
 * что изменить её параметры и положение будет нельзя; если линейки нет — что
 * после добавления указателя добавить линейку будет невозможно.
 *
 * @param rulerPlaced `true`, если линейка уже размещена
 * @param onConfirm подтверждение добавления указателя
 * @param onDismiss отказ от добавления
 */
@Composable
fun NorthWarningDialog(
    rulerPlaced: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (rulerPlaced) {
        "После добавления указателя изменить параметры и положение линейки " +
            "будет невозможно. Указатель появится в правом верхнем углу; его " +
            "можно перемещать, поворачивать и менять его размер и цвет."
    } else {
        "После добавления указателя добавить линейку будет невозможно. " +
            "Указатель появится в правом верхнем углу; его можно перемещать, " +
            "поворачивать и менять его размер и цвет."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить указатель?") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
