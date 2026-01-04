package com.example.presentation.category

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.model.NewsCategory
import com.example.presentation.R

@Composable
fun CategoryScreen(onCategorySelected: (NewsCategory) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(all = 16.dp),
            text = "News Category",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleLarge,
        )
        NewsCategory.entries.forEach { category ->
            ListItem(
                modifier = Modifier.clickable(
                    onClick = { onCategorySelected.invoke(category) }
                ),
                leadingContent = {
                    Icon(
                        painter = painterResource(category.getIcon()),
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
            )
        }
    }
}

@DrawableRes
private fun NewsCategory.getIcon() = when(this) {
    NewsCategory.INTERNATIONAL -> R.drawable.globe
    NewsCategory.TRAVEL -> R.drawable.travel
    NewsCategory.SPORTS -> R.drawable.sports
    NewsCategory.ENTERTAINMENT -> R.drawable.entertainment
    NewsCategory.TECHNOLOGY -> R.drawable.desktop
}