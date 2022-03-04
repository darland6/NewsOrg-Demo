package com.darland.domain.everything

import com.darland.domain.ApiHelper
import com.darland.domain.UseCase
import com.example.data.ApiResult
import com.example.data.OfflineApi
import com.example.data.OnlineApi
import com.example.data.entities.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

/**
 * The purpose of this use case is to retrieve data.
 * It handles retrieving and caching of the data as well seamlessly to the
 * consumer of the usecase
 */
class GetNewsUseCase(
    private val apiHelper: ApiHelper,
    private val ktorApi: OnlineApi,
    private val room: OfflineApi,
    private val coroutineScope: CoroutineScope
) :
    UseCase<ApiResult<List<Article>>>() {
    var page = 1
    var currentCount = 0

    override suspend fun execute(): ApiResult<List<Article>> {
        return if (apiHelper.hasInternet()) {
            var result = ktorApi.getNews(page)
            (result as? ApiResult.Success<List<Article>>)?.let {
                val imageFilteredResults = it.data.filter { it.urlToImage?.isNotEmpty() == true }.toSet().toList()
                result = ApiResult.Success(imageFilteredResults)
                coroutineScope.async { room.saveNews(imageFilteredResults) }.start()
            }
            result
        } else {
            // retrieve from cache
            room.getNews(currentCount)
        }
    }
}
