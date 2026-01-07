package com.example.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.News
import com.example.data.model.NewsCategory
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NewsViewModel(
    private val category: NewsCategory,
    newsRepository: NewsRepository,
) : ViewModel() {
    val newsState = combine(
        newsRepository.isOnline,
        newsRepository.getNewsByCategoryFlow(category = category),
    ) { isOnline, allNews ->
        if (isOnline.not() && allNews.isEmpty()) {
            NewsState(
                isOnline = isOnline,
                isLoading = false,
                errorMessage = "No Internet and No Cached News",
            )
        } else {
            val (latest, relevant) = allNews.partition { news -> news.summary != null }
            NewsState(
                isOnline = isOnline,
                isLoading = allNews.isEmpty(),
                latestNews = latest,
                relevantNews = relevant,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsState()
    )
}

data class NewsState(
    val isOnline: Boolean = false,
    val isLoading: Boolean = true,
    val latestNews: List<News> = emptyList(),
    val relevantNews: List<News> = emptyList(),
    val errorMessage: String? = null,
)
