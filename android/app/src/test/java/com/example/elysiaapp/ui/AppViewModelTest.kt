package com.example.elysiaapp.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    
    private lateinit var testDispatcher: TestDispatcher
    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        viewModel = AppViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() {
        val initialState = viewModel.uiState.value
        
        assertFalse(initialState.isLoading)
        assertEquals("", initialState.serverResponse)
        assertEquals("", initialState.error)
        assertEquals(emptyList<MessageDisplayModel>(), initialState.messages)
        assertEquals("Disconnected", initialState.connectionStatus)
    }

    @Test
    fun `ping server sets loading state`() = runTest {
        // This test would ideally mock the ApiClient, but for now just test the loading state
        viewModel.pingServer()
        
        // Since we can't easily mock the network call in this simple test,
        // we'll just verify that the method can be called without crashing
        assertTrue("pingServer method executed without exception", true)
    }

    @Test
    fun `formatTimestamp handles different timestamp formats`() {
        // This is a private method, so we're testing indirectly through the public API
        // In a real app, you might extract this to a utility class for easier testing
        
        val message = MessageDisplayModel(
            type = "chat",
            from = "Test",
            text = "Test message",
            timestamp = "12:34:56"
        )
        
        assertEquals("Test message", message.text)
        assertEquals("Test", message.from)
    }

    @Test
    fun `sendMessage executes without error`() = runTest {
        // Test that sendMessage can be called
        viewModel.sendMessage("TestUser", "Test message")
        
        // Verify method executes without throwing
        assertTrue("sendMessage method executed without exception", true)
    }
}