package com.ykononenko.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Верхняя панель с заголовком, настройкой подсказок и кнопками загрузки фото и
 * выхода.
 *
 * @param onLoadPhoto обработчик загрузки фотографии
 * @param onExit обработчик выхода из приложения
 * @param suppressWarnings отключены ли информационные предупреждения
 * @param onSuppressWarningsChange обработчик переключения настройки
 */
@Composable
fun TopBar(
    onLoadPhoto: () -> Unit,
    onExit: () -> Unit,
    suppressWarnings: Boolean,
    onSuppressWarningsChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1976D2))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "ScaleRuler",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = suppressWarnings,
                onCheckedChange = onSuppressWarningsChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.White,
                    uncheckedColor = Color.White,
                    checkmarkColor = Color(0xFF1976D2)
                )
            )
            Text(
                "Не показывать всплывающие подсказки",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onSuppressWarningsChange(!suppressWarnings)
                }
            )
        }
        TextButton(onClick = onLoadPhoto) {
            Text("Загрузить фото", color = Color.White)
        }
        TextButton(onClick = onExit) {
            Text("Выход", color = Color.White)
        }
    }
}
