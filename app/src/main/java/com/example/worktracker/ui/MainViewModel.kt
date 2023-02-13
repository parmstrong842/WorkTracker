package com.example.worktracker.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.worktracker.Constants.breakStartKey
import com.example.worktracker.Constants.breakTimeStampKey
import com.example.worktracker.Constants.breakTotalKey
import com.example.worktracker.Constants.clockedInKey
import com.example.worktracker.Constants.onBreakKey
import com.example.worktracker.Constants.prefsFileName
import com.example.worktracker.Constants.shiftStartKey
import com.example.worktracker.Constants.timeStampKey
import com.example.worktracker.data.Shift
import com.example.worktracker.data.ShiftsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

class MainViewModel(app: Application, private val shiftsRepository: ShiftsRepository) : AndroidViewModel(app) {

    //TODO if shift is longer then 24h it messes up

    private val sharedPref: SharedPreferences =
        getApplication<Application>().getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)

    private val _uiState: MutableStateFlow<MainUiState>
    val uiState: StateFlow<MainUiState>

    init {
        Log.d("MainViewModel", "MainViewModel Created")

        val clockedIn = sharedPref.getBoolean(clockedInKey, false)
        val onBreak = sharedPref.getBoolean(onBreakKey, false)
        val shiftStartTime = sharedPref.getString(shiftStartKey, "error")
        val breakStartTime = sharedPref.getString(breakStartKey, "error")
        val breakTotal = sharedPref.getString(breakTotalKey, "0:00")
        val counter = getCounter()
        val breakCounter = getBreakCounter()

        _uiState = MutableStateFlow(
            MainUiState(
                clockedIn,
                onBreak,
                checkNotNull(shiftStartTime),
                checkNotNull(breakStartTime),
                checkNotNull(breakTotal),
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
        if (sharedPref.getBoolean(clockedInKey, false)) {
            val currentDateTime = getTimeStamp()
            val clockedTime = checkNotNull(sharedPref.getString(timeStampKey, "")).substring(9)
            val time = getTimeDiff(clockedTime, currentDateTime)
            // get break time
            val breakTime = if (sharedPref.getBoolean(onBreakKey, false)) {
                val breakTime = checkNotNull(sharedPref.getString(breakTimeStampKey, "")).substring(12)
                getTimeDiff(breakTime, currentDateTime)
            } else {
                checkNotNull(sharedPref.getString(breakTotalKey, "0:00"))
            }
            return reformatTime(subtractBreakFromTotal(breakTime, time))
        }
        return "0m"
    }

    private fun getBreakCounter(): String {
        if (sharedPref.getBoolean(onBreakKey, false)) {
            val currentDateTime = getTimeStamp()
            val breakTime = checkNotNull(sharedPref.getString(breakTimeStampKey, "")).substring(12)
            return reformatTime(getTimeDiff(breakTime, currentDateTime))
        }
        return reformatTime(checkNotNull(sharedPref.getString(breakTotalKey, "0:00")))
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

    //                                    clock                   break
    //clocked in  / on break       clock out/end break          end break
    //clocked in  / not break           clock out               start break
    //clocked out / not break           clock in                 invalid

    fun updateClockedIn() {
        val clockedIn = uiState.value.clockedIn

        if (uiState.value.onBreak){
            updateOnBreak()
        }

        var shiftStartTime = uiState.value.shiftStartTime
        if (!clockedIn) { //clocking in
            //CLOCK_IN 2023.01.22.12:12 PM
            val timestamp = "CLOCK_IN ${getTimeStamp()}"
            shiftStartTime = if (timestamp[20] == '0') timestamp.substring(21, 28) else timestamp.substring(20, 28)
            with (sharedPref.edit()) {
                putBoolean(clockedInKey, true)
                putString(shiftStartKey, shiftStartTime)
                putString(timeStampKey, timestamp)
                apply()
            }
        } else { // clocking out
            val timestamp = "${sharedPref.getString(timeStampKey, "")}CLOCK_OUT ${getTimeStamp()}"

            createShiftAndInsert(timestamp)

            with (sharedPref.edit()) {
                putBoolean(clockedInKey, false)
                remove(breakTotalKey)
                remove(timeStampKey)
                apply()
            }
        }
        _uiState.update { currentState ->
            currentState.copy(
                clockedIn = !clockedIn,
                shiftStartTime = shiftStartTime
            )
        }
    }


    private fun createShiftAndInsert(timestamp: String) {
        //CLOCK_IN 2023.01.22.12:12 PMCLOCK_OUT 2023.01.22.12:12 PM

        val date = timestamp.substring(9, 19)

        val breakTime = sharedPref.getString(breakTotalKey, "0:00")

        val shiftLength = getTimeDiff(timestamp.substring(9, 28), timestamp.substring(38))
        val shiftTotal = subtractBreakFromTotal(breakTime!!, shiftLength)

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

        var breakStartTime = uiState.value.breakStartTime
        var breakTotal = uiState.value.breakTotal
        if (!onBreak) {
            val timestamp = "START_BREAK ${getTimeStamp()}"
            breakStartTime = if (timestamp[23] == '0') timestamp.substring(24, 31) else timestamp.substring(23, 31)
            with (sharedPref.edit()) {
                putBoolean(onBreakKey, true)
                putString(breakStartKey, breakStartTime)
                putString(breakTimeStampKey, timestamp)
                apply()
            }
        } else {
            val timestamp = "${sharedPref.getString(breakTimeStampKey, "")}END_BREAK ${getTimeStamp()}"

            breakTotal = getBreakTotal(timestamp)

            with (sharedPref.edit()) {
                putBoolean(onBreakKey, false)
                putString(breakTotalKey, breakTotal)
                remove(breakTimeStampKey)
                apply()
            }
        }

        _uiState.update { currentState ->
            currentState.copy(
                onBreak = !onBreak,
                breakStartTime = breakStartTime,
                breakTotal = breakTotal
            )
        }

    }

    private fun getBreakTotal(timestamp: String): String {
        //START_BREAK 2023.01.17.19:50 AMEND_BREAK2023.01.17.19:50 AM

        val timeStart = timestamp.substring(12, 31)
        val timeEnd = timestamp.substring(40)

        return getTimeDiff(timeStart, timeEnd)
    }

    private fun getTimeDiff(timeStart: String, timeEnd: String): String {
        val format = SimpleDateFormat("yyyy.MM.dd.hh:mm a", Locale.getDefault())
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
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
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
        val z = ZoneId.of("America/Chicago") // Or get the JVM’s current default time zone: ZoneId.systemDefault()
        val time = ZonedDateTime.now(z)
        return DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm a").format(time)
    }
}