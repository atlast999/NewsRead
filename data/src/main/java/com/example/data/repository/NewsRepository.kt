package com.example.data.repository

import com.example.data.model.News
import com.example.data.model.NewsCategory
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>>

    suspend fun downloadNewsMedia(url: String)
}