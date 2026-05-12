package com.ykononenko.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.state.AppState

@Composable
fun BottomBar(
    appState: AppState,
    mmText: String,
    onMmTextChange: (String) -> Unit,
    buttonText: String,
    selectedUnit: LengthUnit,
    onUnitChange: (LengthUnit) -> Unit,
    onMoveToCorner: (Corner) -> Unit,
    onBatchClick: () -> Unit,  // ← новый
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
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
        }

        if (appState == AppState.RULER_PLACED) {
            Column {
                Row {
                    SmallCornerButton(Corner.TOP_LEFT.label) { onMoveToCorner(Corner.TOP_LEFT) }
                    SmallCornerButton(Corner.TOP_RIGHT.label) { onMoveToCorner(Corner.TOP_RIGHT) }
                }
                Row {
                    SmallCornerButton(Corner.BOTTOM_LEFT.label) { onMoveToCorner(Corner.BOTTOM_LEFT) }
                    SmallCornerButton(Corner.BOTTOM_RIGHT.label) { onMoveToCorner(Corner.BOTTOM_RIGHT) }
                }
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onBatchClick,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Пакетная обработка")
            }
        }

        Button(
            onClick = onButtonClick,
            modifier = Modifier.height(48.dp)
        ) {
            Text(buttonText)
        }
    }
}

@Composable
private fun SmallCornerButton(arrow: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp).padding(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(arrow, fontSize = 12.sp)
    }
}