package com.example.presentation.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.News
import com.example.data.repository.NewsRepository
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

    }
}

data class NewsReadState(
    val news: News,
    val downloadableMedias: Set<MediaUrl> = emptySet(),
)

@JvmInline
value class MediaUrl(val url: String)