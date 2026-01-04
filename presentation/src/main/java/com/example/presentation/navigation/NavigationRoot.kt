package com.example.presentation.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun NavigationRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(CategoryKey)
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = listDetailStrategy,
        entryDecorators = listOf(
            // Add the default decorators for managing scenes and saving state
            rememberSaveableStateHolderNavEntryDecorator(),
            // Then add the view model store decorator
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<CategoryKey>(
                metadata = ListDetailSceneStrategy.listPane(),
            ) {
                CategoryScreen(
                    onCategorySelected = { category ->
                        backStack.addSingleTop(NewsKey(category = category))
                    },
                )
            }
            entry<NewsKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { newsKey ->
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

private inline fun <reified T: NavKey> NavBackStack<NavKey>.addSingleTop(navKey: T) {
    removeIf { it is T }
    add(navKey)
}