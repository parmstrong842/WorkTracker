package com.example.worktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.example.worktracker.data.ShiftsRepository
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.mock.ShiftsRepositoryMock
import com.example.worktracker.ui.ShiftScreen
import com.example.worktracker.ui.ShiftViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class ShiftScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var shiftsRepository: ShiftsRepositoryMock
    private lateinit var uiDevice: UiDevice
    @Before
    fun setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        shiftsRepository = ShiftsRepositoryMock()

        val clockInInstant = Instant.parse("2023-09-10T10:15:30.00Z")
        val fixedClock = Clock.fixed(clockInInstant, ZoneId.of("UTC"))

        val viewModel = ShiftViewModel(
            shiftsRepository,
            SharedPreferencesMock(),
            fixedClock
        )

        rule.setContent {
            WorkTrackerTheme {
                ShiftScreen(
                    topBarTitle = "New Shift",
                    navigateBack = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun shiftScreenTest() {
        rule.onNodeWithText("New Shift").assertIsDisplayed()
        rule.onNode(hasTestTag("startDate") and hasText("Sun, Sep 10"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("startTime") and hasText("10:15 AM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("endDate") and hasText("Sun, Sep 10"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("endTime") and hasText("10:15 AM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("---").assertIsDisplayed()
        rule.onNodeWithText("0:00").assertIsDisplayed()

        rule.onNodeWithTag("startDate", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("14")).click()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("startDate") and hasText("Thu, Sep 14"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("startTime", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("startTime") and hasText("10:15 AM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("endDate", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("15")).click()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("endDate") and hasText("Fri, Sep 15"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("endTime", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("endTime") and hasText("10:15 AM"), useUnmergedTree = true).assertIsDisplayed()

        rule.onNodeWithContentDescription("Select break time").performClick()
        rule.onNodeWithText("Break (In Minutes)").assertIsDisplayed()
        rule.onNodeWithText("Set break time").performTextInput("30")
        rule.onNodeWithText("Confirm").performClick()

        rule.onNodeWithText("30").assertIsDisplayed()
        rule.onNodeWithText("23:30").assertIsDisplayed()

        rule.onNodeWithText("Save").performClick()
        assert(shiftsRepository.currentShift?.date == "2023.09.14")
        assert(shiftsRepository.currentShift?.shiftSpan == "10:15 AM - 10:15 AM")
        assert(shiftsRepository.currentShift?.breakTotal == "0:30")
        assert(shiftsRepository.currentShift?.shiftTotal == "23:30")
    }
}