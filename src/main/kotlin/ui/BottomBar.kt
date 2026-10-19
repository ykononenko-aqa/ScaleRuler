package com.ykononenko.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.NorthArrowStyle
import com.ykononenko.model.RulerOrientation
import com.ykononenko.model.RulerStyle
import com.ykononenko.state.AppState
import kotlin.math.roundToInt

/**
 * Нижняя панель управления.
 *
 * Набор элементов зависит от [appState]:
 * - [AppState.PHOTO_LOADED] — кнопка добавления указателя на север (без линейки);
 * - [AppState.POINTS_SET] — поле ввода реального размера, выбор единицы измерения,
 *   количество ячеек и цена деления (единиц на ячейку);
 * - [AppState.RULER_PLACED] — кнопки перемещения линейки в углы, переключение
 *   ориентации, стиль линейки, добавление указателя на север и пакетная обработка;
 * - [AppState.NORTH_PLACED] — работа с указателем на север: перемещение в углы,
 *   цвет и размер, пакетная обработка (если линейка есть);
 * всегда видна основная кнопка действия.
 *
 * @param appState текущее состояние сценария
 * @param mmText текущий текст поля ввода размера
 * @param onMmTextChange изменение текста поля ввода размера
 * @param cellCountText текущий текст поля ввода количества ячеек
 * @param onCellCountTextChange изменение текста поля ввода количества ячеек
 * @param scaleText текущий текст поля ввода цены деления
 * @param onScaleTextChange изменение текста поля ввода цены деления
 * @param buttonText подпись основной кнопки
 * @param selectedUnit выбранная единица измерения
 * @param onUnitChange изменение единицы измерения
 * @param orientation текущая ориентация линейки
 * @param onToggleOrientation переключение ориентации линейки
 * @param style текущий стиль отрисовки линейки
 * @param onStyleChange изменение стиля отрисовки линейки
 * @param northArrow указатель на север или `null`
 * @param northStyle текущий цвет указателя
 * @param onNorthStyleChange изменение цвета указателя
 * @param onNorthRadiusChange изменение размера указателя
 * @param onAddNorth запрос на добавление указателя на север
 * @param showBatch доступна ли пакетная обработка (нужна линейка)
 * @param onMoveToCorner перемещение линейки или указателя в выбранный угол
 * @param onBatchClick открытие диалога пакетной обработки
 * @param onButtonClick нажатие основной кнопки действия
 */
@Composable
fun BottomBar(
    appState: AppState,
    mmText: String,
    onMmTextChange: (String) -> Unit,
    cellCountText: String,
    onCellCountTextChange: (String) -> Unit,
    scaleText: String,
    onScaleTextChange: (String) -> Unit,
    buttonText: String,
    selectedUnit: LengthUnit,
    onUnitChange: (LengthUnit) -> Unit,
    orientation: RulerOrientation,
    onToggleOrientation: () -> Unit,
    style: RulerStyle,
    onStyleChange: (RulerStyle) -> Unit,
    northArrow: NorthArrowData?,
    northStyle: NorthArrowStyle,
    onNorthStyleChange: (NorthArrowStyle) -> Unit,
    onNorthRadiusChange: (Float) -> Unit,
    onAddNorth: () -> Unit,
    showBatch: Boolean,
    onMoveToCorner: (Corner) -> Unit,
    onBatchClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    // Фиксированная высота: панель не растёт при появлении новых элементов,
    // поэтому область изображения не уменьшается по ходу работы.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    ) {
        if (appState == AppState.POINTS_SET) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Введите размер: ", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = mmText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onMmTextChange(newValue)
                        }
                    },
                    modifier = Modifier.width(80.dp).height(48.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                Spacer(Modifier.width(4.dp))

                // Выпадающий список единиц
                UnitDropdown(selectedUnit = selectedUnit, onUnitChange = onUnitChange)

                Spacer(Modifier.width(12.dp))

                Text("Ячеек: ", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = cellCountText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                            onCellCountTextChange(newValue)
                        }
                    },
                    modifier = Modifier.width(60.dp).height(48.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                Spacer(Modifier.width(12.dp))

                Text("Цена деления: ", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = scaleText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            onScaleTextChange(newValue)
                        }
                    },
                    modifier = Modifier.width(70.dp).height(48.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
            }
        }

        if (appState == AppState.RULER_PLACED || appState == AppState.NORTH_PLACED) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column {
                    Row {
                        SmallIconButton(
                            Corner.TOP_LEFT.label,
                            modifier = Modifier.size(24.dp)
                        ) { onMoveToCorner(Corner.TOP_LEFT) }
                        SmallIconButton(
                            Corner.TOP_RIGHT.label,
                            modifier = Modifier.size(24.dp)
                        ) { onMoveToCorner(Corner.TOP_RIGHT) }
                    }
                    Row {
                        SmallIconButton(
                            Corner.BOTTOM_LEFT.label,
                            modifier = Modifier.size(24.dp)
                        ) { onMoveToCorner(Corner.BOTTOM_LEFT) }
                        SmallIconButton(
                            Corner.BOTTOM_RIGHT.label,
                            modifier = Modifier.size(24.dp)
                        ) { onMoveToCorner(Corner.BOTTOM_RIGHT) }
                    }
                }
                Spacer(Modifier.width(8.dp))

                // Ориентацию можно менять только до добавления указателя
                if (northArrow == null) {
                    SmallIconButton(
                        arrow = if (orientation == RulerOrientation.VERTICAL) "↔" else "↕",
                        onClick = onToggleOrientation,
                        modifier = Modifier.size(48.dp).padding(0.dp),
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.width(8.dp))
                }

                // После добавления указателя выбор цвета указателя заменяет
                // выбор стиля линейки
                if (northArrow == null) {
                    RulerStyleDropdown(style = style, onStyleChange = onStyleChange)
                } else {
                    NorthStyleDropdown(style = northStyle, onStyleChange = onNorthStyleChange)
                }
                Spacer(Modifier.width(8.dp))

                if (showBatch) {
                    Button(
                        onClick = onBatchClick,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Пакетная обработка")
                    }
                }

                if (appState == AppState.RULER_PLACED) {
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onAddNorth,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Добавить указатель")
                    }
                }
            }

        }

        // Основная кнопка и управление размером указателя находятся в одной
        // строке, чтобы панель не росла в высоту после добавления указателя.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (appState == AppState.NORTH_PLACED && northArrow != null) {
                Text("Размер указателя: ", fontSize = 14.sp)
                Slider(
                    value = northArrow.radiusFraction,
                    onValueChange = onNorthRadiusChange,
                    valueRange = NorthArrowData.MIN_RADIUS_FRACTION..NorthArrowData.MAX_RADIUS_FRACTION,
                    modifier = Modifier.width(160.dp).height(48.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${(northArrow.radiusFraction * 100f).roundToInt()}%",
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(16.dp))
            }

            if (appState == AppState.PHOTO_LOADED) {
                Button(
                    onClick = onAddNorth,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Добавить указатель")
                }
                Spacer(Modifier.width(8.dp))
            }

            Button(
                onClick = onButtonClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}

