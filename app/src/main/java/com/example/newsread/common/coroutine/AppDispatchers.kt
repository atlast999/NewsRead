package com.example.newsread.common.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

enum class AppDispatchers {
    IO,
    Default,
}

fun provideApplicationScope(dispatcher: CoroutineDispatcher): CoroutineScope {
    return CoroutineScope(SupervisorJob() + dispatcher)
}