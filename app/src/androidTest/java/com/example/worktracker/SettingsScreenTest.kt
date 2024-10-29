package com.example.worktracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.worktracker.mock.SharedPreferencesMock
import com.example.worktracker.ui.SettingsScreen
import com.example.worktracker.ui.SettingsViewModel
import com.example.worktracker.ui.theme.WorkTrackerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer

class SettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val viewModel = SettingsViewModel(SharedPreferencesMock())
    private lateinit var uiDevice: UiDevice

    @Before
    fun setUp() {
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        rule.setContent {
            WorkTrackerTheme {
                SettingsScreen(
                    navigateBack = {},
                    viewModel
                )
            }
        }
    }

    @Test
    fun testComponentsAreDisplayed() {
        rule.onNodeWithText("Settings").assertIsDisplayed()
        rule.onNodeWithText("Time Zone").assertIsDisplayed()
        rule.onNodeWithText("Start of Week").assertIsDisplayed()
    }

    @Test
    fun testTimeZoneDialog() {// must run on Pixel_3a_API_33_x86_64
        rule.onNodeWithText("Time Zone").performClick()
        rule.onNodeWithText("Alaska / USA").performTouchInput { swipeUp() }
        rule.onNodeWithContentDescription("go to top").assertIsDisplayed().performClick()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("go to top").assertDoesNotExist()

        rule.onNodeWithTag("ABCBar")
            .assertIsDisplayed()
            .performTouchInput {
                down(Offset(centerX, 0f))
                moveBy(Offset(0f, 100f))
            }
        val actual = rule.onNodeWithTag("TimeZoneSelectionDialog")
            .captureToImage()
            .asAndroidBitmap()
        val expected = BitmapFactory.decodeStream(InstrumentationRegistry.getInstrumentation().context.assets.open("ABCBarTest.png"))
        assertTrue(areBitmapsEqual(actual, expected))
        rule.onNodeWithTag("ABCBar").performTouchInput { up() }

        rule.onNodeWithText("Alaska / USA").performClick()
        rule.onRoot().printToLog("TAG")

        rule.onNodeWithText("Time Zone").assertIsDisplayed()
        rule.onNodeWithText("Belgrade / Serbia").assertIsDisplayed()

        rule.onNodeWithText("Time Zone").performClick()
        rule.onNodeWithContentDescription("Time zone select back").performClick()

        rule.onNodeWithText("Time Zone").performClick()
        rule.onNodeWithText("Search").performTextInput("usa")
        rule.onNodeWithText("Alaska / USA").performClick()
        rule.onNodeWithText("Time Zone").assertIsDisplayed()
        rule.onNodeWithText("Alaska / USA").assertIsDisplayed()
    }
}

private fun areBitmapsEqual(bmp1: Bitmap, bmp2: Bitmap): Boolean {
    if (bmp1.width != bmp2.width || bmp1.height != bmp2.height) {
        return false
    }

    for (y in 0 until bmp1.height) {
        for (x in 0 until bmp1.width) {
            if (bmp1.getPixel(x, y) != bmp2.getPixel(x, y)) {
                return false
            }
        }
    }

    return true
}