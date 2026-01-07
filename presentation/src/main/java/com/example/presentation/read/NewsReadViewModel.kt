package com.example.presentation.read

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.News
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsReadViewModel(
    private val news: News,
    private val newsRepository: NewsRepository,
): ViewModel() {

    private val _downloadableMedias = MutableStateFlow(emptySet<MediaUrl>())
    private val _newsSummary = newsRepository.getSummaryByNews(news = news)
    private val _summaryErrorMessage = MutableStateFlow<String?>(null)
    val uiState = combine(
        _downloadableMedias,
        _newsSummary,
        _summaryErrorMessage,
    ) { downloadableMedias, newsSummary, summaryErrorMessage ->
        NewsReadState(
            news = news,
            downloadableMedias = downloadableMedias,
            newsSummaryState = NewsSummaryState(
                summary = newsSummary,
                error = summaryErrorMessage,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsReadState(news = news)
    )

    fun onMediaFileDetected(url: String) {
        _downloadableMedias.update {
            it + MediaUrl(url)
        }
    }

    fun downloadMedia(mediaUrl: MediaUrl) = viewModelScope.launch {
        newsRepository.downloadNewsMedia(url = mediaUrl.url)
    }

    fun summarizeNews() = viewModelScope.launch(
        CoroutineExceptionHandler { _, throwable ->
            _summaryErrorMessage.update {
                throwable.message ?:"Unknown error"
            }
        }
    ) {
        newsRepository.summarizeNews(news = news)
    }
}

data class NewsReadState(
    val news: News,
    val downloadableMedias: Set<MediaUrl> = emptySet(),
    val newsSummaryState: NewsSummaryState = NewsSummaryState(),
)

@JvmInline
value class MediaUrl(val url: String)

data class NewsSummaryState(
    val summary: String? = null,
    val error: String? = null,
)