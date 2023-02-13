package com.example.worktracker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

class UnitTests {


    @Test
    fun subtractBreakFromTotalTest() {
        val list = listOf(
            Triple("0:15", "8:10", "7:55"),
            Triple("0:00", "8:10", "8:10"),
            Triple("0:30", "10:00", "9:30"),
            Triple("0:30", "11:00", "10:30"),
        )

        list.forEach {
            assertEquals(subtractBreakFromTotal(it.first, it.second), it.third)
        }
    }

    @Test
    fun timeTest() {
        val time = getTimeStamp()
        println(time)
    }

    @Test
    fun getBreakTotalTest() {
        val list = listOf(
            Pair("START_BREAK 2023.01.17.00:00 AMEND_BREAK2023.01.17.00:00 AM", "0m"),
            Pair("START_BREAK 2023.01.17.00:00 AMEND_BREAK2023.01.17.00:01 AM", "1m"),
            Pair("START_BREAK 2023.01.17.00:00 AMEND_BREAK2023.01.17.00:30 AM", "30m"),
            Pair("START_BREAK 2023.01.17.00:00 AMEND_BREAK2023.01.17.01:00 AM", "1h 0m"),
            Pair("START_BREAK 2023.01.17.00:00 AMEND_BREAK2023.01.17.20:50 AM", "20h 50m"),
        )

        list.forEach {
            assertEquals(getBreakTotal(it.first), it.second)
        }
    }

    @Test
    fun getTimeDiffTest() {
        val list = listOf(
            Triple("2023.01.17.00:00 AM", "2023.01.17.00:00 AM", "0:00"),
            Triple("2023.01.24.08:55 AM", "2023.01.25.12:09 PM", "3:14"),

            )

        list.forEach {
            assertEquals(getTimeDiff(it.first, it.second), it.third)
        }
    }

    @Test
    fun getHourAndMinuteTest() {
        val list = listOf(
            Pair("4:55 AM", Pair(4, 55)),
            Pair("12:55 AM", Pair(0, 55)),
        )

        list.forEach {
            assertEquals(getHourAndMinute(it.first), it.second)
        }
    }

    private fun getHourAndMinute(str: String): Pair<Int, Int> {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val time = LocalTime.parse(str, formatter)
        return Pair(time.hour, time.minute)
    }

    @Test
    fun getTotalTest() {
        val list = listOf(
            listOf("Wed, Jan 25", "1:18 PM", "Wed, Jan 25", "1:18 PM", "0", "0:00"),
            listOf("Wed, Jan 25", "1:18 PM", "Thu, Jan 26", "1:18 PM", "0", "24:00"),
            listOf("Wed, Jan 25", "1:18 PM", "Fri, Jan 27", "1:18 PM", "0", "48:00"),
            listOf("Wed, Jan 25", "1:18 PM", "Fri, Jan 27", "1:18 PM", "0", "48:00"),
            listOf("Sun, Jan 1", "12:00 AM", "Sun, Jan 1", "1:00 AM", "0", "1:00"),
            listOf("Sun, Jan 1", "12:00 AM", "Sun, Jan 1", "1:00 AM", "30", "0:30"),
        )

        list.forEach {
            assertEquals(getTotal(it[0], it[1], it[2], it[3], it[4]), it[5])
        }
    }

    @Test
    fun getDateForInsertTest() {
        assertEquals(getDateForInsert("2023", "Sun, Jan 1", "12:20 PM"), "2023.01.01")
    }

    private fun getDateForInsert(startYear: String, startDate: String, startTime: String): String {
        val date = LocalDateTime.parse("$startYear $startDate $startTime",
            DateTimeFormatter.ofPattern("u EEE, LLL d h:mm a"))
        return DateTimeFormatter.ofPattern("u.MM.dd").format(date)
    }

    @Test
    fun getBreakForInsertTest() {
        assertEquals(getBreakForInsert("---"), "0:00")
        assertEquals(getBreakForInsert("0"), "0:00")
        assertEquals(getBreakForInsert("5"), "0:05")
        assertEquals(getBreakForInsert("25"), "0:25")
        assertEquals(getBreakForInsert("90"), "1:30")
    }

    private fun getBreakForInsert(minutes: String): String {
        if (minutes == "---") return "0:00"

        val min = minutes.toInt() % 60
        val hours = minutes.toInt() / 60

        return String.format("%d:%02d", hours, min)
    }

    @Test
    fun getBreakInMinutesTest() {
        assertEquals(getBreakInMinutes("1:30"), "90")
        assertEquals(getBreakInMinutes("0:05"), "5")
        assertEquals(getBreakInMinutes("0:30"), "30")
    }

    private fun getBreakInMinutes(breakTotal: String): String {
        //1:30   90
        //0:05   5
        //0:30   30
        val tokens = breakTotal.split(':')
        val hours = tokens[0].toInt()
        val minutes = tokens[1].toInt()
        return (hours * 60 + minutes).toString()
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

        val seconds = total.get(ChronoUnit.SECONDS)
        val minutes = seconds / (60) % 60
        val hours = seconds / (60 * 60)

        return String.format("%d:%02d", hours, minutes)
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

    private fun getBreakTotal(timestamp: String): String {
        //START_BREAK 2023.01.17.19:50 AMEND_BREAK2023.01.17.19:50 AM

        val timeStart = timestamp.substring(12, 31)
        val timeEnd = timestamp.substring(40)

        val total = getTimeDiff(timeStart, timeEnd)
        // 10 : 00
        // 0  : 01
        val halves = total.split(':')
        val minutes = if (halves[1][0] == '0') halves[1][1] else halves[1]

        return if (halves[0] == "0") {
            "${minutes}m"
        } else {
            "${halves[0]}h ${minutes}m"
        }
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


}