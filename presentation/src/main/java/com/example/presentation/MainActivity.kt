package com.example.presentation

import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.NewsCategory
import com.example.presentation.news.NewsScreen
import com.example.presentation.news.NewsViewModel
import com.example.presentation.theme.NewsReadTheme
import org.koin.compose.viewmodel.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsReadTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestNewsScreen(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(20.dp),
                        viewModel = koinViewModel(),
                    )
//                    NewsViewer(
//                        modifier = Modifier
//                            .padding(innerPadding)
//                            .padding(20.dp)
//                    )
                }
            }
        }
    }
}

@Composable
fun TestNewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel,
) {
    NewsScreen(viewModel = viewModel)
}

@Composable
fun NewsViewer(modifier: Modifier = Modifier) {
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

                        if (url.endsWith(".mp3") || url.endsWith(".m4a") || url.contains(".m3u8")) {
                            // You found the audio URL
                            Log.d("HOANTAG", "Found audio URL: $url")
                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }
                webChromeClient = WebChromeClient()
//                loadUrl("https://vnexpress.net/3-nguyen-tac-tich-san-vang-khi-gia-tang-cao-5000401.html")
//                loadUrl("https://vnexpress.net/vne-go/podcast/tai-chinh-ca-nhan")
//                loadUrl("https://google.com")
                loadUrl("https://dantri.com.vn/kinh-doanh/podcast-bi-mat-hoa-hong-ban-o-to-hang-sang-20230820091736120.htm")
            }
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsReadTheme {
        Greeting("Android")
    }
}