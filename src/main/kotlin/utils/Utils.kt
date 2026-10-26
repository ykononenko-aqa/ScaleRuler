package com.ykononenko.utils

/** Отступ изображения от края в пикселях, общий для линейки и указателя. */
const val IMAGE_MARGIN_PX = 10f

/** Текст поля «Ячеек» по умолчанию. */
const val DEFAULT_CELL_COUNT_TEXT = "5"

/** Текст поля «Цена деления» по умолчанию. */
const val DEFAULT_SCALE_TEXT = "1"

private val DECIMAL_INPUT = Regex("^\\d*\\.?\\d*$")
private val INTEGER_INPUT = Regex("^\\d*$")

/**
 * Проверяет, что строка является допустимым вводом дробного числа.
 *
 * Разрешены пустая строка, цифры и не более одной точки.
 */
fun isDecimalInput(value: String): Boolean = value.matches(DECIMAL_INPUT)

/**
 * Проверяет, что строка является допустимым вводом целого числа.
 *
 * Разрешены пустая строка и цифры.
 */
fun isIntegerInput(value: String): Boolean = value.matches(INTEGER_INPUT)

/**
 * Разбирает положительное дробное число.
 *
 * @return число или `null`, если строка не является конечным числом больше нуля
 */
fun parsePositiveFloat(value: String): Float? =
    value.toFloatOrNull()?.takeIf { it.isFinite() && it > 0f }

/**
 * Разбирает положительное целое число.
 *
 * @return число или `null`, если строка не является целым числом больше нуля
 */
fun parsePositiveInt(value: String): Int? =
    value.toIntOrNull()?.takeIf { it > 0 }
