package com.example.newsread

import android.app.Application
import com.example.newsread.di.coroutineModule
import com.example.newsread.di.dataModule
import com.example.newsread.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                coroutineModule,
                dataModule,
                presentationModule,
            )
        }
    }
}