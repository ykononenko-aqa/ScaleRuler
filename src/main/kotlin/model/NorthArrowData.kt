package com.ykononenko.model

/**
 * Данные указателя на север, накладываемого на изображение.
 *
 * Все координаты и размеры хранятся в пикселях исходного изображения
 * (см. соглашение о координатах в AGENTS.md).
 *
 * @property centerX координата X центра указателя в пикселях изображения
 * @property centerY координата Y центра указателя в пикселях изображения
 * @property radiusFraction радиус указателя как доля короткой стороны изображения
 * @property angleRad угол поворота стрелки: `0` — на север (вверх), по часовой стрелке
 * @property style стиль отрисовки указателя
 */
data class NorthArrowData(
    val centerX: Float,
    val centerY: Float,
    val radiusFraction: Float = DEFAULT_RADIUS_FRACTION,
    val angleRad: Float = 0f,
    val style: NorthArrowStyle = NorthArrowStyle.BLACK
) {

    /**
     * Радиус внешней невидимой окружности указателя в пикселях изображения.
     *
     * @param imageWidth ширина изображения в пикселях
     * @param imageHeight высота изображения в пикселях
     */
    fun radiusPx(imageWidth: Int, imageHeight: Int): Float =
        radiusFraction * minOf(imageWidth.toFloat(), imageHeight.toFloat())

    companion object {

        /** Радиус указателя по умолчанию как доля короткой стороны изображения. */
        const val DEFAULT_RADIUS_FRACTION = 0.15f

        /** Минимальный радиус указателя как доля короткой стороны изображения. */
        const val MIN_RADIUS_FRACTION = 0.05f

        /** Максимальный радиус указателя как доля короткой стороны изображения. */
        const val MAX_RADIUS_FRACTION = 0.35f

        /** Радиус центрального круга-ручки как доля радиуса указателя. */
        const val CENTER_CIRCLE_FRACTION = 0.22f
    }
}

/**
 * Стиль отрисовки указателя на север. Задаёт цвет стрелки, центрального круга
 * и буквы «С».
 *
 * @property label подпись стиля, отображаемая пользователю
 */
enum class NorthArrowStyle(val label: String) {

    /** Чёрный указатель (для светлых фотографий). */
    BLACK("Чёрный"),

    /** Белый указатель (для тёмных фотографий). */
    WHITE("Белый")
}
