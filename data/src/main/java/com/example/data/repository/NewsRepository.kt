package com.example.data.repository

import com.example.data.model.News
import com.example.data.model.NewsCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NewsRepository {
    val isOnline: StateFlow<Boolean>
    fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>>
    fun getSummaryByNews(news: News): Flow<String?>
    suspend fun downloadNewsMedia(url: String)
    suspend fun summarizeNews(news: News)
}