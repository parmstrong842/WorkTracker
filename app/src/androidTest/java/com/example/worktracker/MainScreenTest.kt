package com.example.worktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.mock.ShiftsRepositoryMock
import com.example.worktracker.ui.MainScreen
import com.example.worktracker.ui.MainViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
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

    private val shiftsRepositoryMock = ShiftsRepositoryMock()
    private val sharedPreferencesMock = SharedPreferencesMock()

    private val viewModel = MainViewModel(
        shiftsRepositoryMock,
        sharedPreferencesMock,
    )

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} android.permission.POST_NOTIFICATIONS")

        Utils.initialize(sharedPreferencesMock)

        composeTestRule.setContent {
            WorkTrackerTheme {
                MainScreen(
                    viewShiftsOnClick = {},
                    navigateToSettings = {},
                    viewModel
                )
            }
        }
    }

    @Test
    fun testClockInButtonInitialDisplay() {
        composeTestRule.onNodeWithText("Clock In").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Shifts").assertIsDisplayed()

        composeTestRule.onNodeWithText("Clock In").performClick()
        composeTestRule.onNodeWithText("Clock Out").assertIsDisplayed()

        composeTestRule.onNodeWithText("Clock Out").performClick()
        composeTestRule.onNodeWithText("Shift Saved").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export csv").assertIsDisplayed()
    }

    @Test
    fun testCreateShiftNoBreak() {
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", "10:15 AM",5, 0, "5:00", "5h 0m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", "10:15 AM",5, 30, "5:30", "5h 30m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", "10:15 AM",0, 30, "0:30", "30m")
        clockInClockOutNoBreak("2023-08-22T10:15:30.00Z", "10:15 AM",0, 0, "0:00", "0m")
    }
    @Test
    fun testCreateShiftBreak() {
        clockInClockOutBreak("2023-08-22T10:15:30.00Z", "10:15 AM", "11:15 AM", 1, 0, 15, "0:15", "1:00", "15m")
        clockInClockOutBreak("2023-08-22T10:15:30.00Z", "10:15 AM", "11:15 AM", 1, 0, 0, "0:00", "1:00", "0m")
    }

    private fun clockInClockOutNoBreak(clockInTime: String, shiftStart: String, hours: Long, minutes: Long, shiftTotal: String, counter: String) {
        val fixedClock = setClock(clockInTime)
        composeTestRule.onNodeWithText("Clock In").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(shiftStart).assertExists()
        adjustClock(fixedClock, hours, minutes)
        composeTestRule.onNodeWithTag("counter").assertTextEquals(counter)
        composeTestRule.onNodeWithText("Clock Out").performClick()
        assert(shiftsRepositoryMock.currentShift?.shiftTotal == shiftTotal)
    }

    private fun clockInClockOutBreak(clockInTime: String, shiftStart: String, breakStart: String, hours: Long, minutes: Long, breakMinutes: Long, expectedBreak: String, expected: String, breakCounter: String) {
        var fixedClock = setClock(clockInTime)
        composeTestRule.onNodeWithText("Clock In").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(shiftStart).assertExists()
        fixedClock = adjustClock(fixedClock, hours, minutes)
        composeTestRule.onNodeWithContentDescription("Break Start:").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(breakStart).assertExists()
        adjustClock(fixedClock, 0, breakMinutes)
        composeTestRule.onNodeWithTag("breakCounter").assertTextEquals(breakCounter)
        composeTestRule.onNodeWithContentDescription("Break Start:").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Clock Out").performClick()
        composeTestRule.waitForIdle()
        assert(shiftsRepositoryMock.currentShift?.breakTotal == expectedBreak)
        assert(shiftsRepositoryMock.currentShift?.shiftTotal == expected)
    }

    private fun setClock(clockInTime: String): Clock? {
        val clockInInstant = Instant.parse(clockInTime)
        val fixedClock = Clock.fixed(clockInInstant, ZoneId.of("UTC"))
        Utils.clock = fixedClock
        return fixedClock
    }

    private fun adjustClock(fixedClock: Clock?, hours: Long, minutes: Long): Clock {
        val duration = Duration.ofHours(hours).plusMinutes(minutes)
        val newAdjustableClock = Clock.offset(fixedClock, duration)
        Utils.clock = newAdjustableClock
        viewModel.updateView()
        return newAdjustableClock
    }
}

