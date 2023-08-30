package com.example.worktracker

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.mock.ShiftsRepositoryMock
import com.example.worktracker.ui.MainScreen
import com.example.worktracker.ui.MainViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
import kotlinx.coroutines.delay
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val shiftsRepository = ShiftsRepositoryMock()

    private val viewModel = MainViewModel(
        shiftsRepository,
        SharedPreferencesMock(),
    )

    @Before
    fun grantPermissions() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} android.permission.POST_NOTIFICATIONS")
    }

    @Test
    fun createShiftTest() {
        composeTestRule.setContent {
            WorkTrackerTheme {
                MainScreen(
                    viewShiftsOnClick = {},
                    navigateToSettings = {},
                    viewModel
                )
            }
        }

        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", 5, 0, "5:00", "5h 0m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", 5, 30, "5:30", "5h 30m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", 0, 30, "0:30", "30m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", 0, 0, "0:00", "0m")

    }

    private fun clockInClockOutNoBreak(clockInTime: String, hours: Long, minutes: Long, string1: String, string2: String) {
        val fixedClock = setClock(clockInTime)
        composeTestRule.onNodeWithText("Clock In").performClick()
        adjustClock(fixedClock, hours, minutes)
        Thread.sleep(1000)
        composeTestRule.onNodeWithText(string2).assertExists()
        composeTestRule.onNodeWithText("Clock Out").performClick()
        assert(shiftsRepository.shift?.shiftTotal == string1)
    }

    private fun clockInClockOutBreak(clockInTime: String, hours: Long, minutes: Long, expected: String) {
        val fixedClock = setClock(clockInTime)
        composeTestRule.onNodeWithText("Clock In").performClick()
        adjustClock(fixedClock, hours, minutes)
        composeTestRule.onNodeWithText("Clock Out").performClick()
        assert(shiftsRepository.shift?.shiftTotal == expected)
    }

    private fun setClock(clockInTime: String): Clock? {
        val clockInInstant = Instant.parse(clockInTime)
        val fixedClock = Clock.fixed(clockInInstant, ZoneId.of("UTC"))
        val offset = Duration.ZERO
        val adjustableClock = Clock.offset(fixedClock, offset)
        viewModel.clock = adjustableClock
        return fixedClock
    }

    private fun adjustClock(fixedClock: Clock?, hours: Long, minutes: Long) {
        val duration = Duration.ofHours(hours).plusMinutes(minutes)
        val newAdjustableClock = Clock.offset(fixedClock, duration)
        viewModel.clock = newAdjustableClock
    }
}

