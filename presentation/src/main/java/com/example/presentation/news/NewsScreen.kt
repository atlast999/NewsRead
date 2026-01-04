package com.example.presentation.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.News
import com.example.presentation.R

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
        state.isLoading -> {
            Box(modifier = modifier) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
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
    ) {
        item {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = "News",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        items(
            items = news,
            key = { it.url }
        ) {
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
    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.globe),
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = newItem.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = {
            Text(
                text = newItem.summary ?: "",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    )
}