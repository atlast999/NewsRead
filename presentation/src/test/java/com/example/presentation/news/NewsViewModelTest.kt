package com.example.presentation.news

import com.example.data.model.News
import com.example.data.model.NewsCategory
import com.example.data.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsRepository: NewsRepository
    private val testDispatcher = StandardTestDispatcher()
    private val category = NewsCategory.INTERNATIONAL

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        newsRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Initial State Validation`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val newsFlow = MutableStateFlow<List<News>>(emptyList())
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)

        // Act
        val initialState = viewModel.newsState.value

        // Assert
        val expectedState = NewsState() // Default values
        assertEquals("Initial state should match default NewsState", expectedState, initialState)
    }

    @Test
    fun `Offline and Empty Cache Error State`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(false)
        val newsFlow = MutableStateFlow<List<News>>(emptyList())
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertFalse("isOnline should be false", finalState.isOnline)
        assertFalse("isLoading should be false", finalState.isLoading)
        assertTrue("Error message should not null", finalState.errorMessage != null)
    }

    @Test
    fun `Offline with Cached News`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(false)
        val newsList = listOf(
            News(url = "1", title = "T1", summary = "S1", thumbnail = ""), // Has summary -> Latest
            News(url = "2", title = "T2", summary = null, thumbnail = "")  // No summary -> Relevant
        )
        val newsFlow = MutableStateFlow(newsList)
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertFalse("isOnline should be false", finalState.isOnline)
        assertFalse("isLoading should be false", finalState.isLoading)
        assertNull("errorMessage should be null", finalState.errorMessage)
        assertEquals("Latest news size should be 1", 1, finalState.latestNews.size)
        assertEquals("Latest news URL mismatch", "1", finalState.latestNews[0].url)
        assertEquals("Relevant news size should be 1", 1, finalState.relevantNews.size)
        assertEquals("Relevant news URL mismatch", "2", finalState.relevantNews[0].url)
    }

    @Test
    fun `Online and Loading Empty List`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val newsFlow = MutableStateFlow<List<News>>(emptyList())
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertTrue("isOnline should be true", finalState.isOnline)
        assertTrue("isLoading should be true", finalState.isLoading)
        assertNull("errorMessage should be null", finalState.errorMessage)
        assertTrue("Latest news should be empty", finalState.latestNews.isEmpty())
        assertTrue("Relevant news should be empty", finalState.relevantNews.isEmpty())
    }

    @Test
    fun `Online with News Partitioning Logic`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val newsList = listOf(
            News(url = "1", title = "T1", summary = "S1", thumbnail = ""),
            News(url = "2", title = "T2", summary = null, thumbnail = ""),
            News(url = "3", title = "T3", summary = "S3", thumbnail = "")
        )
        val newsFlow = MutableStateFlow(newsList)
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertTrue("isOnline should be true", finalState.isOnline)
        assertFalse("isLoading should be false", finalState.isLoading)
        
        // Items with summary -> latestNews
        assertEquals("Latest news size should be 2", 2, finalState.latestNews.size)
        assertTrue("Latest news should contain url 1", finalState.latestNews.any { it.url == "1" })
        assertTrue("Latest news should contain url 3", finalState.latestNews.any { it.url == "3" })

        // Items without summary -> relevantNews
        assertEquals("Relevant news size should be 1", 1, finalState.relevantNews.size)
        assertTrue("Relevant news should contain url 2", finalState.relevantNews.any { it.url == "2" })
    }

    @Test
    fun `All News are Latest Partitioning Edge Case`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val newsList = listOf(
            News(url = "1", title = "T1", summary = "S1", thumbnail = ""),
            News(url = "2", title = "T2", summary = "S2", thumbnail = "")
        )
        val newsFlow = MutableStateFlow(newsList)
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertEquals("Latest news size should be 2", 2, finalState.latestNews.size)
        assertTrue("Relevant news should be empty", finalState.relevantNews.isEmpty())
    }

    @Test
    fun `All News are Relevant Partitioning Edge Case`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val newsList = listOf(
            News(url = "1", title = "T1", summary = null, thumbnail = ""),
            News(url = "2", title = "T2", summary = null, thumbnail = "")
        )
        val newsFlow = MutableStateFlow(newsList)
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Act
        val finalState = viewModel.newsState.value

        // Assert
        assertEquals("Relevant news size should be 2", 2, finalState.relevantNews.size)
        assertTrue("Latest news should be empty", finalState.latestNews.isEmpty())
    }

    @Test
    fun `Network Status Transition Offline to Online`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(false)
        val newsList = listOf(News(url = "1", title = "T1", summary = "S1", thumbnail = ""))
        val newsFlow = MutableStateFlow(newsList)
        
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()

        // Verify initial offline state
        val offlineState = viewModel.newsState.value
        assertFalse("Initial state should be offline", offlineState.isOnline)
        assertEquals("Initial state should have 1 latest news", 1, offlineState.latestNews.size)

        // Act: Go Online
        isOnlineFlow.value = true

        // Assert
        val finalState = viewModel.newsState.first { it.isOnline }
        assertTrue("Final state should be online", finalState.isOnline)
        // Data should persist/be the same since we didn't change the news list
        assertEquals("Final state should still have 1 latest news", 1, finalState.latestNews.size)
        assertEquals("News URL should match", "1", finalState.latestNews[0].url)
    }

    @Test
    fun `Data Update Propagation`() = runTest {
        // Arrange
        val isOnlineFlow = MutableStateFlow(true)
        val initialList = listOf(News(url = "1", title = "T1", summary = "S1", thumbnail = ""))
        val newsFlow = MutableStateFlow(initialList)
        
        coEvery { newsRepository.isOnline } returns isOnlineFlow
        coEvery { newsRepository.getNewsByCategoryFlow(category) } returns newsFlow

        viewModel = NewsViewModel(category, newsRepository)
        // Ensure flow is active for stateIn(WhileSubscribed) to process updates
        backgroundScope.launch { viewModel.newsState.collect() }

        advanceUntilIdle()
        // Verify initial state
        val initialState = viewModel.newsState.value
        assertEquals("Initial state should have 1 latest news", 1, initialState.latestNews.size)

        // Act: Update data
        val updatedList = listOf(
            News(url = "1", title = "T1", summary = "S1", thumbnail = ""),
            News(url = "2", title = "T2", summary = "S2", thumbnail = "")
        )
        newsFlow.value = updatedList

        // Assert
        val finalState = viewModel.newsState.first { it.latestNews.size == 2 }
        assertEquals("Updated state should have 2 latest news", 2, finalState.latestNews.size)
        assertTrue("Updated state should contain new news item", finalState.latestNews.any { it.url == "2" })
    }
}