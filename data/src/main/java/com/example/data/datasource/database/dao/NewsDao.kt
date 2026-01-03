package com.example.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.datasource.database.entity.NewsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news WHERE categoryId = :categoryId")
    fun getNewsByCategory(categoryId: Int): Flow<List<NewsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsEntities(newsEntities: List<NewsEntity>)

    @Query("DELETE FROM news")
    suspend fun clearNewsEntities()
}