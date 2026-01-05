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
import com.example.newsread.common.coroutine.AppDispatchers
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val dataModule = module {
    single<NewsDatabase> { provideNewsDatabase(context = get()) }
    single<NewsDao> { get<NewsDatabase>().newsDao() }
    single<HttpClient> { provideKtorHttpClient() }
    singleOf(::KtorNetworkDataSource) { bind<NewsNetworkDataSource>() }
    singleOf(::AndroidFileDownloader) { bind<FileDownloader>() }
    single<NewsRepository> { OfflineFirstNewsRepository(
        newsDao = get(),
        newsNetworkDataSource = get(),
        applicationScope = get(),
        fileDownloader = get(),
    ) }
}