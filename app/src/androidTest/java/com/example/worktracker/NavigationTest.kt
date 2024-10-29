package com.example.worktracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.worktracker.ui.WorkNavGraph
import com.example.worktracker.ui.theme.WorkTrackerTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val rule = createComposeRule()

    @Before
    fun setUp() {
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("pm grant ${InstrumentationRegistry.getInstrumentation().targetContext.packageName} android.permission.POST_NOTIFICATIONS")

        rule.setContent {
            WorkTrackerTheme {
                WorkNavGraph()
            }
        }
    }

    @Test
    fun testNavigation() {
        rule.onNodeWithText("Clock In").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Clock Out").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Work Tracker").assertIsDisplayed()
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Settings").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Work Tracker").assertIsDisplayed()
        rule.onNodeWithText("View Shifts").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Shifts").assertIsDisplayed()
        rule.onNodeWithContentDescription("Create new shift").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("New Shift").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Shifts").assertIsDisplayed()
        rule.onNodeWithText(" - ", substring = true).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Edit Shift").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Work Tracker").assertIsDisplayed()
    }
}