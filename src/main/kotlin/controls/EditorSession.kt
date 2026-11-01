package com.ykononenko.controls

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import com.ykononenko.logic.NorthArrowFactory
import com.ykononenko.logic.RulerFactory
import com.ykononenko.logic.distanceBetween
import com.ykononenko.model.AppPoints
import com.ykononenko.model.Corner
import com.ykononenko.model.LengthUnit
import com.ykononenko.model.Measurement
import com.ykononenko.model.MeasurementColor
import com.ykononenko.model.MeasurementData
import com.ykononenko.model.NorthArrowData
import com.ykononenko.model.NorthArrowStyle
import com.ykononenko.model.Point
import com.ykononenko.model.RulerData
import com.ykononenko.model.RulerStyle
import com.ykononenko.state.AppState
import com.ykononenko.utils.DEFAULT_CELL_COUNT_TEXT
import com.ykononenko.utils.DEFAULT_SCALE_TEXT
import com.ykononenko.utils.isDecimalInput
import com.ykononenko.utils.isIntegerInput
import com.ykononenko.utils.parsePositiveFloat
import com.ykononenko.utils.parsePositiveInt

/**
 * Единое изменяемое состояние приложения.
 *
 * Хранит данные изображения, точек, линейки, указателя на север, полей нижней
 * панели и флагов диалогов. Все переходы по кнопкам выполняются через
 * [StateButton], а действия над данными — через методы этого класса.
 *
 * @param fileService сервис диалогов загрузки и сохранения изображений
 * @param bottomBar модель элементов нижней панели
 * @param settingsStore хранилище пользовательских настроек
 */
