package com.example.data.datasource.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    @SerialName("articles") val news: List<NewsDto>
)

@Serializable
data class NewsDto(
    @SerialName("title") val title: String,
    @SerialName("summary") val summary: String?,
    @SerialName("thumbnail") val thumbnail: String,
    @SerialName("url") val url: String,
)

@Serializable
data class SummarizeNewsResponse(
    @SerialName("summary") val summary: String
)