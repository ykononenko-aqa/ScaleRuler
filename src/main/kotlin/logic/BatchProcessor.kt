package com.ykononenko.logic

import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import java.io.File

object BatchProcessor {

    fun process(
        sourceDir: File,
        targetDir: File,
        ruler: RulerData,
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
                val adjustedRuler = RulerFactory.fitToImage(ruler, skiaImage.width, skiaImage.height)

                // Отрисовываем с скорректированной линейкой
                val surface = ImageConverter.fileToSurface(file, adjustedRuler)
                if (surface != null) {
                    val targetFile = File(targetDir, file.nameWithoutExtension + ".png")
                    ImageConverter.surfaceToFile(surface, targetFile, adjustedRuler, unit)
                    successCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return successCount
    }
}