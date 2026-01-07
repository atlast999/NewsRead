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
import com.example.data.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class OfflineFirstNewsRepository(
    private val networkMonitor: NetworkMonitor,
    private val newsDao: NewsDao,
    private val newsNetworkDataSource: NewsNetworkDataSource,
    private val fileDownloader: FileDownloader,
    private val applicationScope: CoroutineScope,
) : NewsRepository {

    override val isOnline: StateFlow<Boolean>
        get() = networkMonitor.isOnline

    private var syncJob: Job? = null
    private val syncedCategories = mutableSetOf<NewsCategory>()

    override fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>> {
        return newsDao.getNewsByCategory(categoryId = category.identity)
            .distinctUntilChanged()
            .map { entities ->
                entities.map(NewsEntity::asNews)
            }.onStart {
                syncJob = syncNewsByCategory(category = category)
            }.onCompletion {
                syncJob?.cancel()
            }
    }

    override fun getSummaryByNews(news: News): Flow<String?> {
        return newsDao.getSummaryByNews(newsUrl = news.url)
            .distinctUntilChanged()
            .map { it?.summary }
    }

    override suspend fun downloadNewsMedia(url: String) {
        fileDownloader.downloadFile(url = url)
    }

    override suspend fun summarizeNews(news: News) {
        val networkSummary = runCatching {
            newsNetworkDataSource.summarizeNews(news = news).summary
        }.getOrNull() ?: throw IllegalStateException("Unable to summarize news content")
        newsDao.insertNewsSummary(
            newsSummaryEntity = NewsSummaryEntity(
                url = news.url,
                summary = networkSummary,
            )
        )
    }

    private fun syncNewsByCategory(category: NewsCategory) = applicationScope.launch {
        Log.d("HOANTAG", "Sync job for category: ${category.name} is started")
        isOnline.filter { it }.collectLatest {
            if (syncedCategories.contains(category)) return@collectLatest
            Log.d("HOANTAG", "Begin syncing news for category: ${category.name}")
            runCatching {
                val dtos = newsNetworkDataSource.fetchNewsByCategory(category = category).news
                val entities = dtos.map { it.asEntity(categoryId = category.identity) }
                newsDao.clearNewsEntitiesByCategory(categoryId = category.identity)
                newsDao.insertNewsEntities(newsEntities = entities)
            }.onFailure {
                Log.e("HOANTAG", "Failed to sync news for category: ${category.name}", it)
            }
            Log.d("HOANTAG", "Finish syncing news for category: ${category.name}")
            syncedCategories.add(category)
        }
    }.also {
        it.invokeOnCompletion {
            Log.d("HOANTAG", "Sync job for category: ${category.name} is stopped")
        }
    }

    private fun NewsDto.asEntity(categoryId: Int) = NewsEntity(
        title = title,
        summary = summary,
        thumbnail = thumbnail,
        categoryId = categoryId,
        url = url,
    )

}