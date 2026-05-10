package com.ykononenko.logic

import com.ykononenko.state.AppState

fun nextState(current: AppState): AppState {
    return when (current) {
        AppState.IDLE -> AppState.PHOTO_LOADED
        AppState.PHOTO_LOADED -> AppState.AWAITING_POINT_1
        AppState.AWAITING_POINT_1 -> AppState.AWAITING_POINT_2
        AppState.AWAITING_POINT_2 -> AppState.POINTS_SET
        AppState.POINTS_SET -> AppState.RULER_PLACED
        AppState.RULER_PLACED -> current // остаёмся на месте
    }
}

fun buttonText(state: AppState): String {
    return when (state) {
        AppState.IDLE -> "Загрузить фото"
        AppState.PHOTO_LOADED -> "Перейти в режим расстановки точек"
        AppState.AWAITING_POINT_1 -> "Сохранить первую точку"
        AppState.AWAITING_POINT_2 -> "Сохранить вторую точку"
        AppState.POINTS_SET -> "Рассчитать и вывести линейку"
        AppState.RULER_PLACED -> "Сохранить изображение"
    }
}

fun statusMessage(state: AppState): String {
    return when (state) {
        AppState.IDLE -> "Загрузите фото через меню «Загрузить фото» или кнопку ниже"
        AppState.PHOTO_LOADED -> "Фото загружено. Нажмите кнопку для перехода в режим расстановки точек"
        AppState.AWAITING_POINT_1 -> "Кликните по фото и нажмите «Сохранить первую точку»"
        AppState.AWAITING_POINT_2 -> "Кликните по фото и нажмите «Сохранить вторую точку»"
        AppState.POINTS_SET -> "Введите расстояние в мм и нажмите «Рассчитать и вывести линейку»"
        AppState.RULER_PLACED -> "Линейка размещена. Перетащите её и нажмите «Сохранить изображение»"
    }
}