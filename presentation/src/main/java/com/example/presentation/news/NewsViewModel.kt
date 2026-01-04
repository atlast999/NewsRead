package com.example.presentation.news

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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
    savedStateHandle: SavedStateHandle,
    newsRepository: NewsRepository,
) : ViewModel() {

    init {
        savedStateHandle.keys().joinToString().let {
            Log.d("HOANTAG", "savedStateHandle keys: $it")
        }
    }
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
