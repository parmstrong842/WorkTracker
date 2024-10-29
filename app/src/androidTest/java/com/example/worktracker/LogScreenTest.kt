package com.example.worktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.worktracker.data.Shift
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.mock.ShiftsRepositoryMock
import com.example.worktracker.ui.LogScreen
import com.example.worktracker.ui.LogViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class LogScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        val shiftsRepository = ShiftsRepositoryMock()
        runBlocking {
            shiftsRepository.insertItem(Shift(date = "2023.09.11", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2023.09.09", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2023.09.17", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2023.08.17", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2023.10.17", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2022.10.17", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
            shiftsRepository.insertItem(Shift(date = "2024.10.17", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
        }

        val clockInInstant = Instant.parse("2023-09-10T10:15:30.00Z")
        val fixedClock = Clock.fixed(clockInInstant, ZoneId.of("UTC"))
        val viewModel = LogViewModel(
            shiftsRepository,
            SharedPreferencesMock(),
            fixedClock
        )

        rule.setContent {
            WorkTrackerTheme {
                LogScreen(
                    navigateBack = {},
                    navigateToShift = {},
                    navigateToItemUpdate = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun logScreenTest() {
        rule.onNodeWithText("2023-09-10 -")
        rule.onNodeWithText("2023-09-16")
        checkShiftIsDisplayed("1", "Mon, Sep 11")
        rule.onNodeWithContentDescription("previous").performClick()
        rule.onNodeWithText("2023-09-03 -")
        rule.onNodeWithText("2023-09-09")
        checkShiftIsDisplayed("2", "Sat, Sep 9")
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithText("2023-09-17 -")
        rule.onNodeWithText("2023-09-23")
        checkShiftIsDisplayed("3", "Sun, Sep 17")

        rule.onNodeWithText("Month").performClick()
        rule.onNodeWithText("2023-09-01 -")
        rule.onNodeWithText("2023-09-30")
        checkShiftIsDisplayed("1", "Mon, Sep 11")
        checkShiftIsDisplayed("2", "Sat, Sep 9")
        checkShiftIsDisplayed("3", "Sun, Sep 17")
        rule.onNodeWithText("27:00").assertIsDisplayed()
        rule.onNodeWithContentDescription("previous").performClick()
        rule.onNodeWithText("2023-08-01 -")
        rule.onNodeWithText("2023-08-31")
        checkShiftIsDisplayed("4", "Thu, Aug 17")
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithText("2023-10-01 -")
        rule.onNodeWithText("2023-10-31")
        checkShiftIsDisplayed("5", "Tue, Oct 17")

        rule.onNodeWithText("Year").performClick()
        rule.onNodeWithText("2023-01-01 -")
        rule.onNodeWithText("2023-12-31")
        checkShiftIsDisplayed("1", "Mon, Sep 11")
        checkShiftIsDisplayed("2", "Sat, Sep 9")
        checkShiftIsDisplayed("3", "Sun, Sep 17")
        checkShiftIsDisplayed("4", "Thu, Aug 17")
        checkShiftIsDisplayed("5", "Tue, Oct 17")
        rule.onNodeWithContentDescription("previous").performClick()
        rule.onNodeWithText("2022-01-01 -")
        rule.onNodeWithText("2022-12-31")
        checkShiftIsDisplayed("6", "Mon, Oct 17")
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithContentDescription("next").performClick()
        rule.onNodeWithText("2024-01-01 -")
        rule.onNodeWithText("2024-12-31")
        checkShiftIsDisplayed("7", "Thu, Oct 17")

        rule.onNodeWithText("All").performClick()
        rule.onNodeWithText("All Shifts")
        checkShiftIsDisplayed("1", "Mon, Sep 11")
        checkShiftIsDisplayed("2", "Sat, Sep 9")
        checkShiftIsDisplayed("3", "Sun, Sep 17")
        checkShiftIsDisplayed("4", "Thu, Aug 17")
        checkShiftIsDisplayed("5", "Tue, Oct 17")
        checkShiftIsDisplayed("6", "Mon, Oct 17")
        checkShiftIsDisplayed("7", "Thu, Oct 17")
    }

    private fun checkShiftIsDisplayed(id: String, date: String) {
        rule.onNode(hasParent(hasTestTag(id)) and hasText(date), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasParent(hasTestTag(id)) and hasText("8:00 AM - 5:30 PM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasAnySibling(hasTestTag(id)) and hasTestTag("break") and hasText("0:30"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasAnySibling(hasTestTag(id)) and hasTestTag("total") and hasText("9:00"), useUnmergedTree = true).assertIsDisplayed()
    }
}