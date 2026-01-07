package com.example.data.datasource.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.News

@Entity(tableName = "news")
data class NewsEntity(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "summary") val summary: String?,
    @ColumnInfo(name = "thumbnail") val thumbnail: String,
    @ColumnInfo(name = "categoryId") val categoryId: Int,
    @ColumnInfo(name = "url") @PrimaryKey val url: String,
)

fun NewsEntity.asNews() = News(
    title = title,
    summary = summary,
    thumbnail = thumbnail,
    url = url,
)

@Entity(tableName = "news_summary")
data class NewsSummaryEntity(
    @ColumnInfo(name = "url") @PrimaryKey val url: String,
    @ColumnInfo(name = "summary") val summary: String,
)