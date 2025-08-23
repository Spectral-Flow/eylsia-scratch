package com.example.elysiaapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.elysiaapp.ui.App
import com.example.elysiaapp.ui.theme.ElysiaAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AppInstrumentedTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appDisplaysCorrectly() {
        // Start the app
        composeTestRule.setContent {
            ElysiaAppTheme {
                App()
            }
        }

        // Check that key UI elements are displayed
        composeTestRule.onNodeWithText("Server Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ping Server").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnected").assertIsDisplayed()
    }

    @Test
    fun chatInputIsDisplayed() {
        composeTestRule.setContent {
            ElysiaAppTheme {
                App()
            }
        }

        // Check that the message input hint is displayed
        composeTestRule.onNodeWithText("Type a message...").assertIsDisplayed()
    }
}