package com.example.worktracker.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants.BREAK_START_KEY
import com.example.worktracker.Constants.BREAK_TIME_STAMP_KEY
import com.example.worktracker.Constants.BREAK_TOTAL_KEY
import com.example.worktracker.Constants.CLOCKED_IN_KEY
import com.example.worktracker.Constants.ON_BREAK_KEY
import com.example.worktracker.Constants.SHIFT_START_KEY
import com.example.worktracker.Constants.TIME_STAMP_KEY
import com.example.worktracker.Constants.TIME_ZONE_KEY
import com.example.worktracker.data.SharedPreferencesRepository
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


data class MainUiState(
    val clockedIn: Boolean,
    val onBreak: Boolean,
    val shiftStartTime: String,
    val breakStartTime: String,
    val breakTotal: String,
    val counter: String,
    val breakCounter: String,
)

class MainViewModel(
    private val shiftsRepository: ShiftsRepository,
    private val sharedPref: SharedPreferencesRepository,
) : ViewModel() {

    var clock: Clock = Clock.systemUTC()

    //TODO if shift is longer then 24h it messes up

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>

    private val selectedTimeZone: ZoneId

    init {
        val timeZoneString = sharedPref.getString(TIME_ZONE_KEY, "UTC")
        selectedTimeZone = ZoneId.of(timeZoneString)

        val clockedIn = sharedPref.getBoolean(CLOCKED_IN_KEY, false)
        val onBreak = sharedPref.getBoolean(ON_BREAK_KEY, false)

        val timestampStart = sharedPref.getString(SHIFT_START_KEY, "error")
        val shiftStartTime = if (timestampStart != "error") getDisplayTimeAtTimeZone(selectedTimeZone, timestampStart) else "error"

        val timestampBreak = sharedPref.getString(BREAK_START_KEY, "error")
        val breakStartTime = if (timestampBreak != "error") getDisplayTimeAtTimeZone(selectedTimeZone, timestampBreak) else "error"

        val breakTotal = sharedPref.getString(BREAK_TOTAL_KEY, "0:00")
        val counter = getCounter()
        val breakCounter = getBreakCounter()

        _uiState = MutableStateFlow(
            MainUiState(
                clockedIn,
                onBreak,
                shiftStartTime,
                breakStartTime,
                breakTotal,
                counter,
                breakCounter
            )
        )
        uiState = _uiState.asStateFlow()

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                _uiState.update {
                    it.copy(
                        counter = getCounter(),
                        breakCounter = getBreakCounter()
                    )
                }
                handler.postDelayed(this, 1000L)
            }
        }
        handler.post(runnable)
    }

    private fun getCounter(): String {
        if (sharedPref.getBoolean(CLOCKED_IN_KEY, false)) {
            val currentDateTime = getTimeStamp()
            val clockedTime = checkNotNull(sharedPref.getString(TIME_STAMP_KEY, "")).substring(9)
            val time = getTimeDiff(clockedTime, currentDateTime)
            // get break time
            val breakTime = if (sharedPref.getBoolean(ON_BREAK_KEY, false)) {
                val breakTime = checkNotNull(sharedPref.getString(BREAK_TIME_STAMP_KEY, "")).substring(12)
                getTimeDiff(breakTime, currentDateTime)
            } else {
                checkNotNull(sharedPref.getString(BREAK_TOTAL_KEY, "0:00"))
            }
            return reformatTime(subtractBreakFromTotal(breakTime, time))
        }
        return "0m"
    }

    private fun getBreakCounter(): String {
        if (sharedPref.getBoolean(ON_BREAK_KEY, false)) {
            val currentDateTime = getTimeStamp()
            val breakTime = checkNotNull(sharedPref.getString(BREAK_TIME_STAMP_KEY, "")).substring(12)
            return reformatTime(getTimeDiff(breakTime, currentDateTime))
        }
        return reformatTime(checkNotNull(sharedPref.getString(BREAK_TOTAL_KEY, "0:00")))
    }

    private fun reformatTime(time: String): String {
        val halves = time.split(':')
        val minutes = if (halves[1][0] == '0') halves[1][1] else halves[1]

        return if (halves[0] == "0") {
            "${minutes}m"
        } else {
            "${halves[0]}h ${minutes}m"
        }
    }

    fun updateClockedIn() {
        val clockedIn = uiState.value.clockedIn

        if (uiState.value.onBreak){
            updateOnBreak()
        }

        if (!clockedIn) { //clocking in
            //CLOCK_IN 2023.01.22.12:12 PM
            val timestamp = getTimeStamp()
            val timestampLog = "CLOCK_IN $timestamp"

            val shiftStartTimeUser = getDisplayTimeAtTimeZone(selectedTimeZone, timestamp)

            sharedPref.putBoolean(CLOCKED_IN_KEY, true)
            sharedPref.putString(SHIFT_START_KEY, timestamp)
            sharedPref.putString(TIME_STAMP_KEY, timestampLog)

            _uiState.update { currentState ->
                currentState.copy(
                    clockedIn = true,
                    shiftStartTime = shiftStartTimeUser
                )
            }
        } else { // clocking out
            val timestamp = "${sharedPref.getString(TIME_STAMP_KEY, "")}CLOCK_OUT ${getTimeStamp()}"

            createShiftAndInsert(timestamp)

            sharedPref.putBoolean(CLOCKED_IN_KEY, false)
            sharedPref.remove(BREAK_TOTAL_KEY)
            sharedPref.remove(TIME_STAMP_KEY)

            _uiState.update { currentState ->
                currentState.copy(
                    clockedIn = false,
                )
            }
        }
    }

    private fun createShiftAndInsert(timestamp: String) {
        //CLOCK_IN 2023.01.22.12:12 PMCLOCK_OUT 2023.01.22.12:12 PM

        val date = timestamp.substring(9, 19)

        val breakTime = sharedPref.getString(BREAK_TOTAL_KEY, "0:00")

        val shiftLength = getTimeDiff(timestamp.substring(9, 28), timestamp.substring(38))
        val shiftTotal = subtractBreakFromTotal(breakTime, shiftLength)

        val timeStart = if (timestamp[20] == '0') timestamp.substring(21, 28) else timestamp.substring(20, 28)
        val timeEnd = if (timestamp[49] == '0') timestamp.substring(50) else timestamp.substring(49)

        val shift = Shift(
            date = date,
            shiftSpan = "$timeStart - $timeEnd",
            breakTotal = breakTime,
            shiftTotal = shiftTotal
        )

        viewModelScope.launch {
            shiftsRepository.insertItem(shift)
        }
    }

    fun updateOnBreak() {
        val onBreak = uiState.value.onBreak

        if (!uiState.value.clockedIn) {
            return
        }

        if (!onBreak) {
            val timestamp = getTimeStamp()
            val timestampLog = "START_BREAK $timestamp"

            val breakStartTime = getDisplayTimeAtTimeZone(selectedTimeZone, timestamp)

            sharedPref.putBoolean(ON_BREAK_KEY, true)
            sharedPref.putString(BREAK_START_KEY, timestamp)
            sharedPref.putString(BREAK_TIME_STAMP_KEY, timestampLog)

            _uiState.update { currentState ->
                currentState.copy(
                    onBreak = true,
                    breakStartTime = breakStartTime,
                )
            }
        } else {
            val timestamp = "${sharedPref.getString(BREAK_TIME_STAMP_KEY, "")}END_BREAK ${getTimeStamp()}"

            val breakTotal = getBreakTotal(timestamp)

            sharedPref.putBoolean(ON_BREAK_KEY, false)
            sharedPref.putString(BREAK_TOTAL_KEY, breakTotal)
            sharedPref.remove(BREAK_TIME_STAMP_KEY)

            _uiState.update { currentState ->
                currentState.copy(
                    onBreak = false,
                    breakTotal = breakTotal
                )
            }
        }
    }


    private fun getBreakTotal(timestamp: String): String {
        //START_BREAK 2023.01.17.19:50 AMEND_BREAK2023.01.17.19:50 AM

        val timeStart = timestamp.substring(12, 31)
        val timeEnd = timestamp.substring(40)

        return getTimeDiff(timeStart, timeEnd)
    }

    private fun getTimeDiff(timeStart: String, timeEnd: String): String {
        val format = SimpleDateFormat("yyyy.MM.dd.hh:mm a", Locale.US)
        val date1: Date? = format.parse(timeStart)
        val date2: Date? = format.parse(timeEnd)
        if (date1 != null && date2 != null) {
            val diff = date2.time - date1.time
            val diffMinutes = diff / (60 * 1000) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24

            return String.format("%d:%02d", diffHours, diffMinutes)

        }
        return "invalid date"
    }

    private fun subtractBreakFromTotal(breakTotal: String, shiftTotal: String): String {
        val format = SimpleDateFormat("HH:mm", Locale.US)
        val date1: Date? = format.parse(shiftTotal)
        val date2: Date? = format.parse(breakTotal)
        if (date1 != null && date2 != null) {
            val diff = date1.time - date2.time
            val diffMinutes = diff / (60 * 1000) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24

            return String.format("%d:%02d", diffHours, diffMinutes)
        }
        return "invalid date"
    }

    private fun getTimeStamp(): String {
        val utcInstant = Instant.now(clock)
        val utcZoneId = ZoneId.of("UTC")
        val utcZonedDateTime = ZonedDateTime.ofInstant(utcInstant, utcZoneId)

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm a", Locale.US)
        return formatter.format(utcZonedDateTime)
    }

    private fun getDisplayTimeAtTimeZone(timeZone: ZoneId, timestamp: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm a", Locale.US)
        val localDateTime: LocalDateTime = LocalDateTime.parse(timestamp, formatter)

        val zoneId = ZoneId.of("UTC")
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)

        val targetZonedDateTime = zonedDateTime.withZoneSameInstant(timeZone)

        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        return targetZonedDateTime.format(outputFormatter)
    }
}
