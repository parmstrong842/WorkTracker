package com.example.worktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.example.worktracker.data.Shift
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.mock.ShiftsRepositoryMock
import com.example.worktracker.ui.ShiftEditDestination
import com.example.worktracker.ui.ShiftEditScreen
import com.example.worktracker.ui.ShiftEditViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ShiftEditScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var shiftsRepository: ShiftsRepositoryMock
    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        shiftsRepository = ShiftsRepositoryMock()
        runBlocking {
            shiftsRepository.insertItem(Shift(date = "2023.09.11", shiftSpan = "8:00 AM - 5:30 PM", breakTotal = "0:30", shiftTotal = "9:00"))
        }

        val initialState = mapOf(
            ShiftEditDestination.shiftIdArg to 1,
        )
        val savedStateHandle = SavedStateHandle(initialState)

        val viewModel = ShiftEditViewModel(
            savedStateHandle,
            shiftsRepository,
            SharedPreferencesMock(),
        )

        rule.setContent {
            WorkTrackerTheme {
                ShiftEditScreen(
                    topBarTitle = "Edit Shift",
                    navigateBack = {},
                    viewModel = viewModel
                )
            }
        }
    }

    @Test
    fun shiftEditScreenTest() {
        rule.onNodeWithText("Edit Shift").assertIsDisplayed()
        rule.onNode(hasTestTag("startDate") and hasText("Mon, Sep 11"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("startTime") and hasText("8:00 AM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("endDate") and hasText("Mon, Sep 11"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNode(hasTestTag("endTime") and hasText("5:30 PM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithText("30").assertIsDisplayed()
        rule.onNodeWithText("9:00").assertIsDisplayed()

        rule.onNodeWithTag("startDate", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("14")).click()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("startDate") and hasText("Thu, Sep 14"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("startTime", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("startTime") and hasText("8:00 AM"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("endDate", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("15")).click()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("endDate") and hasText("Fri, Sep 15"), useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("endTime", useUnmergedTree = true).performClick()
        uiDevice.findObject(By.text("OK")).click()
        rule.onNode(hasTestTag("endTime") and hasText("5:30 PM"), useUnmergedTree = true).assertIsDisplayed()

        rule.onNodeWithContentDescription("Select break time").performClick()
        rule.onNodeWithText("Break (In Minutes)").assertIsDisplayed()
        rule.onNodeWithText("Set break time").performTextInput("60")
        rule.onNodeWithText("Confirm").performClick()

        rule.onNodeWithText("60").assertIsDisplayed()
        rule.onNodeWithText("32:30").assertIsDisplayed()

        rule.onNodeWithText("Save").performClick()

        runBlocking {
            val updatedShift = shiftsRepository.getItemStream(1)
            assert(updatedShift?.date == "2023.09.14")
            assert(updatedShift?.shiftSpan == "8:00 AM - 5:30 PM")
            assert(updatedShift?.breakTotal == "1:00")
            assert(updatedShift?.shiftTotal == "32:30")
        }
    }

    @Test
    fun deleteTest() {
        rule.onNodeWithText("Delete").performClick()
        rule.onNodeWithText("Delete Shift?").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithText("Delete").performClick()
        rule.onNodeWithText("Confirm").performClick()
        runBlocking {
            assert(shiftsRepository.getAllItems().isEmpty())
        }
    }
}