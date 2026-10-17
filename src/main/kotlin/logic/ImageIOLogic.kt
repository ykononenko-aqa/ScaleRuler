package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Загрузка изображений из файла и сохранение результата через диалоги выбора файлов.
 */

/**
 * Открывает диалог выбора изображения и загружает выбранный файл в [ImageBitmap].
 *
 * @return загруженное изображение или `null`, если пользователь отменил выбор
 *   либо файл не удалось прочитать
 */
fun loadImageFromFile(): ImageBitmap? {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter(
            "Изображения (*.jpg, *.jpeg, *.png, *.bmp)",
            "jpg", "jpeg", "png", "bmp"
        )
        dialogTitle = "Выберите изображение"
    }
    val result = chooser.showOpenDialog(null)
    if (result != JFileChooser.APPROVE_OPTION) return null

    return try {
        val bytes = chooser.selectedFile.readBytes()
        SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Открывает диалог сохранения и записывает изображение вместе с линейкой в PNG.
 *
 * @param image исходное изображение
 * @param ruler линейка для наложения или `null`
 * @param unit единица измерения для подписей линейки
 * @return `true`, если файл успешно сохранён; иначе `false`
 */
fun saveImageWithDialog(image: ImageBitmap, ruler: RulerData?, unit: LengthUnit): Boolean {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("PNG (*.png)", "png")
        selectedFile = File("result.png")
    }
    if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return false

    val file = chooser.selectedFile
    val out = if (file.extension.isBlank()) File("${file.absolutePath}.png") else file

    return try {
        val surface = ImageConverter.bitmapToSurface(image, ruler, unit)
        ImageConverter.surfaceToFile(surface, out)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
