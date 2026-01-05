package com.example.presentation.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.News
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsReadViewModel(
    private val news: News,
    private val newsRepository: NewsRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(NewsReadState(news = news))
    val uiState: StateFlow<NewsReadState> = _uiState.asStateFlow()

    fun onMediaFileDetected(url: String) {
        _uiState.update {
            it.copy(
                downloadableMedias = it.downloadableMedias + MediaUrl(url)
            )
        }
    }

    fun downloadMedia(mediaUrl: MediaUrl) = viewModelScope.launch {
        newsRepository.downloadNewsMedia(url = mediaUrl.url)
    }

    fun summarizeNews() = viewModelScope.launch {
        _uiState.update {
            it.copy(
                newsSummary = newsRepository.summarizeNews(news = news)
            )
        }
    }
}

data class NewsReadState(
    val news: News,
    val downloadableMedias: Set<MediaUrl> = emptySet(),
    val newsSummary: String? = null,
)

@JvmInline
value class MediaUrl(val url: String)