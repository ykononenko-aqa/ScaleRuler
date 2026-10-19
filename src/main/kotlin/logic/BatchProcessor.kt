package com.ykononenko.logic

import com.ykononenko.model.LengthUnit
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

/**
 * Пакетная обработка: наносит линейку одного масштаба на все изображения папки.
 *
 * Все фото должны быть сняты при одинаковых настройках камеры, иначе масштаб
 * линейки будет неверным.
 */
object BatchProcessor {

    /**
     * Обрабатывает все изображения из [sourceDir] и сохраняет результат в PNG
     * в [targetDir].
     *
     * Для каждого фото линейка подгоняется под его размеры через
     * [RulerFactory.fitToImage], сохраняя исходный масштаб.
     *
     * @param sourceDir папка с исходными изображениями (JPG/JPEG/PNG/BMP)
     * @param targetDir папка для сохранения результатов
     * @param ruler линейка, задающая масштаб (для всех фото одинаковый), или
     *   `null`, если наносится только указатель на север
     * @param northArrow указатель на север или `null`
     * @param unit единица измерения для подписей линейки
     * @param onProgress обратный вызов прогресса: номер текущего файла, всего
     *   файлов и имя обрабатываемого файла
     * @return количество успешно обработанных файлов
     */
    fun process(
        sourceDir: File,
        targetDir: File,
        ruler: RulerData?,
        northArrow: NorthArrowData?,
        unit: LengthUnit,
        onProgress: (current: Int, total: Int, fileName: String) -> Unit
    ): Int {
        val extensions = listOf("jpg", "jpeg", "png", "bmp")
        val files = sourceDir.listFiles { f ->
            f.isFile && f.extension.lowercase() in extensions
        }?.sortedBy { it.name } ?: return 0

        var successCount = 0

        files.forEachIndexed { index, file ->
            onProgress(index + 1, files.size, file.name)
            try {
                // Загружаем, чтобы узнать размер
                val bytes = file.readBytes()
                val skiaImage = SkiaImage.makeFromEncoded(bytes)
                val adjustedRuler = ruler?.let {
                    RulerFactory.fitToImage(it, skiaImage.width, skiaImage.height, unit)
                }
                val adjustedNorth = northArrow?.let {
                    NorthArrowFactory.fitToImage(it, skiaImage.width, skiaImage.height)
                }

                // Отрисовываем с скорректированной линейкой
                val surface = ImageConverter.fileToSurface(file, adjustedRuler, adjustedNorth, unit)
                if (surface != null) {
                    val targetFile = File(targetDir, file.nameWithoutExtension + ".png")
                    ImageConverter.surfaceToFile(surface, targetFile)
                    successCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return successCount
    }
}
