package com.example.newsread.di

import com.example.newsread.common.coroutine.AppDispatchers
import com.example.newsread.common.coroutine.provideApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val coroutineModule = module {
    factory(qualifier = AppDispatchers.IO.qualifier) {
        Dispatchers.IO
    }
    factory(qualifier = AppDispatchers.Default.qualifier) {
        Dispatchers.Default
    }
    single<CoroutineScope> {
        provideApplicationScope(dispatcher = get(qualifier = AppDispatchers.Default.qualifier))
    }
}