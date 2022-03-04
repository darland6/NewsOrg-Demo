package com.example.data

import com.example.data.entities.Article

interface OnlineApi {
    suspend fun getNews(page: Int): ApiResult<List<Article>>
}

interface OfflineApi {
    suspend fun getNews(currentCount: Int): ApiResult<List<Article>>
    suspend fun saveNews(articles: List<Article>)
}

// Use a common result class system for api calls
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}
