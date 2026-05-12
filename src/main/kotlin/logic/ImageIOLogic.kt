package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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

fun saveImageWithDialog(image: ImageBitmap, ruler: RulerData?, unit: LengthUnit): Boolean {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("PNG (*.png)", "png")
        selectedFile = File("result.png")
    }
    if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return false

    val file = chooser.selectedFile
    val out = if (file.extension.isBlank()) File("${file.absolutePath}.png") else file

    return try {
        val surface = ImageConverter.bitmapToSurface(image, ruler)
        ImageConverter.surfaceToFile(surface, out, ruler ?: return false, unit)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}