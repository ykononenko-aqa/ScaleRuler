package com.ykononenko.controls

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ykononenko.controls.buttons.AddNorthButton
import com.ykononenko.controls.buttons.BatchButton
import com.ykononenko.controls.buttons.BuildRulerButton
import com.ykononenko.controls.buttons.DeleteMeasurementButton
import com.ykononenko.controls.buttons.LoadPhotoButton
import com.ykononenko.controls.buttons.MoveToCornerButton
import com.ykononenko.controls.buttons.SaveImageButton
import com.ykononenko.controls.buttons.SaveMeasurementPointButton
import com.ykononenko.controls.buttons.SavePointButton
import com.ykononenko.controls.buttons.StartMeasurementButton
import com.ykononenko.controls.buttons.StartPointPlacementButton
import com.ykononenko.controls.buttons.ToggleOrientationButton
import com.ykononenko.controls.fields.SelectControl
import com.ykononenko.controls.fields.SliderControl
import com.ykononenko.controls.fields.TextFieldControl
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.MeasurementColor
import com.ykononenko.model.MeasurementData
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.NorthArrowStyle
import com.ykononenko.model.RulerStyle
import com.ykononenko.state.AppState
import com.ykononenko.utils.isDecimalInput
import com.ykononenko.utils.isIntegerInput
import kotlin.math.roundToInt

/**
 * Модель нижней панели: список всех её элементов управления.
 *
 * Элементы хранят подпись, размер, строку, порядок, условие видимости и
 * действие. Фактическую раскладку выполняет Compose.
 */
class BottomBarModel(val controls: List<Control>) {

    /**
     * Возвращает видимые элементы, сгруппированные по строкам и отсортированные
     * по порядку.
     */
    fun visibleRows(session: EditorSession): List<List<Control>> =
        controls
            .filter { it.isVisible(session) }
            .sortedWith(compareBy({ it.row.ordinal }, { it.order }))
            .groupBy { it.row }
            .values
            .toList()

    companion object {

        /** Создаёт стандартный набор элементов нижней панели. */
        fun default(): BottomBarModel = BottomBarModel(
            listOf(
                TextFieldControl(
                    title = "Введите размер: ",
                    size = DpSize(80.dp, 48.dp),
                    row = ControlRow.SETTINGS,
                    order = 0,
                    visible = { it.appState == AppState.POINTS_SET },
                    value = { it.mmText },
                    filter = ::isDecimalInput,
                    onValueChange = { session, value -> session.updateMmText(value) }
                ),
                SelectControl(
                    title = "Единица измерения",
                    size = DpSize(80.dp, 48.dp),
                    row = ControlRow.SETTINGS,
                    order = 1,
                    visible = { it.appState == AppState.POINTS_SET },
                    options = LengthUnit.entries,
                    optionLabel = { it.label },
                    selected = { it.unit },
                    onSelect = { session, unit -> session.selectUnit(unit) },
                    buttonLabel = { it.unit.label }
                ),
                TextFieldControl(
                    title = "Ячеек: ",
                    size = DpSize(60.dp, 48.dp),
                    row = ControlRow.SETTINGS,
                    order = 2,
                    visible = { it.appState == AppState.POINTS_SET },
                    value = { it.cellCountText },
                    filter = ::isIntegerInput,
                    onValueChange = { session, value -> session.updateCellCountText(value) }
                ),
                TextFieldControl(
                    title = "Цена деления: ",
                    size = DpSize(70.dp, 48.dp),
                    row = ControlRow.SETTINGS,
                    order = 3,
                    visible = { it.appState == AppState.POINTS_SET },
                    value = { it.scaleText },
                    filter = ::isDecimalInput,
                    onValueChange = { session, value -> session.updateScaleText(value) }
                ),

                MoveToCornerButton(Corner.TOP_LEFT, order = 0),
                MoveToCornerButton(Corner.TOP_RIGHT, order = 1),
                MoveToCornerButton(Corner.BOTTOM_LEFT, order = 2),
                MoveToCornerButton(Corner.BOTTOM_RIGHT, order = 3),
                ToggleOrientationButton(),
                SelectControl(
                    title = "Стиль линейки",
                    size = DpSize(150.dp, 48.dp),
                    row = ControlRow.TOOLS,
                    order = 20,
                    visible = {
                        it.appState == AppState.RULER_PLACED && it.northArrow == null
                    },
                    options = RulerStyle.entries,
                    optionLabel = { it.label },
                    selected = { it.ruler?.style ?: RulerStyle.BLOCKS },
                    onSelect = { session, style -> session.setRulerStyle(style) },
                    buttonLabel = { "Стиль линейки" }
                ),
                SelectControl(
                    title = "Цвет указателя",
                    size = DpSize(150.dp, 48.dp),
                    row = ControlRow.TOOLS,
                    order = 21,
                    visible = { it.northArrow != null },
                    options = NorthArrowStyle.entries,
                    optionLabel = { it.label },
                    selected = { it.northArrow?.style ?: it.northStyle },
                    onSelect = { session, style -> session.selectNorthStyle(style) },
                    buttonLabel = { "Цвет указателя" }
                ),
                SelectControl(
                    title = "Цвет точек и линии",
                    size = DpSize(170.dp, 48.dp),
                    row = ControlRow.TOOLS,
                    order = 22,
                    visible = { it.appState == AppState.MEASURING },
                    options = MeasurementColor.entries,
                    optionLabel = { it.label },
                    selected = { it.measurementColor },
                    onSelect = { session, color -> session.selectMeasurementColor(color) },
                    buttonLabel = { "Цвет точек и линии" }
                ),
                DeleteMeasurementButton(),
                BatchButton(),
                AddNorthButton(
                    from = setOf(AppState.RULER_PLACED, AppState.MEASURING),
                    row = ControlRow.TOOLS,
                    order = 60
                ),

                SliderControl(
                    title = "Размер указателя: ",
                    size = DpSize(160.dp, 48.dp),
                    row = ControlRow.PRIMARY,
                    order = 0,
                    visible = {
                        it.appState == AppState.NORTH_PLACED && it.northArrow != null
                    },
                    value = { it.northArrow?.radiusFraction ?: NorthArrowData.DEFAULT_RADIUS_FRACTION },
                    range = NorthArrowData.MIN_RADIUS_FRACTION..NorthArrowData.MAX_RADIUS_FRACTION,
                    onValueChange = { session, value -> session.setNorthRadius(value) },
                    valueLabel = { "${(it * 100f).roundToInt()}%" }
                ),
                AddNorthButton(
                    from = setOf(AppState.PHOTO_LOADED),
                    row = ControlRow.PRIMARY,
                    order = 0
                ),
                SliderControl(
                    title = "Размер шрифта: ",
                    size = DpSize(160.dp, 48.dp),
                    row = ControlRow.PRIMARY,
                    order = 40,
                    visible = { it.appState == AppState.MEASURING },
                    value = { it.measurementFontSize },
                    range = MeasurementData.MIN_FONT_PX..MeasurementData.MAX_FONT_PX,
                    onValueChange = { session, value -> session.updateMeasurementFontSize(value) },
                    valueLabel = { "${it.roundToInt()} px" }
                ),
                LoadPhotoButton(),
                StartPointPlacementButton(),
                StartMeasurementButton(),
                SavePointButton(number = 1),
                SavePointButton(number = 2),
                SaveMeasurementPointButton(number = 1),
                SaveMeasurementPointButton(number = 2),
                BuildRulerButton(),
                SaveImageButton()
            )
        )
    }
}
