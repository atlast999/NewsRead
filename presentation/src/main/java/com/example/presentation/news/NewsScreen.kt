package com.example.presentation.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.News

@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onNewsSelected: (News) -> Unit,
) {
    val uiState = viewModel.newsState.collectAsStateWithLifecycle()
    NewsUI(
        modifier = Modifier.fillMaxSize(),
        state = uiState.value,
        onNewsSelected = onNewsSelected,
    )
}

@Composable
private fun NewsUI(
    modifier: Modifier = Modifier,
    state: NewsState,
    onNewsSelected: (News) -> Unit,
) {
    when {
        state.isLoading -> CircularProgressIndicator()
        state.errorMessage != null -> {
            //Show error message
        }

        else -> NewsList(
            modifier = modifier,
            news = state.news,
            onNewsSelected = onNewsSelected,
        )
    }
}

@Composable
private fun NewsList(
    modifier: Modifier = Modifier,
    news: List<News>,
    onNewsSelected: (News) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        items(
            items = news, key = { it.url }) {
            NewsItem(
                modifier = Modifier.clickable(
                    onClick = { onNewsSelected(it) }
                ),
                newItem = it,
            )
        }
    }
}

@Composable
private fun NewsItem(
    modifier: Modifier = Modifier,
    newItem: News,
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Green, shape = RoundedCornerShape(size = 10.dp)
            )
            .padding(vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .size(size = 140.dp)
                .background(color = Color.Red)
                .align(Alignment.CenterVertically)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
        ) {
            Text(
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                text = newItem.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(height = 8.dp))
            Text(
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
                text = newItem.summary ?: "",
                fontSize = 20.sp
            )
        }
    }
}