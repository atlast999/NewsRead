package com.example.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.datasource.database.entity.NewsEntity
import com.example.data.datasource.database.entity.NewsSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news WHERE categoryId = :categoryId")
    fun getNewsByCategory(categoryId: Int): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsEntities(newsEntities: List<NewsEntity>)

    @Query("DELETE FROM news WHERE categoryId = :categoryId")
    suspend fun clearNewsEntitiesByCategory(categoryId: Int)

    @Query("SELECT * FROM news_summary WHERE url = :newsUrl")
    suspend fun getNewsSummary(newsUrl: String): List<NewsSummaryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsSummary(newsSummaryEntity: NewsSummaryEntity)

}