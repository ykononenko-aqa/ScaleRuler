package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
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

fun saveImageWithDialog(image: ImageBitmap, ruler: RulerData?): Boolean {
    val chooser = JFileChooser().apply {
        fileFilter = FileNameExtensionFilter("PNG (*.png)", "png")
        selectedFile = File("result.png")
    }
    if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return false

    val file = chooser.selectedFile
    val out = if (file.extension.isBlank()) File("${file.absolutePath}.png") else file

    return try {
        val w = image.width
        val h = image.height

        // Создаём Skia Surface и рисуем на нём
        val surface = org.jetbrains.skia.Surface.makeRasterN32Premul(w, h)
        val canvas = surface.canvas

        // Рисуем исходное изображение
        val imagePixels = IntArray(w * h)
        image.readPixels(imagePixels)
        val skBitmap = org.jetbrains.skia.Bitmap()
        skBitmap.allocPixels(org.jetbrains.skia.ImageInfo.makeS32(w, h, org.jetbrains.skia.ColorAlphaType.PREMUL))
        val imageBytes = ByteArray(w * h * 4)
        for (i in imagePixels.indices) {
            val c = imagePixels[i]
            imageBytes[i * 4] = (c and 0xFF).toByte()           // B
            imageBytes[i * 4 + 1] = ((c shr 8) and 0xFF).toByte()  // G
            imageBytes[i * 4 + 2] = ((c shr 16) and 0xFF).toByte() // R
            imageBytes[i * 4 + 3] = ((c shr 24) and 0xFF).toByte() // A
        }
        skBitmap.installPixels(skBitmap.imageInfo, imageBytes, w * 4)
        canvas.drawImage(org.jetbrains.skia.Image.makeFromBitmap(skBitmap), 0f, 0f)

        // Рисуем линейку (единый метод)
        if (ruler != null) {
            RulerDrawer.drawOnSkiaCanvas(canvas, ruler)
        }

        // Сохраняем Surface напрямую в PNG
        val resultImage = surface.makeImageSnapshot()
        val encoded = resultImage.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG, 100)
        out.writeBytes(encoded?.bytes ?: throw Exception("Failed to encode image"))

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}