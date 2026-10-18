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
import com.ykononenko.model.RulerOrientation
import com.ykononenko.model.RulerStyle
import com.ykononenko.state.AppState

/**
 * Нижняя панель управления.
 *
 * Набор элементов зависит от [appState]:
 * - [AppState.POINTS_SET] — поле ввода реального размера, выбор единицы измерения,
 *   количество ячеек и цену деления (единиц на ячейку);
 * - [AppState.RULER_PLACED] — кнопки перемещения линейки в углы, переключение
 *   ориентации (горизонтальная/вертикальная) и запуск пакетной обработки;
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
 * @param onMoveToCorner перемещение линейки в выбранный угол
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
    onMoveToCorner: (Corner) -> Unit,
    onBatchClick: () -> Unit,  // ← новый
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (appState == AppState.POINTS_SET) {
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

            Spacer(Modifier.width(12.dp))
        }

        if (appState == AppState.RULER_PLACED) {
            Column {
                Row {
                    SmallIconButton(Corner.TOP_LEFT.label) { onMoveToCorner(Corner.TOP_LEFT) }
                    SmallIconButton(Corner.TOP_RIGHT.label) { onMoveToCorner(Corner.TOP_RIGHT) }
                }
                Row {
                    SmallIconButton(Corner.BOTTOM_LEFT.label) { onMoveToCorner(Corner.BOTTOM_LEFT) }
                    SmallIconButton(Corner.BOTTOM_RIGHT.label) { onMoveToCorner(Corner.BOTTOM_RIGHT) }
                }
            }
            Spacer(Modifier.width(8.dp))
            SmallIconButton(
                arrow = if (orientation == RulerOrientation.VERTICAL) "↔" else "↕",
                onClick = onToggleOrientation,
                modifier = Modifier.size(48.dp).padding(0.dp),
                fontSize = 22.sp
            )
            Spacer(Modifier.width(8.dp))

            // Выпадающий список стилей отрисовки линейки
            var styleExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { styleExpanded = true },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Стиль")
                }
                DropdownMenu(
                    expanded = styleExpanded,
                    onDismissRequest = { styleExpanded = false }
                ) {
                    RulerStyle.entries.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                onStyleChange(item)
                                styleExpanded = false
                            }
                        ) {
                            Text(if (item == style) "✓  ${item.label}" else "   ${item.label}")
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onBatchClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Пакетная обработка")
            }
            Spacer(Modifier.width(12.dp))
        }

        Button(
            onClick = onButtonClick,
            modifier = Modifier.height(48.dp)
        ) {
            Text(buttonText)
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

