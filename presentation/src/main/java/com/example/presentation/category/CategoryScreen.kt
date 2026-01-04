package com.example.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.NewsCategory

@Composable
fun CategoryScreen(onCategorySelected: (NewsCategory) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NewsCategory.entries.forEach { category ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .background(
                        color = Color.Green,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(
                        onClick = { onCategorySelected(category) }
                    )
            ) {
                Text(
                    text = category.title,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}