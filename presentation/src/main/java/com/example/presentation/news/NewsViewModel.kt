package com.example.presentation.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.News
import com.example.data.model.NewsCategory
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NewsViewModel(
    private val category: NewsCategory,
    newsRepository: NewsRepository,
) : ViewModel() {
    val newsState = newsRepository.getNewsByCategoryFlow(category = category)
        .filter { it.isNotEmpty() }
        .map {
            NewsState(isLoading = false, news = it)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NewsState()
        )
}

data class NewsState(
    val isLoading: Boolean = true,
    val news: List<News> = emptyList(),
    val errorMessage: String? = null,
)
