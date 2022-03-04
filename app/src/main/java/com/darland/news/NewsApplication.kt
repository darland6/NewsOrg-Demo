package com.darland.news

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.darland.news.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NewsApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@NewsApplication)
            modules(appModule)
        }
    }
}
