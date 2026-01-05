package com.example.presentation.read

import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.News
import com.example.presentation.R

@Composable
fun NewsReadScreen(viewModel: NewsReadViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    NewsReadUI(
        modifier = Modifier.fillMaxSize(),
        state = uiState.value,
        onMediaFileDetected = viewModel::onMediaFileDetected,
        onDownloadMedia = viewModel::downloadMedia,
        onRequestSummary = viewModel::summarizeNews,
    )
}

@Composable
private fun NewsReadUI(
    modifier: Modifier = Modifier,
    state: NewsReadState,
    onMediaFileDetected: (String) -> Unit,
    onDownloadMedia: (MediaUrl) -> Unit,
    onRequestSummary: () -> Unit,
) {
    Box(modifier = modifier) {
        NewsReadArea(
            modifier = modifier.fillMaxSize(),
            news = state.news,
            onMediaFileDetected = onMediaFileDetected,
        )
        MediaDownloadFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 8.dp),
            medias = state.downloadableMedias,
            onDownloadMedia = onDownloadMedia,
        )
        AiSummaryFab(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            summary = state.newsSummary,
            onRequestSummary = onRequestSummary,
        )
    }
}

@Composable
private fun NewsReadArea(
    modifier: Modifier = Modifier,
    news: News,
    onMediaFileDetected: (String) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                }
                webViewClient = object : WebViewClient() {

                    override fun onLoadResource(view: WebView?, url: String) {
                        super.onLoadResource(view, url)
                        if (url.endsWith(".mp3") || url.endsWith(".m4a")) {
                            Log.d("HOANTAG", "Found resource URL: $url")
                            onMediaFileDetected.invoke(url)
                        }
                    }
                }
                webChromeClient = WebChromeClient()
            }
        },
        update = { webView ->
            webView.loadUrl(news.url)
        },
        onRelease = { webView ->
            webView.destroy()
        }
    )
}

@Composable
private fun MediaDownloadFab(
    modifier: Modifier = Modifier,
    medias: Set<MediaUrl>,
    onDownloadMedia: (MediaUrl) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = expanded.value,
            enter = scaleIn(transformOrigin = TransformOrigin(1f, 1f)) + fadeIn(),
            exit = scaleOut(transformOrigin = TransformOrigin(1f, 1f)) + fadeOut()
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .wrapContentSize()
                ) {
                    if (medias.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = "No media found. You might need to hit a play button to detect media files.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        medias.forEach { media ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = media.url.substringAfterLast('/'),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onDownloadMedia.invoke(media)
                                    expanded.value = false
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded.value = !expanded.value },
        ) {
            Icon(
                painter = painterResource(R.drawable.download),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun AiSummaryFab(
    modifier: Modifier,
    summary: String?,
    onRequestSummary: () -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    LaunchedEffect(expanded.value, summary) {
        if (expanded.value && summary == null) {
            onRequestSummary.invoke()
        }
    }
    if (expanded.value) {
        SummaryDialog(
            summary = summary,
            onDismissRequest = { expanded.value = false }
        )
    }
    FloatingActionButton(
        modifier = modifier,
        onClick = { expanded.value = !expanded.value },
    ) {
        Icon(
            painter = painterResource(R.drawable.stars),
            contentDescription = null,
        )
    }
}

@Composable
fun SummaryDialog(
    summary: String?,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(all = 16.dp)
                .verticalScroll(
                    state = rememberScrollState()
                ),
            shape = RoundedCornerShape(size = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        ) {
            if (summary == null) {
                Text(
                    text = "Summarizing news...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    modifier = Modifier.padding(all = 16.dp),
                    text = summary,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}