class EditorSession(
    val fileService: ImageFileService,
    val bottomBar: BottomBarModel = BottomBarModel.default(),
    private val settingsStore: SettingsStore = PropertiesSettingsStore()
) {

    /** Текущее состояние сценария. */
    var appState: AppState by mutableStateOf(AppState.IDLE)
        private set

    /** Отключены ли информационные предупреждения. */
    var suppressWarnings: Boolean by mutableStateOf(settingsStore.load().suppressWarnings)
        private set

    /** Загруженное изображение или `null`. */
    var image: ImageBitmap? by mutableStateOf(null)
        private set

    /** Расставленные пользователем точки. */
    var points: AppPoints by mutableStateOf(AppPoints())
        private set

    /** Размерные линии и незавершённые точки режима измерений. */
    var measurements: MeasurementData by mutableStateOf(MeasurementData())
        private set

    /** Цвет новых размерных линий. */
    var measurementColor: MeasurementColor by mutableStateOf(MeasurementColor.BLACK)
        private set

    /** Размер шрифта подписей размерных линий в пикселях. */
    var measurementFontSize: Float by mutableStateOf(MeasurementData.DEFAULT_FONT_PX)
        private set

    /** Размещённая линейка или `null`. */
    var ruler: RulerData? by mutableStateOf(null)
        private set

    /** Указатель на север или `null`. */
    var northArrow: NorthArrowData? by mutableStateOf(null)
        private set

    /** Выбранная единица измерения. */
    var unit: LengthUnit by mutableStateOf(LengthUnit.MM)
        private set

    /** Текст поля реального размера. */
    var mmText: String by mutableStateOf("")
        private set

    /** Текст поля количества ячеек. */
    var cellCountText: String by mutableStateOf(DEFAULT_CELL_COUNT_TEXT)
        private set

    /** Текст поля цены деления. */
    var scaleText: String by mutableStateOf(DEFAULT_SCALE_TEXT)
        private set

    /** Цвет указателя на север. */
    var northStyle: NorthArrowStyle by mutableStateOf(NorthArrowStyle.BLACK)
        private set

    /** Размер области Canvas. */
    var canvasSize: Size by mutableStateOf(Size.Zero)
        private set

    /** Показывать ли предупреждение перед добавлением указателя. */
    var showNorthWarning: Boolean by mutableStateOf(false)
        private set

    /** Показывать ли предупреждение перед входом в режим измерений. */
    var showMeasurementModeWarning: Boolean by mutableStateOf(false)
        private set

    /** Показывать ли предупреждение перед удалением измерения. */
    var showMeasurementDeleteWarning: Boolean by mutableStateOf(false)
        private set

    /** Есть ли что удалять в режиме измерений. */
    val hasMeasurementToDelete: Boolean
        get() = measurements.pendingPoint != null ||
            measurements.point1 != null ||
            measurements.items.isNotEmpty()

    /** Показывать ли диалог пакетной обработки. */
    var showBatchDialog: Boolean by mutableStateOf(false)
        private set

    /**
     * Выполняет переход в новое состояние.
     *
     * Доступен только действиям пакета `controls`.
     */
    internal fun transitionTo(state: AppState) {
        appState = state
    }

    /**
     * Сбрасывает данные под новое загруженное фото.
     *
     * Состояние не меняет: переход выполняет кнопка `LoadPhotoButton`.
     */
    fun resetForNewPhoto(image: ImageBitmap) {
        this.image = image
        mmText = ""
        cellCountText = DEFAULT_CELL_COUNT_TEXT
        scaleText = DEFAULT_SCALE_TEXT
        unit = LengthUnit.MM
        points = AppPoints()
        measurements = MeasurementData()
        ruler = null
        northArrow = null
        showNorthWarning = false
        showMeasurementModeWarning = false
        showMeasurementDeleteWarning = false
        showBatchDialog = false
    }

    /**
     * Фиксирует ожидающую точку как первую или вторую.
     *
     * @param number номер точки: `1` или `2`
     * @return `true`, если точка зафиксирована
     */
    fun commitPendingPoint(number: Int): Boolean {
        val pending = points.pendingPoint ?: return false
        points = when (number) {
            1 -> points.copy(point1 = pending, pendingPoint = null)
            2 -> points.copy(point2 = pending, pendingPoint = null)
            else -> return false
        }
        return true
    }

    /**
     * Рассчитывает масштаб по точкам и введённому размеру и создаёт линейку.
     *
     * @return `true`, если ввод корректен и линейка создана
     */
    fun buildRulerFromInputs(): Boolean {
        val mm = parsePositiveFloat(mmText) ?: return false
        val cellCount = parsePositiveInt(cellCountText) ?: return false
        val scalePerCell = parsePositiveFloat(scaleText) ?: return false
        val p1 = points.point1 ?: return false
        val p2 = points.point2 ?: return false
        val image = image ?: return false
        val distance = distanceBetween(p1, p2)
        if (distance <= 0f) return false

        val scalePxPerMm = distance / mm
        ruler = RulerFactory.create(image, scalePxPerMm, cellCount, scalePerCell, unit)
        northArrow = null
        points = AppPoints()
        return true
    }

    /**
     * Создаёт указатель на север в правом верхнем углу изображения.
     *
     * Если линейки ещё нет, режим остаётся только с указателем. Состояние не
     * меняет: переход выполняет кнопка `ConfirmAddNorthButton`.
     *
     * @return `true`, если указатель создан
     */
    fun createNorthArrow(): Boolean {
        val image = image ?: return false
        northArrow = NorthArrowFactory.create(image.width, image.height, northStyle)
        if (appState == AppState.PHOTO_LOADED) {
            ruler = null
        }
        showNorthWarning = false
        clearMeasurementInProgress()
        return true
    }

    /**
     * Сохраняет текущее изображение с линейкой и указателем.
     *
     * @return `true`, если файл сохранён
     */
    fun saveImage(): Boolean {
        val image = image ?: return false
        return fileService.saveImage(
            image = image,
            ruler = ruler,
            northArrow = northArrow,
            measurements = measurements.items,
            measurementFontSize = measurementFontSize,
            unit = unit
        )
    }

    /** Запоминает точку клика в координатах изображения. */
    fun setPendingPoint(imagePoint: Point) {
        points = points.copy(pendingPoint = imagePoint)
    }

    /** Запоминает временную точку режима измерений в координатах изображения. */
    fun setMeasurementPoint(imagePoint: Point) {
        measurements = measurements.copy(pendingPoint = imagePoint)
    }

    /**
     * Фиксирует ожидающую точку как первую или завершает линию второй точкой.
     *
     * @param number номер точки: `1` или `2`
     * @return `true`, если точка зафиксирована или линия создана
     */
    fun commitMeasurementPoint(number: Int): Boolean {
        val pending = measurements.pendingPoint ?: return false
        measurements = when (number) {
            1 -> measurements.copy(point1 = pending, pendingPoint = null)
            2 -> {
                val first = measurements.point1 ?: return false
                measurements.copy(
                    items = measurements.items + Measurement(first, pending, measurementColor),
                    point1 = null,
                    pendingPoint = null
                )
            }

            else -> return false
        }
        return true
    }

    /**
     * Удаляет незавершённую точку или последнюю завершённую линию.
     */
    fun removeLastMeasurement() {
        val current = measurements
        measurements = when {
            current.pendingPoint != null -> current.copy(pendingPoint = null)
            current.point1 != null -> current.copy(point1 = null)
            current.items.isNotEmpty() -> current.copy(items = current.items.dropLast(1))
            else -> current
        }
        showMeasurementDeleteWarning = false
    }

    /** Выбирает цвет для новых размерных линий. */
    fun selectMeasurementColor(color: MeasurementColor) {
        measurementColor = color
    }

    /** Устанавливает размер шрифта подписей размерных линий. */
    fun updateMeasurementFontSize(value: Float) {
        measurementFontSize = value.coerceIn(
            MeasurementData.MIN_FONT_PX,
            MeasurementData.MAX_FONT_PX
        )
    }

    /** Очищает незавершённые точки текущей линии, не удаляя готовые линии. */
    fun clearMeasurementInProgress() {
        measurements = measurements.copy(pendingPoint = null, point1 = null)
        showMeasurementDeleteWarning = false
    }

    /** Смещает линейку на заданное расстояние в пикселях изображения. */
    fun moveRulerBy(deltaImage: Offset) {
        val ruler = ruler ?: return
        this.ruler = ruler.copy(
            x = ruler.x + deltaImage.x,
            y = ruler.y + deltaImage.y
        )
    }

    /** Смещает указатель на север в пикселях изображения. */
    fun moveNorthBy(deltaImage: Offset) {
        val arrow = northArrow ?: return
        northArrow = NorthArrowFactory.move(arrow, deltaImage.x, deltaImage.y)
    }

    /** Поворачивает указатель на север на заданный угол в радианах. */
    fun rotateNorth(angleRad: Float) {
        northArrow = northArrow?.copy(angleRad = angleRad)
    }

    /** Перемещает указатель, если он есть, иначе линейку, в выбранный угол. */
    fun moveSelectionToCorner(corner: Corner) {
        val image = image ?: return
        val arrow = northArrow
        if (arrow != null) {
            northArrow = NorthArrowFactory.moveToCorner(
                north = arrow,
                imageWidth = image.width,
                imageHeight = image.height,
                corner = corner
            )
        } else {
            val ruler = ruler ?: return
            this.ruler = RulerFactory.moveToCorner(ruler, image, corner, unit)
        }
    }

    /** Переключает ориентацию линейки. */
    fun toggleRulerOrientation() {
        val ruler = ruler ?: return
        val image = image ?: return
        this.ruler = RulerFactory.toggleOrientation(ruler, image, unit)
    }

    /** Устанавливает единицу измерения. */
    fun selectUnit(value: LengthUnit) {
        unit = value
    }

    /** Включает или отключает показ информационных предупреждений. */
    fun updateSuppressWarnings(value: Boolean) {
        suppressWarnings = value
        settingsStore.save(AppSettings(suppressWarnings = value))
    }

    /** Устанавливает стиль отрисовки линейки. */
    fun setRulerStyle(style: RulerStyle) {
        ruler = ruler?.copy(style = style)
    }

    /** Устанавливает цвет указателя на север. */
    fun selectNorthStyle(style: NorthArrowStyle) {
        northStyle = style
        northArrow = northArrow?.copy(style = style)
    }

    /** Устанавливает размер указателя на север как долю короткой стороны. */
    fun setNorthRadius(fraction: Float) {
        northArrow = northArrow?.let { NorthArrowFactory.setRadiusFraction(it, fraction) }
    }

    /** Обновляет текст реального размера, если ввод допустим. */
    fun updateMmText(value: String) {
        if (isDecimalInput(value)) mmText = value
    }

    /** Обновляет текст количества ячеек, если ввод допустим. */
    fun updateCellCountText(value: String) {
        if (isIntegerInput(value)) cellCountText = value
    }

    /** Обновляет текст цены деления, если ввод допустим. */
    fun updateScaleText(value: String) {
        if (isDecimalInput(value)) scaleText = value
    }

    /** Запоминает размер области Canvas. */
    fun updateCanvasSize(size: Size) {
        canvasSize = size
    }

    /** Открывает предупреждение перед добавлением указателя. */
    fun requestAddNorth() {
        showNorthWarning = true
    }

    /** Закрывает предупреждение без добавления указателя. */
    fun dismissNorthWarning() {
        showNorthWarning = false
    }

    /** Открывает предупреждение перед входом в режим измерений. */
    fun requestMeasurementMode() {
        showMeasurementModeWarning = true
    }

    /** Закрывает предупреждение без входа в режим измерений. */
    fun dismissMeasurementModeWarning() {
        showMeasurementModeWarning = false
    }

    /** Открывает предупреждение перед удалением измерения. */
    fun requestDeleteMeasurement() {
        if (hasMeasurementToDelete) showMeasurementDeleteWarning = true
    }

    /** Закрывает предупреждение без удаления. */
    fun dismissDeleteMeasurementWarning() {
        showMeasurementDeleteWarning = false
    }

    /** Подтверждает удаление и закрывает предупреждение. */
    fun confirmDeleteMeasurement() {
        showMeasurementDeleteWarning = false
        removeLastMeasurement()
    }

    /** Открывает диалог пакетной обработки. */
    fun requestBatch() {
        showBatchDialog = true
    }

    /** Закрывает диалог пакетной обработки. */
    fun dismissBatch() {
        showBatchDialog = false
    }
}
