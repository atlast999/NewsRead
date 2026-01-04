package com.example.data.repository.impl

import android.util.Log
import com.example.data.datasource.database.dao.NewsDao
import com.example.data.datasource.database.entity.NewsEntity
import com.example.data.datasource.database.entity.asNews
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
): NewsRepository {

    private var syncJob: Job? = null

    override fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>> {
        return newsDao.getNewsByCategory(categoryId = category.identity).map { entities ->
            entities.map(NewsEntity::asNews)
        }.onStart {
            Log.d("HOANTAG", "Start getting news -> Trigger sync")
            syncJob = syncNewsByCategory(category = category)
        }.onCompletion {
            Log.d("HOANTAG", "Completed getting news -> Cancel sync")
            syncJob?.cancel()
            syncJob = null
        }
    }

    private fun syncNewsByCategory(category: NewsCategory) = applicationScope.launch(ioDispatcher) {
        Log.d("HOANTAG", "Start syncing news")
        val dtos = newsNetworkDataSource.fetchNewsByCategory(category = category).news
        val entities = dtos.map { it.asEntity(categoryId = category.identity) }
        newsDao.clearNewsEntitiesByCategory(categoryId = category.identity)
        newsDao.insertNewsEntities(newsEntities = entities)
        Log.d("HOANTAG", "Finish syncing news")
    }

    private fun NewsDto.asEntity(categoryId: Int) = NewsEntity(
        title = title,
        summary = summary,
        thumbnail = thumbnail,
        categoryId = categoryId,
        url = url,
    )

}