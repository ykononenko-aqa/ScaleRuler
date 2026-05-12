package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Surface
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
                val bytes = file.readBytes()
                val skiaImage = SkiaImage.makeFromEncoded(bytes)
                val image = skiaImage.toComposeImageBitmap()
                val w = image.width
                val h = image.height

                val surface = Surface.makeRasterN32Premul(w, h)
                val canvas = surface.canvas

                // Рисуем фото
                val imagePixels = IntArray(w * h)
                image.readPixels(imagePixels)
                val skBitmap = org.jetbrains.skia.Bitmap()
                skBitmap.allocPixels(
                    org.jetbrains.skia.ImageInfo.makeS32(
                        w, h,
                        org.jetbrains.skia.ColorAlphaType.PREMUL
                    )
                )
                val imageBytes = ByteArray(w * h * 4)
                for (i in imagePixels.indices) {
                    val c = imagePixels[i]
                    imageBytes[i * 4] = (c and 0xFF).toByte()
                    imageBytes[i * 4 + 1] = ((c shr 8) and 0xFF).toByte()
                    imageBytes[i * 4 + 2] = ((c shr 16) and 0xFF).toByte()
                    imageBytes[i * 4 + 3] = ((c shr 24) and 0xFF).toByte()
                }
                skBitmap.installPixels(skBitmap.imageInfo, imageBytes, w * 4)
                canvas.drawImage(
                    org.jetbrains.skia.Image.makeFromBitmap(skBitmap), 0f, 0f
                )

                // Рисуем линейку
                RulerDrawer.drawOnSkiaCanvas(canvas, ruler)

                // Сохраняем PNG
                val resultImage = surface.makeImageSnapshot()
                val encoded = resultImage.encodeToData(
                    org.jetbrains.skia.EncodedImageFormat.PNG, 100
                )
                if (encoded != null) {
                    val targetFile = File(targetDir, file.nameWithoutExtension + ".png")
                    targetFile.writeBytes(encoded.bytes)

                    // Дорисовываем подписи AWT
                    val bufferedImage = javax.imageio.ImageIO.read(targetFile)
                    val g = bufferedImage.createGraphics()
                    RulerDrawer.drawLabelsOnAWT(g, ruler, unit)
                    g.dispose()
                    javax.imageio.ImageIO.write(bufferedImage, "png", targetFile)

                    successCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return successCount
    }
}