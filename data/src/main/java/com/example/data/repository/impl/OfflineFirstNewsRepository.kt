package com.example.data.repository.impl

import android.util.Log
import com.example.data.datasource.database.dao.NewsDao
import com.example.data.datasource.database.entity.NewsEntity
import com.example.data.datasource.database.entity.NewsSummaryEntity
import com.example.data.datasource.database.entity.asNews
import com.example.data.datasource.downloader.FileDownloader
import com.example.data.datasource.network.NewsNetworkDataSource
import com.example.data.datasource.network.dto.NewsDto
import com.example.data.model.News
import com.example.data.model.NewsCategory
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class OfflineFirstNewsRepository(
    private val newsDao: NewsDao,
    private val newsNetworkDataSource: NewsNetworkDataSource,
    private val applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
    private val fileDownloader: FileDownloader,
) : NewsRepository {

    private var syncJob: Job? = null
    private val syncedCategories = mutableSetOf<NewsCategory>()

    override fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>> {
        return newsDao.getNewsByCategory(categoryId = category.identity).map { entities ->
            entities.map(NewsEntity::asNews)
        }.onStart {
            syncJob = syncNewsByCategory(category = category)
        }.onCompletion {
            syncJob?.cancel()
        }
    }

    override suspend fun downloadNewsMedia(url: String) {
        fileDownloader.downloadFile(url = url)
    }

    override suspend fun summarizeNews(news: News): String {
        val cachedSummary = newsDao.getNewsSummary(newsUrl = news.url).firstOrNull()?.summary
        if (cachedSummary != null) return cachedSummary
        val networkSummary = newsNetworkDataSource.summarizeNews(news = news).summary
        newsDao.insertNewsSummary(
            newsSummaryEntity = NewsSummaryEntity(
                url = news.url,
                summary = networkSummary,
            )
        )
        return newsDao.getNewsSummary(newsUrl = news.url).firstOrNull()?.summary
            ?: throw IllegalStateException("No summary found")
    }

    private fun syncNewsByCategory(category: NewsCategory) = applicationScope.launch(ioDispatcher) {
        if (syncedCategories.contains(category)) return@launch
        Log.d("HOANTAG", "Start syncing news for category: ${category.name}")
        val dtos = newsNetworkDataSource.fetchNewsByCategory(category = category).news
        val entities = dtos.map { it.asEntity(categoryId = category.identity) }
        newsDao.clearNewsEntitiesByCategory(categoryId = category.identity)
        newsDao.insertNewsEntities(newsEntities = entities)
        Log.d("HOANTAG", "Finish syncing news for category: ${category.name}")
        syncedCategories.add(category)
    }

    private fun NewsDto.asEntity(categoryId: Int) = NewsEntity(
        title = title,
        summary = summary,
        thumbnail = thumbnail,
        categoryId = categoryId,
        url = url,
    )

}