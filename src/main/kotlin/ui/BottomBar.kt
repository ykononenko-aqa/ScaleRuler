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
import com.ykononenko.controls.ButtonControl
import com.ykononenko.controls.Control
import com.ykononenko.controls.EditorSession
import com.ykononenko.controls.buttons.MoveToCornerButton
import com.ykononenko.controls.buttons.ToggleOrientationButton
import com.ykononenko.controls.fields.SelectControl
import com.ykononenko.controls.fields.SliderControl
import com.ykononenko.controls.fields.TextFieldControl

/**
 * Нижняя панель управления.
 *
 * Состав элементов определяется [EditorSession.bottomBar]: панель лишь
 * отображает видимые controls и вызывает их действия. Все переходы состояний
 * находятся в кнопках.
 */
@Composable
fun BottomBar(session: EditorSession) {
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
        session.bottomBar.visibleRows(session).forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                val corners = row.filterIsInstance<MoveToCornerButton>()
                if (corners.isNotEmpty()) {
                    CornerGrid(corners, session)
                }
                row.filterNot { it is MoveToCornerButton }.forEach { control ->
                    ControlView(control, session)
                }
            }
        }
    }
}

/**
 * Сетка 2×2 из кнопок перемещения в углы.
 */
@Composable
private fun CornerGrid(corners: List<MoveToCornerButton>, session: EditorSession) {
    Column {
        corners.chunked(2).forEach { pair ->
            Row {
                pair.forEach { ControlView(it, session) }
            }
        }
    }
}

/**
 * Отображает один элемент нижней панели.
 */
@Composable
private fun ControlView(control: Control, session: EditorSession) {
    when (control) {
        is ButtonControl -> ButtonView(control, session)
        is TextFieldControl -> TextFieldView(control, session)
        is SelectControl<*> -> SelectView(control, session)
        is SliderControl -> SliderView(control, session)
        else -> Unit
    }
}

@Composable
private fun ButtonView(control: ButtonControl, session: EditorSession) {
    val small = control.size.width <= 24.dp
    val fontSize = when {
        small -> 11.sp
        control is ToggleOrientationButton -> 22.sp
        else -> 14.sp
    }

    Button(
        onClick = { control.action(session) },
        modifier = Modifier.size(control.size),
        contentPadding = if (small) PaddingValues(0.dp) else PaddingValues(horizontal = 8.dp),
        enabled = control.isEnabled(session)
    ) {
        Text(control.displayTitle(session), fontSize = fontSize)
    }
}

@Composable
private fun TextFieldView(control: TextFieldControl, session: EditorSession) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(control.title, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = control.value(session),
            onValueChange = { value ->
                if (control.filter(value)) {
                    control.onValueChange(session, value)
                }
            },
            modifier = Modifier
                .width(control.size.width)
                .height(control.size.height),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
    }
}

@Composable
private fun SelectView(control: SelectControl<*>, session: EditorSession) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.height(control.size.height)
        ) {
            Text(control.buttonLabel(session), fontSize = 14.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            control.options.forEach { option ->
                val value = option ?: return@forEach
                DropdownMenuItem(
                    onClick = {
                        control.select(session, value)
                        expanded = false
                    }
                ) {
                    val selected = value == control.selectedOption(session)
                    Text(if (selected) "✓  ${control.labelOf(value)}" else "   ${control.labelOf(value)}")
                }
            }
        }
    }
}

@Composable
private fun SliderView(control: SliderControl, session: EditorSession) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(control.title, fontSize = 14.sp)
        Slider(
            value = control.value(session),
            onValueChange = { control.onValueChange(session, it) },
            valueRange = control.range,
            modifier = Modifier
                .width(control.size.width)
                .height(control.size.height)
        )
        Spacer(Modifier.width(8.dp))
        Text(control.valueLabel(control.value(session)), fontSize = 14.sp)
    }
}
