package com.ykononenko.model

/** Размер шрифта подписи по умолчанию в пикселях. */
const val DEFAULT_MEASUREMENT_FONT_PX = 16f

/** Минимальный размер шрифта подписи в пикселях. */
const val MIN_MEASUREMENT_FONT_PX = 8f

/** Максимальный размер шрифта подписи в пикселях. */
const val MAX_MEASUREMENT_FONT_PX = 48f

/**
 * Цвет размерной линии, её точек и подписи.
 *
 * @property label подпись цвета, отображаемая пользователю
 */
enum class MeasurementColor(val label: String) {

    /** Чёрная линия (для светлых фотографий). */
    BLACK("Чёрный"),

    /** Белая линия (для тёмных фотографий). */
    WHITE("Белый")
}

/**
 * Завершённая размерная линия между двумя точками.
 *
 * Все координаты хранятся в пикселях исходного изображения.
 *
 * @property point1 первая точка в координатах изображения
 * @property point2 вторая точка в координатах изображения
 * @property color цвет линии, точек и подписи
 * @property fontSizePx размер шрифта подписи, зафиксированный при создании линии
 */
data class Measurement(
    val point1: Point,
    val point2: Point,
    val color: MeasurementColor = MeasurementColor.BLACK,
    val fontSizePx: Float = DEFAULT_MEASUREMENT_FONT_PX
)

/**
 * Состояние режима размерных линий.
 *
 * @property items завершённые линии и их подписи
 * @property pendingPoint временная точка до фиксации
 * @property point1 зафиксированная первая точка до сохранения второй
 */
data class MeasurementData(
    val items: List<Measurement> = emptyList(),
    val pendingPoint: Point? = null,
    val point1: Point? = null
) {

    companion object {

        /** Размер шрифта подписи по умолчанию в пикселях. */
        const val DEFAULT_FONT_PX = DEFAULT_MEASUREMENT_FONT_PX

        /** Минимальный размер шрифта подписи в пикселях. */
        const val MIN_FONT_PX = MIN_MEASUREMENT_FONT_PX

        /** Максимальный размер шрифта подписи в пикселях. */
        const val MAX_FONT_PX = MAX_MEASUREMENT_FONT_PX
    }
}
