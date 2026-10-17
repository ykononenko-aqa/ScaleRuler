package com.ykononenko.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ykononenko.logic.BatchProcessor
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

/**
 * Диалог пакетной обработки: выбор папок и запуск нанесения линейки на все фото.
 *
 * Обработка выполняется в фоновом потоке ([Dispatchers.IO]), прогресс
 * отображается по ходу выполнения.
 *
 * @param ruler линейка, задающая масштаб для всех изображений
 * @param unit единица измерения для подписей линейки
 * @param onDismiss закрытие диалога
 */
@Composable
fun BatchDialog(
    ruler: RulerData,
    unit: LengthUnit,
    onDismiss: () -> Unit
) {
    var sourceDir by remember { mutableStateOf<File?>(null) }
    var targetDir by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0) }
    var total by remember { mutableStateOf(0) }
    var currentFile by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            modifier = Modifier.width(400.dp).padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Пакетная обработка", fontSize = 18.sp, style = MaterialTheme.typography.h6)

                // Папка-источник
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Источник: ", fontSize = 14.sp)
                    Text(
                        text = sourceDir?.absolutePath ?: "не выбрана",
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Button(onClick = {
                    sourceDir = chooseDirectory("Выберите папку с фотографиями")
                }) {
                    Text("Выбрать папку-источник")
                }

                // Папка-приёмник
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Приёмник: ", fontSize = 14.sp)
                    Text(
                        text = targetDir?.absolutePath ?: "не выбрана",
                        fontSize = 12.sp,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
                Button(onClick = {
                    targetDir = chooseDirectory("Выберите папку для сохранения")
                }) {
                    Text("Выбрать папку-приёмник")
                }

                // Прогресс
                if (isProcessing) {
                    Text("$currentFile", fontSize = 12.sp)
                    LinearProgressIndicator(
                        progress = if (total > 0) progress.toFloat() / total else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("$progress из $total", fontSize = 12.sp)
                }

                // Сообщение о результате
                resultMessage?.let {
                    Text(it, fontSize = 14.sp, color = MaterialTheme.colors.primary)
                }

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isProcessing) {
                        Button(onClick = { /* нельзя отменить */ }, enabled = false) {
                            Text("Выполняется...")
                        }
                    } else {
                        if (resultMessage != null) {
                            TextButton(onClick = onDismiss) {
                                Text("Закрыть")
                            }
                        } else {
                            TextButton(onClick = onDismiss) {
                                Text("Отмена")
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val src = sourceDir
                                    val tgt = targetDir
                                    if (src != null && tgt != null) {
                                        isProcessing = true
                                        resultMessage = null
                                        scope.launch {
                                            val success = withContext(Dispatchers.IO) {
                                                BatchProcessor.process(
                                                    sourceDir = src,
                                                    targetDir = tgt,
                                                    ruler = ruler,
                                                    unit = unit,
                                                    onProgress = { cur, tot, file ->
                                                        progress = cur
                                                        total = tot
                                                        currentFile = file
                                                    }
                                                )
                                            }
                                            resultMessage = "Готово. Обработано $success файлов."
                                            isProcessing = false
                                        }
                                    }
                                },
                                enabled = sourceDir != null && targetDir != null
                            ) {
                                Text("Запустить")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Открывает системный диалог выбора папки.
 *
 * @param title заголовок диалога
 * @return выбранная папка или `null`, если пользователь отменил выбор
 */
private fun chooseDirectory(title: String): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = title
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        isAcceptAllFileFilterUsed = false
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else {
        null
    }
}
