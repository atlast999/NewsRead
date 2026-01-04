package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.data.model.News
import com.example.data.model.NewsCategory
import com.example.presentation.category.CategoryScreen
import com.example.presentation.news.NewsScreen
import com.example.presentation.news.NewsViewModel
import com.example.presentation.read.NewsReadScreen
import com.example.presentation.read.NewsReadViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
object CategoryKey : NavKey

@Serializable
data class NewsKey(val category: NewsCategory) : NavKey

@Serializable
data class ReadKey(val news: News) : NavKey

@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(CategoryKey)
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            // Add the default decorators for managing scenes and saving state
            rememberSaveableStateHolderNavEntryDecorator(),
            // Then add the view model store decorator
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<CategoryKey> {
                CategoryScreen(onCategorySelected = {
                    backStack.add(NewsKey(category = it))
                })
            }
            entry<NewsKey> { newsKey ->
                val viewModel = koinViewModel<NewsViewModel> {
                    parametersOf(newsKey.category)
                }
                NewsScreen(
                    viewModel = viewModel,
                    onNewsSelected = {
                        backStack.add(ReadKey(news = it))
                    },
                )
            }
            entry<ReadKey> { readKey ->
                val viewModel = koinViewModel<NewsReadViewModel> {
                    parametersOf(readKey.news)
                }
                NewsReadScreen(viewModel = viewModel)
            }
        }
    )
}