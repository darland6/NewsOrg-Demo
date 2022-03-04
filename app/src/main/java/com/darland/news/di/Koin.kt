package com.darland.news.di

import androidx.room.Room
import com.darland.domain.ApiHelper
import com.darland.domain.everything.GetNewsUseCase
import com.darland.news.NetworkHelper
import com.darland.news.R
import com.darland.news.ui.NewsViewModel
import com.example.data.OfflineApi
import com.example.data.OnlineApi
import com.example.data.apis.KtorApi
import com.example.data.apis.RoomApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    // Repos
    single<OnlineApi> {
        KtorApi(
            androidContext().resources.getString(R.string.NEWS_API_URL),
            androidContext().resources.getString(R.string.NEWS_API_KEY)
        )
    }
    single<OfflineApi> {
        RoomApi(get())
    }
    single {
        Room.databaseBuilder(
            androidContext(),
            RoomApi.NewsDatabase::class.java,
            "news_db"
        ).build()
    }

    // Other
    single<ApiHelper> {
        NetworkHelper(androidContext())
    }

    factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    // Usecases
    factoryOf(::GetNewsUseCase)

    // viewmodels
    viewModel { NewsViewModel(get()) }
}
