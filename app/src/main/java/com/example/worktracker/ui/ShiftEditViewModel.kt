package com.example.worktracker.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants
import com.example.worktracker.data.SharedPreferencesRepository
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

class ShiftEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val shiftsRepository: ShiftsRepository,
    sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val shiftId: Int = checkNotNull(savedStateHandle[ShiftEditDestination.shiftIdArg])

    private val _uiState: MutableStateFlow<ShiftUiState>
    val uiState: StateFlow<ShiftUiState>

    private lateinit var startYear: String
    private val selectedTimeZone: ZoneId

    init {
        val timeZoneString = sharedPreferencesRepository.getString(Constants.TIME_ZONE_KEY, "UTC")
        selectedTimeZone = ZoneId.of(timeZoneString)

        _uiState = MutableStateFlow(ShiftUiState("---", "---", "---", "---", "---", "---"))
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            val shift = fetchItem()
            updateUiState(checkNotNull(shift))
            startYear = shift.date.split('.')[0]
        }
    }

    private suspend fun fetchItem(): Shift? {
        return shiftsRepository.getItemStream(shiftId)
    }

    private fun updateUiState(shift: Shift) {
        _uiState.update { buildShiftUiState(shift) }
    }

    fun updateStartDate(year: Int, month: Int, dayOfMonth: Int) {
        startYear = year.toString()
        _uiState.update { currentState ->
            currentState.copy(
                startDate = getDate(year, month, dayOfMonth)
            )
        }
    }

    fun updateEndDate(year: Int, month: Int, dayOfMonth: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                endDate = getDate(year, month, dayOfMonth)
            )
        }
    }

    fun updateStartTime(hour: Int, minute: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                startTime = getStringFromTime(hour, minute)
            )
        }
    }

    fun updateEndTime(hour: Int, minute: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                endTime = getStringFromTime(hour, minute)
            )
        }
    }

    fun updateBreakTotal(minutesTemp: String) {//TODO handle very big break input
        var minutes = minutesTemp
        if (minutes == "") {
            minutes = "---"
        }

        val l = minutes.toLongOrNull()
        if (l == null) {
            if (minutes != "---") {
                return
            }
        }
        _uiState.update { currentState ->
            currentState.copy(
                breakTotal = minutes
            )
        }
    }

    fun updateTotal() {
        _uiState.update { currentState ->
            currentState.copy(
                total = getTotal(uiState.value.startDate, uiState.value.startTime, uiState.value.endDate, uiState.value.endTime, uiState.value.breakTotal)
            )
        }
    }

    private fun getBreakForInsert(minutes: String): String {
        if (minutes == "---") return "0:00"

        val min = minutes.toInt() % 60
        val hours = minutes.toInt() / 60

        return String.format("%d:%02d", hours, min)
    }

    private fun getTotal(startDate: String, startTime: String, endDate: String, endTime: String, breakTotal: String): String {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("LLL d h:mm a")
            .parseDefaulting(ChronoField.YEAR, 1970)
            .toFormatter(Locale.US)
        val start = LocalDateTime.parse("${startDate.substring(5)} $startTime", formatter)
        val end = LocalDateTime.parse("${endDate.substring(5)} $endTime", formatter)
        var total = Duration.between(start, end)
        total = total.minus(Duration.of(breakTotal.toLongOrNull() ?: 0, ChronoUnit.MINUTES))

        var seconds = total.get(ChronoUnit.SECONDS)
        val negative = seconds < 0
        seconds = abs(seconds)
        val minutes = seconds / (60) % 60
        val hours = seconds / (60 * 60)

        return String.format("${if (negative) "-" else ""}%d:%02d", hours, minutes)
    }

    private fun getStringFromTime(hour: Int, minute: Int): String {
        val time = LocalTime.parse("$hour:$minute", DateTimeFormatter.ofPattern("H:m"))
        return DateTimeFormatter.ofPattern("h:mm a").format(time)
    }

    private fun getDate(year: Int, month: Int, dayOfMonth: Int): String {
        val date = LocalDate.of(year, month+1, dayOfMonth)
        return DateTimeFormatter.ofPattern("EEE, LLL d").format(date)
    }

    private fun getDateForInsert(startYear: String, startDate: String, startTime: String): String {
        val date = LocalDateTime.parse("$startYear $startDate $startTime",
            DateTimeFormatter.ofPattern("u EEE, LLL d h:mm a"))
        return DateTimeFormatter.ofPattern("u.MM.dd").format(date)
    }


    private fun buildShiftUiState(shift: Shift): ShiftUiState {
        convertShiftToDifferentTimeZone(shift, ZoneId.of("UTC"), selectedTimeZone)

        val date = LocalDate.parse(shift.date, DateTimeFormatter.ofPattern("u.MM.dd"))
        val startDate = DateTimeFormatter.ofPattern("EEE, LLL d").format(date)
        val times = shift.shiftSpan.split(" - ")
        val startTime = times[0]
        val endTime = times[1]
        val breakTotal = getBreakInMinutes(shift.breakTotal)
        val total = shift.shiftTotal

        var dateTime = LocalDateTime.parse("${shift.date} $startTime", DateTimeFormatter.ofPattern("u.MM.dd h:mm a"))
        val tokens = shift.shiftTotal.split(':')
        dateTime = dateTime.plusHours(tokens[0].toLong())
        dateTime = dateTime.plusMinutes(tokens[1].toLong())
        val endDate = DateTimeFormatter.ofPattern("EEE, LLL d").format(dateTime)

        return ShiftUiState(
            startDate = startDate,
            startTime = startTime,
            endDate = endDate,
            endTime = endTime,
            breakTotal = breakTotal,
            total = total
        )
    }

    fun updateShift() {
        val date = getDateForInsert(startYear, uiState.value.startDate, uiState.value.startTime)//2023.01.24
        val shiftSpan = "${uiState.value.startTime} - ${uiState.value.endTime}"//6:21 PM - 6:23 PM
        val breakTotal = getBreakForInsert(uiState.value.breakTotal)//0:00
        val shiftTotal = uiState.value.total//0:00

        val shift = Shift(shiftId, date, shiftSpan, breakTotal, shiftTotal)
        convertShiftToDifferentTimeZone(shift, selectedTimeZone, ZoneId.of("UTC"))

        viewModelScope.launch {
            shiftsRepository.updateItem(shift)
        }
    }

    private fun convertShiftToDifferentTimeZone(shift: Shift, startZone: ZoneId, endZone: ZoneId) {
        val timeTokens = shift.shiftSpan.split(" - ")

        val timestamp1 = "${shift.date}.${timeTokens[0]}"
        val zonedDateTime1 = getZonedDateTime(timestamp1, startZone, endZone)
        val timestamp2 = "${shift.date}.${timeTokens[1]}"
        val zonedDateTime2 = getZonedDateTime(timestamp2, startZone, endZone)

        if (zonedDateTime2.isBefore(zonedDateTime1)) {
            zonedDateTime2.plusDays(1)
        }

        val datePattern = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val timePattern = DateTimeFormatter.ofPattern("h:mm a")

        val newDate = zonedDateTime1.format(datePattern)
        val newTimeToken1 = zonedDateTime1.format(timePattern)
        val newTimeToken2 = zonedDateTime2.format(timePattern)

        val newShiftSpan = "$newTimeToken1 - $newTimeToken2"
        shift.date = newDate
        shift.shiftSpan = newShiftSpan
    }

    private fun getZonedDateTime(timestamp: String, startZone: ZoneId, endZone: ZoneId): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.h:mm a")
        val localDateTime = LocalDateTime.parse(timestamp, formatter)
        val zonedDateTime = ZonedDateTime.of(localDateTime, startZone)
        return zonedDateTime.withZoneSameInstant(endZone)
    }

    fun deleteShift() {
        viewModelScope.launch {
            shiftsRepository.deleteItem(
                Shift(shiftId, "", "", "", "")
            )
        }
    }

    private fun getBreakInMinutes(breakTotal: String): String {
        val tokens = breakTotal.split(':')
        val hours = tokens[0].toInt()
        val minutes = tokens[1].toInt()
        return (hours * 60 + minutes).toString()
    }

    fun getDatePickerStart(): Triple<Int, Int, Int> {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("LLL d")
            .parseDefaulting(ChronoField.YEAR, 1970)
            .toFormatter(Locale.US)
        val date = LocalDate.parse(uiState.value.startDate.substring(5), formatter)
        return Triple(startYear.toInt(), date.month.value-1, date.dayOfMonth)
    }

    fun getDatePickerEnd(): Triple<Int, Int, Int> {//TODO it should return the end year instead of startYear
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("LLL d")
            .parseDefaulting(ChronoField.YEAR, 1970)
            .toFormatter(Locale.US)
        val date = LocalDate.parse(uiState.value.endDate.substring(5), formatter)
        return Triple(startYear.toInt(), date.month.value-1, date.dayOfMonth)
    }
}