package com.ykononenko.logic

import com.ykononenko.sessionWithMeasurement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ImageConverterTest {

    @Test
    fun bitmapToSurfaceDrawsMeasurementsAtImageResolution() {
        val session = sessionWithMeasurement()
        val image = assertNotNull(session.image)
        val ruler = assertNotNull(session.ruler)
        assertTrue(session.measurements.items.isNotEmpty())

        val surface = ImageConverter.bitmapToSurface(
            image = image,
            ruler = ruler,
            northArrow = null,
            unit = session.unit,
            measurements = session.measurements.items
        )

        try {
            assertEquals(image.width, surface.width)
            assertEquals(image.height, surface.height)
        } finally {
            surface.close()
        }
    }
}
