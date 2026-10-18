package com.ykononenko.model

/**
 * Данные линейки, накладываемой на изображение.
 *
 * Все координаты и размеры хранятся в пикселях исходного изображения
 * (см. соглашение о координатах в AGENTS.md).
 *
 * @property x координата X левого верхнего угла линейки в пикселях изображения
 * @property y координата Y левого верхнего угла линейки в пикселях изображения
 * @property scalePxPerMm масштаб — сколько пикселей изображения приходится на один миллиметр
 * @property cellCount количество ячеек (делений) линейки
 * @property scalePerCell сколько единиц измерения приходится на одну ячейку
 * @property heightMm высота линейки в миллиметрах
 * @property orientation ориентация линейки; при [RulerOrientation.VERTICAL] деления
 *   идут снизу вверх, а [lengthMm] откладывается по вертикали
 * @property style стиль отрисовки линейки
 */
data class RulerData(
    val x: Float,
    val y: Float,
    val scalePxPerMm: Float,
    val cellCount: Int = DEFAULT_CELL_COUNT,
    val scalePerCell: Float = DEFAULT_SCALE_PER_CELL,
    val heightMm: Float = DEFAULT_HEIGHT_MM,
    val orientation: RulerOrientation = RulerOrientation.HORIZONTAL,
    val style: RulerStyle = RulerStyle.BLOCKS
) {

    /**
     * Полная длина линейки в единицах измерения: [cellCount] × [scalePerCell].
     */
    val lengthMm: Float
        get() = cellCount * scalePerCell

    /**
     * Ширина габаритного прямоугольника линейки в пикселях изображения.
     *
     * Для горизонтальной линейки — это длина, для вертикальной — высота.
     */
    val boxWidthPx: Float
        get() = if (orientation == RulerOrientation.HORIZONTAL) {
            lengthMm * scalePxPerMm
        } else {
            heightMm * scalePxPerMm
        }

    /**
     * Высота габаритного прямоугольника линейки в пикселях изображения.
     *
     * Для горизонтальной линейки — это высота, для вертикальной — длина.
     */
    val boxHeightPx: Float
        get() = if (orientation == RulerOrientation.HORIZONTAL) {
            heightMm * scalePxPerMm
        } else {
            lengthMm * scalePxPerMm
        }

    companion object {

        /** Количество ячеек линейки по умолчанию. */
        const val DEFAULT_CELL_COUNT = 5

        /** Масштаб одной ячейки по умолчанию (единиц измерения на ячейку). */
        const val DEFAULT_SCALE_PER_CELL = 1f

        /** Высота линейки по умолчанию в миллиметрах. */
        const val DEFAULT_HEIGHT_MM = 0.5f
    }
}

/**
 * Ориентация линейки на изображении.
 */
enum class RulerOrientation {

    /** Горизонтальная линейка, деления слева направо. */
    HORIZONTAL,

    /** Вертикальная линейка, деления снизу вверх. */
    VERTICAL
}

/**
 * Стиль отрисовки линейки.
 *
 * @property label подпись стиля, отображаемая пользователю
 */
enum class RulerStyle(val label: String) {

    /** Чередующиеся чёрно-белые клетки. */
    BLOCKS("Клетки"),

    /** Чередующиеся чёрно-белые клетки; подписи за линейкой белые. */
    BLOCKS_WHITE_LABELS("Клетки (белые подписи)"),

    /** Полоса снизу с чёрными зарубками. */
    BAR_BLACK("Чёрные зарубки"),

    /** Полоса снизу с белыми зарубками. */
    BAR_WHITE("Белые зарубки")
}

/**
 * Единица измерения длины. Влияет только на текст подписей линейки,
 * на геометрию и масштаб не влияет.
 *
 * @property label подпись единицы, отображаемая пользователю
 */
enum class LengthUnit(val label: String) {

    /** Миллиметры. */
    MM("мм"),

    /** Сантиметры. */
    CM("см"),

    /** Метры. */
    M("м"),

    /** Километры. */
    KM("км")
}

/**
 * Угол изображения, в который можно переместить линейку.
 *
 * @property label символ-стрелка, отображаемый на кнопке
 */
enum class Corner(val label: String) {

    /** Верхний левый угол. */
    TOP_LEFT("↖"),

    /** Верхний правый угол. */
    TOP_RIGHT("↗"),

    /** Нижний левый угол. */
    BOTTOM_LEFT("↙"),

    /** Нижний правый угол. */
    BOTTOM_RIGHT("↘")
}
