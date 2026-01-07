package com.example.presentation.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.data.model.News
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(
    viewModel: NewsViewModel,
    onNewsSelected: (News) -> Unit,
) {
    val uiState = viewModel.newsState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NewsUI(
        modifier = Modifier.fillMaxSize(),
        state = uiState.value,
        snackBarHostState = snackBarHostState,
        onNewsSelected = {
            if (uiState.value.isOnline) {
                onNewsSelected.invoke(it)
            } else {
                scope.launch {
                    snackBarHostState.showSnackbar(
                        message = "No Internet To Read News",
                        withDismissAction = true
                    )
                }
            }
        },
    )
}

@Composable
private fun NewsUI(
    modifier: Modifier = Modifier,
    state: NewsState,
    snackBarHostState: SnackbarHostState,
    onNewsSelected: (News) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        contentWindowInsets = WindowInsets(),
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(all = 16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                else -> NewsList(
                    modifier = Modifier.fillMaxSize(), // Fill the entire Scaffold content area
                    latestNews = state.latestNews,
                    relevantNews = state.relevantNews,
                    onNewsSelected = onNewsSelected,
                )
            }
        }
    }
}

@Composable
private fun NewsList(
    modifier: Modifier = Modifier,
    latestNews: List<News>,
    relevantNews: List<News>,
    onNewsSelected: (News) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(all = 16.dp),
                text = "Latest News",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        items(
            items = latestNews,
            key = { it.url }
        ) {
            LatestNewsItem(
                modifier = Modifier.clickable(
                    onClick = { onNewsSelected(it) }
                ),
                newItem = it,
            )
        }
        stickyHeader {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(all = 16.dp),
                text = "Relevant News",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        items(
            items = relevantNews,
            key = { it.url }
        ) {
            RelevantNewsItem(
                modifier = Modifier.clickable(
                    onClick = { onNewsSelected(it) }
                ),
                newItem = it,
            )
        }
    }
}

@Composable
private fun LatestNewsItem(
    modifier: Modifier = Modifier,
    newItem: News,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(width = 120.dp, height = 100.dp),
                model = newItem.thumbnail,
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

@Composable
private fun RelevantNewsItem(
    modifier: Modifier = Modifier,
    newItem: News,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            AsyncImage(
                modifier = Modifier.size(width = 120.dp, height = 100.dp),
                model = newItem.thumbnail,
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = newItem.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
            )
        },
    )
}