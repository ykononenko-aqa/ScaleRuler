package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
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

    val file: File = chooser.selectedFile
    return try {
        val bytes = file.readBytes()
        val skiaImage = SkiaImage.makeFromEncoded(bytes)
        skiaImage.toComposeImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}