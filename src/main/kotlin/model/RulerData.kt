package com.ykononenko.model

data class RulerData(
    val x: Float,
    val y: Float,
    val scalePxPerMm: Float,
    val lengthMm: Float = DEFAULT_LENGTH_MM,
    val heightMm: Float = DEFAULT_HEIGHT_MM
) {
    companion object {
        const val DEFAULT_LENGTH_MM = 5f
        const val DEFAULT_HEIGHT_MM = 0.5f
    }
}

enum class LengthUnit(val label: String) {
    MM("мм"),
    CM("см"),
    M("м"),
    KM("км")
}

enum class Corner(val label: String) {
    TOP_LEFT("↖"),
    TOP_RIGHT("↗"),
    BOTTOM_LEFT("↙"),
    BOTTOM_RIGHT("↘")
}