package com.example.presentation.read

import com.example.data.model.News
import com.example.data.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class NewsReadViewModelTest {

    private lateinit var viewModel: NewsReadViewModel
    private lateinit var newsRepository: NewsRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testNews = News(url = "test_url", title = "Test Title", summary = null, thumbnail = "")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        newsRepository = mockk(relaxed = true)
        
        // Default behavior for summary flow
        coEvery { newsRepository.getSummaryByNews(testNews) } returns flowOf(null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial UI state matches news argument`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        
        val initialState = viewModel.uiState.value
        
        assertEquals("Initial state news should match", testNews, initialState.news)
        assertTrue("Initial downloadableMedias should be empty", initialState.downloadableMedias.isEmpty())
        assertNull("Initial summary should be null", initialState.newsSummaryState.summary)
        assertNull("Initial error should be null", initialState.newsSummaryState.error)
    }

    @Test
    fun `UI state combines flow updates correctly`() = runTest {
        // Arrange
        val summaryFlow = MutableStateFlow<String?>(null)
        coEvery { newsRepository.getSummaryByNews(testNews) } returns summaryFlow
        viewModel = NewsReadViewModel(testNews, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.uiState.collect() }

        // Wait for initial emission
        viewModel.uiState.value 

        // Act & Assert 1: Update Summary
        summaryFlow.value = "New Summary"
        advanceUntilIdle()
        val stateWithSummary = viewModel.uiState.value
        assertEquals("Summary should be updated", "New Summary", stateWithSummary.newsSummaryState.summary)

        // Act & Assert 2: Update Medias
        viewModel.onMediaFileDetected("http://media.url")
        val stateWithMedia = viewModel.uiState.first { it.downloadableMedias.isNotEmpty() }
        assertEquals("Medias should be updated", 1, stateWithMedia.downloadableMedias.size)
    }

    @Test
    fun `UI state reflects repository summary updates`() = runTest {
        // Arrange
        val summaryFlow = MutableStateFlow<String?>(null)
        coEvery { newsRepository.getSummaryByNews(testNews) } returns summaryFlow
        viewModel = NewsReadViewModel(testNews, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.uiState.collect() }

        // Act
        summaryFlow.value = "Updated Summary"
        advanceUntilIdle()
        val updatedState = viewModel.uiState.value

        // Assert
        assertEquals("Summary should match repository update", "Updated Summary", updatedState.newsSummaryState.summary)
    }

    @Test
    fun `onMediaFileDetected adds valid URL`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        val url = "http://example.com/image.jpg"

        viewModel.onMediaFileDetected(url)
        advanceUntilIdle()

        val currentState = viewModel.uiState.first { it.downloadableMedias.isNotEmpty() }
        assertTrue("Should contain added URL", currentState.downloadableMedias.contains(MediaUrl(url)))
    }

    @Test
    fun `onMediaFileDetected handles duplicate URLs`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.uiState.collect() }
        
        val url = "http://example.com/image.jpg"

        viewModel.onMediaFileDetected(url)
        viewModel.onMediaFileDetected(url)
        
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Set should ignore duplicates", 1, currentState.downloadableMedias.size)
    }

    @Test
    fun `onMediaFileDetected accumulates multiple distinct URLs`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.uiState.collect() }
        
        val url1 = "http://example.com/1.jpg"
        val url2 = "http://example.com/2.jpg"

        viewModel.onMediaFileDetected(url1)
        viewModel.onMediaFileDetected(url2)
        
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Should contain 2 distinct URLs", 2, currentState.downloadableMedias.size)
        assertTrue(currentState.downloadableMedias.contains(MediaUrl(url1)))
        assertTrue(currentState.downloadableMedias.contains(MediaUrl(url2)))
    }

    @Test
    fun `onMediaFileDetected handles empty string`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.uiState.collect() }
        
        viewModel.onMediaFileDetected("")
        
        advanceUntilIdle()
        
        val currentState = viewModel.uiState.value
        assertTrue("Should not contain empty string entry", currentState.downloadableMedias.isEmpty())
    }

    @Test
    fun `summarizeNews calls repository method`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        
        viewModel.summarizeNews()
        advanceUntilIdle()

        coVerify(exactly = 1) { newsRepository.summarizeNews(testNews) }
    }

    @Test
    fun `summarizeNews handles successful completion`() = runTest {
        viewModel = NewsReadViewModel(testNews, newsRepository)
        coEvery { newsRepository.summarizeNews(testNews) } returns Unit
        
        // Ensure flow is active to propagate any state changes (even if none expected for success in error field)
        backgroundScope.launch { viewModel.uiState.collect() }
        
        viewModel.summarizeNews()
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertNull("Error should remain null on success", currentState.newsSummaryState.error)
    }

    @Test
    fun `summarizeNews handles exception with message`() = runTest {
        // Arrange
        val errorMessage = "Network Failure"
        coEvery { newsRepository.summarizeNews(testNews) } throws IOException(errorMessage)
        viewModel = NewsReadViewModel(testNews, newsRepository)

        // Act
        viewModel.summarizeNews()
        
        // Wait for error state to update
        val errorState = viewModel.uiState.first { it.newsSummaryState.error != null }

        // Assert
        assertEquals("Error message should be captured", errorMessage, errorState.newsSummaryState.error)
    }

    @Test
    fun `summarizeNews handles exception with null message`() = runTest {
        // Arrange
        coEvery { newsRepository.summarizeNews(testNews) } throws RuntimeException() // message is null
        viewModel = NewsReadViewModel(testNews, newsRepository)

        // Act
        viewModel.summarizeNews()
        
        // Wait for error state to update
        val errorState = viewModel.uiState.first { it.newsSummaryState.error != null }

        // Assert
        assertEquals("Should use fallback error message", "Unknown error", errorState.newsSummaryState.error)
    }

    @Test
    fun `UI state retains values during configuration change`() = runTest {
        // Arrange: Setup VM with some state
        val summaryFlow = MutableStateFlow<String?>(null)
        coEvery { newsRepository.getSummaryByNews(testNews) } returns summaryFlow
        viewModel = NewsReadViewModel(testNews, newsRepository)
        
        summaryFlow.value = "Cached Summary"
        viewModel.onMediaFileDetected("http://url.com")
        
        // Consume to update state
        val currentState = viewModel.uiState.first { 
            it.newsSummaryState.summary == "Cached Summary" && it.downloadableMedias.isNotEmpty() 
        }

        // Act: Simulate "re-subscription" (collecting again)
        // Since stateIn caches the value, the new collector should get the current state immediately
        val retainedState = viewModel.uiState.value
        
        // Assert
        assertEquals("Summary should be retained", "Cached Summary", retainedState.newsSummaryState.summary)
        assertEquals("Media should be retained", 1, retainedState.downloadableMedias.size)
    }
}