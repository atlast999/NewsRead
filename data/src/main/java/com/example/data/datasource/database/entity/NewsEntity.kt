package com.example.data.datasource.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.News

@Entity(tableName = "news")
data class NewsEntity(
    val title: String,
    val summary: String?,
    val thumbnail: String,
    val categoryId: Int,
    @PrimaryKey val url: String,
)

fun NewsEntity.asNews() = News(
    title = title,
    summary = summary,
    thumbnail = thumbnail,
    url = url,
)

fun News.asEntity(categoryId: Int) = NewsEntity(
    title = title,
    summary = summary,
    thumbnail = thumbnail,
    categoryId = categoryId,
    url = url,
)