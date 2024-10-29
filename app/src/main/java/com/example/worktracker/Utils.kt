package com.example.worktracker

import com.example.worktracker.Constants.BREAK_START_KEY
import com.example.worktracker.Constants.BREAK_TOTAL_KEY
import com.example.worktracker.Constants.CLOCKED_IN_KEY
import com.example.worktracker.Constants.ON_BREAK_KEY
import com.example.worktracker.Constants.SHIFT_START_KEY
import com.example.worktracker.data.SharedPreferencesRepository
import java.text.SimpleDateFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utils {

    private lateinit var sharedPref: SharedPreferencesRepository
    var clock: Clock = Clock.systemUTC()

    fun initialize(sp: SharedPreferencesRepository) {
        sharedPref = sp
    }

    fun getCounter(): String {
        if (sharedPref.getBoolean(CLOCKED_IN_KEY, false)) {
            val currentDateTime = getTimeStamp()
            val clockedTime = sharedPref.getString(SHIFT_START_KEY, "")
            val time = getTimeDiff(clockedTime, currentDateTime)
            // get break time
            val breakTime = if (sharedPref.getBoolean(ON_BREAK_KEY, false)) {
                val breakTime = sharedPref.getString(BREAK_START_KEY, "")
                getTimeDiff(breakTime, currentDateTime)
            } else {
                sharedPref.getString(BREAK_TOTAL_KEY, "0:00")
            }
            return reformatTime(subtractBreakFromTotal(breakTime, time))
        }
        return "0m"
    }

    fun getBreakCounter(): String {
        if (sharedPref.getBoolean(ON_BREAK_KEY, false)) {
            val currentDateTime = getTimeStamp()
            val breakTime = sharedPref.getString(BREAK_START_KEY, "")
            return reformatTime(getTimeDiff(breakTime, currentDateTime))
        }
        return reformatTime(sharedPref.getString(BREAK_TOTAL_KEY, "0:00"))
    }

    fun getTimeStamp(): String {
        val utcInstant = Instant.now(clock)
        val utcZoneId = ZoneId.of("UTC")
        val utcZonedDateTime = ZonedDateTime.ofInstant(utcInstant, utcZoneId)

        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm a", Locale.US)
        return formatter.format(utcZonedDateTime)
    }

    fun getTimeDiff(timeStart: String, timeEnd: String): String {
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

    fun reformatTime(time: String): String {
        val halves = time.split(':')
        val minutes = if (halves[1][0] == '0') halves[1][1] else halves[1]

        return if (halves[0] == "0") {
            "${minutes}m"
        } else {
            "${halves[0]}h ${minutes}m"
        }
    }

    fun subtractBreakFromTotal(breakTotal: String, shiftTotal: String): String {
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

    fun getDisplayTimeAtTimeZone(timeZone: ZoneId, timestamp: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.hh:mm a", Locale.US)
        val localDateTime: LocalDateTime = LocalDateTime.parse(timestamp, formatter)

        val zoneId = ZoneId.of("UTC")
        val zonedDateTime = ZonedDateTime.of(localDateTime, zoneId)

        val targetZonedDateTime = zonedDateTime.withZoneSameInstant(timeZone)

        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        return targetZonedDateTime.format(outputFormatter)
    }
}