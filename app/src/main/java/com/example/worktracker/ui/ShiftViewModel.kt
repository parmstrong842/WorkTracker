package com.example.worktracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants
import com.example.worktracker.data.SharedPreferencesRepository
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

data class ShiftUiState(
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val breakTotal: String = "---",
    val total: String,
)

class ShiftViewModel(private val shiftsRepository: ShiftsRepository, sharedPref: SharedPreferencesRepository): ViewModel() {

    private val _uiState: MutableStateFlow<ShiftUiState>
    val uiState: StateFlow<ShiftUiState>

    private var startYear: String
    private val selectedTimeZone: ZoneId

    init{
        val timeZoneString = sharedPref.getString(Constants.TIME_ZONE_KEY, "UTC")
        selectedTimeZone = ZoneId.of(timeZoneString)

        startYear = getTimeStamp("u")

        val date = getTimeStamp("EEE, LLL d")
        val time = getTimeStamp("h:mm a")
        val total = getTotal(date, time, date, time, "0")

        _uiState = MutableStateFlow(ShiftUiState(date, time, date, time, total = total))
        uiState = _uiState.asStateFlow()
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

    fun updateBreakTotal(minutesTemp: String) {
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

    fun insertShift() {
        val date = getDateForInsert(startYear, uiState.value.startDate, uiState.value.startTime)//2023.01.24
        val shiftSpan = "${uiState.value.startTime} - ${uiState.value.endTime}"//6:21 PM - 6:23 PM
        val breakTotal = getBreakForInsert(uiState.value.breakTotal)//0:00
        val shiftTotal = uiState.value.total//0:00

        val shift = Shift(
                date = date,
                shiftSpan = shiftSpan,
                breakTotal = breakTotal,
                shiftTotal = shiftTotal
            )
        convertShiftToUTC(shift)

        viewModelScope.launch {
            shiftsRepository.insertItem(shift)
        }
    }

    private fun convertShiftToUTC(shift: Shift) {
        val timeTokens = shift.shiftSpan.split(" - ")

        val timestamp1 = "${shift.date}.${timeTokens[0]}"
        val userZonedDateTime1 = getZonedDateTime(timestamp1)
        val timestamp2 = "${shift.date}.${timeTokens[1]}"
        val userZonedDateTime2 = getZonedDateTime(timestamp2)

        if (userZonedDateTime2.isBefore(userZonedDateTime1)) {
            userZonedDateTime2.plusDays(1)
        }

        val datePattern = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        val timePattern = DateTimeFormatter.ofPattern("h:mm a")

        val newDate = userZonedDateTime1.format(datePattern)
        val newTimeToken1 = userZonedDateTime1.format(timePattern)
        val newTimeToken2 = userZonedDateTime2.format(timePattern)
        val newShiftSpan = "$newTimeToken1 - $newTimeToken2"

        shift.date = newDate
        shift.shiftSpan = newShiftSpan
    }

    private fun getZonedDateTime(timestamp: String): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.h:mm a")

        val localDateTime = LocalDateTime.parse(timestamp, formatter)

        val zonedDateTime = ZonedDateTime.of(localDateTime, selectedTimeZone)

        val zoneId = ZoneId.of("UTC")
        return zonedDateTime.withZoneSameInstant(zoneId)
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

    private fun getTimeStamp(pattern: String): String {
        val time = ZonedDateTime.now(selectedTimeZone)
        return DateTimeFormatter.ofPattern(pattern).format(time)
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

    fun getDatePickerStart(): Triple<Int, Int, Int> {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("LLL d")
            .parseDefaulting(ChronoField.YEAR, 1970)
            .toFormatter(Locale.US)
        val date = LocalDate.parse(uiState.value.startDate.substring(5), formatter)
        return Triple(startYear.toInt(), date.month.value-1, date.dayOfMonth)
    }

    fun getDatePickerEnd(): Triple<Int, Int, Int> {
        val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendPattern("LLL d")
            .parseDefaulting(ChronoField.YEAR, 1970)
            .toFormatter(Locale.US)
        val date = LocalDate.parse(uiState.value.endDate.substring(5), formatter)
        return Triple(startYear.toInt(), date.month.value-1, date.dayOfMonth)
    }
}