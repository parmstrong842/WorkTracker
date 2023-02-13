package com.example.worktracker.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class LogUiState(
    val itemList: List<Shift> = listOf(),
    val startDate: LocalDate,
    val endDate: LocalDate = startDate.plusWeeks(1),
    val tabState: Int = 0,
)

class LogViewModel(private val shiftsRepository: ShiftsRepository) : ViewModel() {

    private val _uiState: MutableStateFlow<LogUiState>
    val uiState: StateFlow<LogUiState>

    private var allShiftsList = listOf<Shift>()

    init {
        Log.d("LogViewModel", "LogViewModel Created")
        viewModelScope.launch {
            allShiftsList = shiftsRepository.getAllItemsStream()
            updateItemList()
        }

        _uiState = MutableStateFlow(LogUiState(listOf(), getStartOfWeek()))
        uiState = _uiState.asStateFlow()
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
        val sunday = getStartOfWeek()
        val nextSunday = sunday.plusWeeks(1)
        return Pair(sunday, nextSunday)
    }

    private fun getMonthDuration(): Pair<LocalDate, LocalDate> {
        val start = getStartOfMonth()
        val end = start.plusMonths(1)
        return Pair(start, end)
    }

    private fun getYearDuration(): Pair<LocalDate, LocalDate> {
        val start = getStartOfYear()
        val end = start.plusYears(1)
        return Pair(start, end)
    }

    private fun getStartOfWeek(): LocalDate {
        var now = ZonedDateTime.now(ZoneId.of("America/Chicago"))
        while (now.dayOfWeek != DayOfWeek.SUNDAY) {
            now = now.minusDays(1)
        }
        return now.toLocalDate()
    }

    private fun getStartOfMonth(): LocalDate {
        var now = ZonedDateTime.now(ZoneId.of("America/Chicago"))
        while (now.dayOfMonth != 1) {
            now = now.minusDays(1)
        }
        return now.toLocalDate()
    }

    private fun getStartOfYear(): LocalDate {
        var now = ZonedDateTime.now(ZoneId.of("America/Chicago"))
        while (now.dayOfYear != 1) {
            now = now.minusDays(1)
        }
        return now.toLocalDate()
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