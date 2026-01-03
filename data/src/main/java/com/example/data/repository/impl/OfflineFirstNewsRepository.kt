package com.example.data.repository.impl

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfflineFirstNewsRepository(
    private val newsDao: NewsDao,
    private val newsNetworkDataSource: NewsNetworkDataSource,
    private val applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher,
): NewsRepository {

    override fun getNewsByCategoryFlow(category: NewsCategory): Flow<List<News>> {
        syncNewsByCategory(category = category)
        return newsDao.getNewsByCategory(categoryId = category.identity).map { entities ->
            entities.map(NewsEntity::asNews)
        }
    }

    private fun syncNewsByCategory(category: NewsCategory) = applicationScope.launch(ioDispatcher) {
        val dtos = newsNetworkDataSource.fetchNewsByCategory(category = category).news
        val entities = dtos.map { it.asEntity(categoryId = category.identity) }
        newsDao.clearNewsEntities()
        newsDao.insertNewsEntities(newsEntities = entities)
    }

    private fun NewsDto.asEntity(categoryId: Int) = NewsEntity(
        title = title,
        summary = summary,
        thumbnail = thumbnail,
        categoryId = categoryId,
        url = url,
    )

}