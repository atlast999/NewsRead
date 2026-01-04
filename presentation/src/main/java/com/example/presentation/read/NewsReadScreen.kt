package com.example.presentation.read

import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.News

@Composable
fun NewsReadScreen(viewModel: NewsReadViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    NewsReadUI(
        modifier = Modifier.fillMaxSize(),
        state = uiState.value,
        onMediaFileDetected = viewModel::onMediaFileDetected,
        onDownloadMedia = viewModel::downloadMedia,
    )
}

@Composable
private fun NewsReadUI(
    modifier: Modifier = Modifier,
    state: NewsReadState,
    onMediaFileDetected: (String) -> Unit,
    onDownloadMedia: (MediaUrl) -> Unit,
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
                .padding(16.dp)
                .wrapContentSize(),
            medias = state.downloadableMedias,
            onDownloadMedia = onDownloadMedia,
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

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {

                        val url = request?.url.toString()

                        if (url.endsWith(".mp3") || url.endsWith(".m4a")) {
                            // You found the audio URL
                            Log.d("HOANTAG", "Found audio URL: $url")
                            onMediaFileDetected.invoke(url)
                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }
                webChromeClient = WebChromeClient()
            }
        },
        update = { webView ->
            webView.loadUrl(news.url)
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
                                        overflow = TextOverflow.Ellipsis
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
            Text(
                text = "↓",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
