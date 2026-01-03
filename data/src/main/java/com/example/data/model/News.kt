package com.example.data.model

import androidx.annotation.StringRes

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
    TECHNOLOGY(
        identity = 1,
        parameter = "cong-nghe",
        title = "Technology",
    )
}