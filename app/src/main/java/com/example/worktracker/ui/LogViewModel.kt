package com.example.worktracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants.START_OF_WEEK_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.data.SharedPreferencesRepository
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class LogUiState(
    val itemList: List<Shift> = listOf(),
    val startDate: LocalDate,
    val endDate: LocalDate = startDate.plusWeeks(1),
    val tabState: Int = 0,
)

class LogViewModel(
    private val shiftsRepository: ShiftsRepository,
    sharedPref: SharedPreferencesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<LogUiState>
    val uiState: StateFlow<LogUiState>

    private val selectedTimeZone: ZoneId
    private val selectedStartOfWeek: DayOfWeek

    private var allShiftsList = listOf<Shift>()

    init {
        val timeZoneString = sharedPref.getString(TIME_ZONE_KEY, "UTC")
        selectedTimeZone = ZoneId.of(timeZoneString)

        val dayOfWeekString = sharedPref.getString(START_OF_WEEK_KEY, "SUNDAY")
        selectedStartOfWeek = DayOfWeek.valueOf(dayOfWeekString)

        _uiState = MutableStateFlow(LogUiState(startDate = getStartOfWeek(selectedStartOfWeek, selectedTimeZone)))
        uiState = _uiState.asStateFlow()

        viewModelScope.launch {
            allShiftsList = convertShiftsToUserTimeZone(shiftsRepository.getAllItemsStream())
            updateItemList()
        }
    }

    private fun convertShiftsToUserTimeZone(list: List<Shift>): List<Shift> {
        list.forEach {
            val timeTokens = it.shiftSpan.split(" - ")

            val timestamp1 = "${it.date}.${timeTokens[0]}"
            val userZonedDateTime1 = getZonedDateTime(timestamp1)
            val timestamp2 = "${it.date}.${timeTokens[1]}"
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
            it.date = newDate
            it.shiftSpan = newShiftSpan
        }
        return list
    }

    private fun getZonedDateTime(timestamp: String): ZonedDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.h:mm a")

        val localDateTime = LocalDateTime.parse(timestamp, formatter)

        val zoneId = ZoneId.of("UTC")
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)

        return zonedDateTime.withZoneSameInstant(selectedTimeZone)
    }

    private fun updateItemList() {
        _uiState.update { currentState ->
            currentState.copy(
                itemList = getShiftsForDatesBetween(allShiftsList, uiState.value.startDate, uiState.value.endDate)
            )
        }
    }

    fun minusDate() {
        val (newStartDate, newEndDate) = when (uiState.value.tabState) {
            0 -> subtractWeek()
            1 -> subtractMonth()
            2 -> subtractYear()
            else -> Pair(LocalDate.MIN, LocalDate.MAX)
        }

        _uiState.update {
            it.copy(
                startDate = newStartDate,
                endDate = newEndDate
            )
        }

        updateItemList()
    }

    fun plusDate() {
        val (newStartDate, newEndDate) = when (uiState.value.tabState) {
            0 -> plusWeek()
            1 -> plusMonth()
            2 -> plusYear()
            else -> Pair(LocalDate.MIN, LocalDate.MAX)
        }

        _uiState.update {
            it.copy(
                startDate = newStartDate,
                endDate = newEndDate
            )
        }

        updateItemList()
    }

    fun updateTabState(index: Int) {
        val (newStartDate, newEndDate) = when (index) {
            0 -> getWeekDuration()
            1 -> getMonthDuration()
            2 -> getYearDuration()
            else -> Pair(LocalDate.MIN, LocalDate.MAX)
        }
        _uiState.update {
            it.copy(
                startDate = newStartDate,
                endDate = newEndDate,
                tabState = index
            )
        }

        updateItemList()
    }

    private fun subtractWeek(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.minusWeeks(1),
            uiState.value.endDate.minusWeeks(1)
        )
    }

    private fun subtractMonth(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.minusMonths(1),
            uiState.value.endDate.minusMonths(1)
        )
    }

    private fun subtractYear(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.minusYears(1),
            uiState.value.endDate.minusYears(1)
        )
    }

    private fun plusWeek(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.plusWeeks(1),
            uiState.value.endDate.plusWeeks(1)
        )
    }

    private fun plusMonth(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.plusMonths(1),
            uiState.value.endDate.plusMonths(1)
        )
    }

    private fun plusYear(): Pair<LocalDate, LocalDate> {
        return Pair(
            uiState.value.startDate.plusYears(1),
            uiState.value.endDate.plusYears(1)
        )
    }

    private fun getWeekDuration(): Pair<LocalDate, LocalDate> {
        val start = getStartOfWeek(selectedStartOfWeek, selectedTimeZone)
        val end = start.plusWeeks(1)
        return Pair(start, end)
    }

    private fun getMonthDuration(): Pair<LocalDate, LocalDate> {
        val start = getStartOfMonth(selectedTimeZone)
        val end = start.plusMonths(1)
        return Pair(start, end)
    }

    private fun getYearDuration(): Pair<LocalDate, LocalDate> {
        val start = getStartOfYear(selectedTimeZone)
        val end = start.plusYears(1)
        return Pair(start, end)
    }

    private fun getStartOfWeek(selectedDayOfWeek: DayOfWeek, selectedZone: ZoneId): LocalDate {
        val now = ZonedDateTime.now(selectedZone)
        return now.with(TemporalAdjusters.previousOrSame(selectedDayOfWeek)).toLocalDate()
    }

    private fun getStartOfMonth(selectedZone: ZoneId): LocalDate {
        val now = ZonedDateTime.now(selectedZone)
        return now.withDayOfMonth(1).toLocalDate()
    }

    private fun getStartOfYear(selectedZone: ZoneId): LocalDate {
        val now = ZonedDateTime.now(selectedZone)
        return now.withDayOfYear(1).toLocalDate()
    }

    private fun getShiftsForDatesBetween(shiftList: List<Shift>, start: LocalDate, end: LocalDate): List<Shift> {
        val list = mutableListOf<Shift>()
        shiftList.forEach {
            val shiftDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("u.MM.dd"))
            if (shiftDate.isAfter(start) || shiftDate.isEqual(start)) {
                if (shiftDate.isBefore(end)) {
                    list.add(it)
                }
            }
        }
        return list.toList()
    }
}