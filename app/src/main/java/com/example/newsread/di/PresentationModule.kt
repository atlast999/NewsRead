package com.example.newsread.di

import com.example.presentation.news.NewsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::NewsViewModel)
}