package com.example.data.datasource.network.ktor

import com.example.data.datasource.network.NewsNetworkDataSource
import com.example.data.datasource.network.dto.NewsResponse
import com.example.data.datasource.network.dto.SummarizeNewsResponse
import com.example.data.model.News
import com.example.data.model.NewsCategory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorNetworkDataSource(private val client: HttpClient): NewsNetworkDataSource {

    override suspend fun fetchNewsByCategory(category: NewsCategory): NewsResponse {
        return client.get("https://7tja7bkjzuti6kyome7mtur5di0zomch.lambda-url.ap-northeast-1.on.aws/") {
            parameter("url", category.toUrlParameter())
            parameter("action", "get-articles")
        }.body()
    }

    override suspend fun summarizeNews(news: News): SummarizeNewsResponse {
        return client.get("https://7tja7bkjzuti6kyome7mtur5di0zomch.lambda-url.ap-northeast-1.on.aws/") {
            parameter("url", news.url)
            parameter("action", "summarize-article-content")
        }.body()
    }

    private fun NewsCategory.toUrlParameter() = "https://dantri.com.vn/$parameter.htm"
//    actionParams: get-articles, get-article-content, summarize-article-content
}