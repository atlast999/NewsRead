package com.example.data.datasource.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.datasource.database.dao.NewsDao
import com.example.data.datasource.database.entity.NewsEntity
import com.example.data.datasource.database.entity.NewsSummaryEntity

@Database(
    entities = [NewsEntity::class, NewsSummaryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}

fun provideNewsDatabase(context: Context): NewsDatabase {
    return Room.databaseBuilder(
        context,
        NewsDatabase::class.java,
        "news_database"
    ).build()
}