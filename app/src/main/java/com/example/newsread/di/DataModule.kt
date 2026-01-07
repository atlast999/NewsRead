package com.example.newsread.di

import com.example.data.datasource.database.NewsDatabase
import com.example.data.datasource.database.dao.NewsDao
import com.example.data.datasource.database.provideNewsDatabase
import com.example.data.datasource.downloader.FileDownloader
import com.example.data.datasource.downloader.impl.AndroidFileDownloader
import com.example.data.datasource.network.NewsNetworkDataSource
import com.example.data.datasource.network.ktor.KtorNetworkDataSource
import com.example.data.datasource.network.ktor.provideKtorHttpClient
import com.example.data.repository.NewsRepository
import com.example.data.repository.impl.OfflineFirstNewsRepository
import com.example.data.util.ConnectivityManagerNetworkMonitor
import com.example.data.util.NetworkMonitor
import com.example.newsread.common.coroutine.AppDispatchers
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val dataModule = module {
    single<NewsDatabase> { provideNewsDatabase(context = get()) }
    single<NetworkMonitor> {
        ConnectivityManagerNetworkMonitor(
            context = get(),
            ioDispatcher = get(qualifier = AppDispatchers.IO.qualifier),
            coroutineScope = get(),
        )
    }
    single<NewsDao> { get<NewsDatabase>().newsDao() }
    single<HttpClient> { provideKtorHttpClient() }
    singleOf(::KtorNetworkDataSource) { bind<NewsNetworkDataSource>() }
    singleOf(::AndroidFileDownloader) { bind<FileDownloader>() }
    factoryOf(::OfflineFirstNewsRepository) { bind<NewsRepository>() }
}