/**
 * Выпадающий список единиц измерения.
 *
 * @param selectedUnit выбранная единица измерения
 * @param onUnitChange изменение единицы измерения
 */
@Composable
private fun UnitDropdown(
    selectedUnit: LengthUnit,
    onUnitChange: (LengthUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selectedUnit.label, fontSize = 14.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LengthUnit.entries.forEach { unit ->
                DropdownMenuItem(
                    onClick = {
                        onUnitChange(unit)
                        expanded = false
                    }
                ) {
                    Text(unit.label)
                }
            }
        }
    }
}

/**
 * Выпадающий список стилей линейки.
 *
 * @param style текущий стиль отрисовки линейки
 * @param onStyleChange изменение стиля отрисовки линейки
 */
@Composable
private fun RulerStyleDropdown(
    style: RulerStyle,
    onStyleChange: (RulerStyle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.height(48.dp)
        ) {
            Text("Стиль линейки")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RulerStyle.entries.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onStyleChange(item)
                        expanded = false
                    }
                ) {
                    Text(if (item == style) "✓  ${item.label}" else "   ${item.label}")
                }
            }
        }
    }
}

/**
 * Выпадающий список цветов указателя на север.
 *
 * @param style текущий цвет указателя
 * @param onStyleChange изменение цвета указателя
 */
@Composable
private fun NorthStyleDropdown(
    style: NorthArrowStyle,
    onStyleChange: (NorthArrowStyle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.height(48.dp)
        ) {
            Text("Цвет указателя")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NorthArrowStyle.entries.forEach { item ->
                DropdownMenuItem(
                    onClick = {
                        onStyleChange(item)
                        expanded = false
                    }
                ) {
                    Text(if (item == style) "✓  ${item.label}" else "   ${item.label}")
                }
            }
        }
    }
}

/**
 * Маленькая кнопка с символом-иконкой (стрелка перемещения в угол, ориентация).
 *
 * Выполнена в том же стиле, что и остальные кнопки панели: тёмная заливка,
 * белый символ.
 *
 * @param arrow символ-стрелка, отображаемый на кнопке
 * @param modifier модификатор размера кнопки
 * @param fontSize размер шрифта символа-стрелки
 * @param onClick обработчик нажатия
 */
@Composable
private fun SmallIconButton(
    arrow: String,
    modifier: Modifier = Modifier.size(28.dp).padding(0.dp),
    fontSize: TextUnit = 12.sp,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(arrow, fontSize = fontSize)
    }
}
