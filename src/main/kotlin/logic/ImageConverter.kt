package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorAlphaType
import javax.imageio.ImageIO
import java.io.File

object ImageConverter {

    fun fileToSurface(file: File, ruler: com.ykononenko.model.RulerData): Surface? {
        return try {
            val bytes = file.readBytes()
            val skiaImage = SkiaImage.makeFromEncoded(bytes)
            val image = skiaImage.toComposeImageBitmap()
            imageToSurface(image, ruler)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun bitmapToSurface(image: ImageBitmap, ruler: com.ykononenko.model.RulerData?): Surface {
        return imageToSurface(image, ruler)
    }

    private fun imageToSurface(image: ImageBitmap, ruler: com.ykononenko.model.RulerData?): Surface {
        val w = image.width
        val h = image.height
        val surface = Surface.makeRasterN32Premul(w, h)
        val canvas = surface.canvas

        val imagePixels = IntArray(w * h)
        image.readPixels(imagePixels)
        val skBitmap = Bitmap()
        skBitmap.allocPixels(ImageInfo.makeS32(w, h, ColorAlphaType.PREMUL))
        val imageBytes = ByteArray(w * h * 4)
        for (i in imagePixels.indices) {
            val c = imagePixels[i]
            imageBytes[i * 4] = (c and 0xFF).toByte()
            imageBytes[i * 4 + 1] = ((c shr 8) and 0xFF).toByte()
            imageBytes[i * 4 + 2] = ((c shr 16) and 0xFF).toByte()
            imageBytes[i * 4 + 3] = ((c shr 24) and 0xFF).toByte()
        }
        skBitmap.installPixels(skBitmap.imageInfo, imageBytes, w * 4)
        canvas.drawImage(SkiaImage.makeFromBitmap(skBitmap), 0f, 0f)

        if (ruler != null) {
            RulerDrawer.drawOnSkiaCanvas(canvas, ruler)
        }

        return surface
    }

    fun surfaceToFile(surface: Surface, targetFile: File, ruler: com.ykononenko.model.RulerData, unit: com.ykononenko.model.LengthUnit) {
        val resultImage = surface.makeImageSnapshot()
        val encoded = resultImage.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG, 100)
            ?: throw Exception("Failed to encode image")
        targetFile.writeBytes(encoded.bytes)

        // Дорисовываем подписи AWT
        val bufferedImage = ImageIO.read(targetFile)
        val g = bufferedImage.createGraphics()
        RulerDrawer.drawLabelsOnAWT(g, ruler, unit)
        g.dispose()
        ImageIO.write(bufferedImage, "png", targetFile)
    }
}