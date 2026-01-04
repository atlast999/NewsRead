package com.example.data.model

import kotlinx.serialization.Serializable

@Serializable
data class News(
    val title: String,
    val summary: String?,
    val thumbnail: String,
    val url: String,
)

enum class NewsCategory(
    val identity: Int,
    val parameter: String,
    val title: String,
) {
    INTERNATIONAL(
        identity = 0,
        parameter = "the-gioi",
        title = "International",
    ),
    TRAVEL(
        identity = 1,
        parameter = "du-lich",
        title = "Travel",
    ),
    SPORTS(
        identity = 2,
        parameter = "the-thao",
        title = "Sports",
    ),
    ENTERTAINMENT(
        identity = 3,
        parameter = "giai-tri",
        title = "Entertainment",
    ),
    TECHNOLOGY(
        identity = 4,
        parameter = "cong-nghe",
        title = "Technology",
    )
}