package com.example.data.datasource.network

import com.example.data.datasource.network.dto.NewsResponse
import com.example.data.datasource.network.dto.SummarizeNewsResponse
import com.example.data.model.News
import com.example.data.model.NewsCategory

interface NewsNetworkDataSource {
    suspend fun fetchNewsByCategory(category: NewsCategory): NewsResponse
    suspend fun summarizeNews(news: News): SummarizeNewsResponse
}