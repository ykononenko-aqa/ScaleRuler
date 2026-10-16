package com.ykononenko.logic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.RulerData
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.Surface
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorAlphaType
import java.io.File

/**
 * Конвертация изображений через Skia: подготовка поверхности для рисования и
 * сохранение результата в PNG.
 */
object ImageConverter {

    /**
     * Читает файл изображения и возвращает поверхность с уже нанесённой линейкой.
     *
     * @param file файл изображения (JPG/PNG/BMP)
     * @param ruler линейка, положение которой задано в пикселях этого изображения
     * @param unit единица измерения для подписей линейки
     * @return готовая к сохранению [Surface] или `null` при ошибке чтения
     */
    fun fileToSurface(file: File, ruler: RulerData, unit: LengthUnit): Surface? {
        return try {
            val bytes = file.readBytes()
            val skiaImage = SkiaImage.makeFromEncoded(bytes)
            val image = skiaImage.toComposeImageBitmap()
            imageToSurface(image, ruler, unit)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Создаёт поверхность с изображением и, при наличии, нанесённой линейкой.
     *
     * @param image исходное изображение
     * @param ruler линейка для наложения или `null`, чтобы вернуть только изображение
     * @param unit единица измерения для подписей линейки
     * @return поверхность в разрешении исходного изображения
     */
    fun bitmapToSurface(image: ImageBitmap, ruler: RulerData?, unit: LengthUnit): Surface {
        return imageToSurface(image, ruler, unit)
    }

    /**
     * Копирует пиксели [ImageBitmap] на растровую Skia-поверхность и при наличии
     * рисует линейку в масштабе 1:1.
     *
     * @param image исходное изображение
     * @param ruler линейка для наложения или `null`
     * @param unit единица измерения для подписей линейки
     * @return растровая поверхность размером с изображение
     */
    private fun imageToSurface(image: ImageBitmap, ruler: RulerData?, unit: LengthUnit): Surface {
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
            RulerDrawer.draw(canvas, ruler, 1f, ruler.x, ruler.y, unit)
        }

        return surface
    }

    /**
     * Кодирует поверхность в PNG-файл.
     *
     * @param surface поверхность с итоговым изображением
     * @param targetFile файл для записи
     * @throws Exception если не удалось закодировать изображение
     */
    fun surfaceToFile(surface: Surface, targetFile: File) {
        val resultImage = surface.makeImageSnapshot()
        val encoded = resultImage.encodeToData(org.jetbrains.skia.EncodedImageFormat.PNG, 100)
            ?: throw Exception("Failed to encode image")
        targetFile.writeBytes(encoded.bytes)
    }
}
