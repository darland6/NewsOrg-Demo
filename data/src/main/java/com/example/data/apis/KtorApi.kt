package com.example.data.apis

import android.util.ArrayMap
import com.example.data.ApiResult
import com.example.data.OnlineApi
import com.example.data.entities.Article
import com.example.data.entities.NewsEntity
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import java.lang.Exception

/**
 * Module Private Api Implementation
 */
class KtorApi(private val url: String, private val apiKey: String) : OnlineApi {
    private val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(DefaultRequest) {
            parameter("apiKey", apiKey)
            parameter("sortBy", "publishedAt") // TODO allow sorting
            parameter("q", "android") // TODO Allow keyword search
            parameter("pageSize", 40) // TODO Adjust for best user performance
            parameter("language", "en") // TODO Adjust for best performance
        }
        engine {
            endpoint {
                connectTimeout = TIMEOUT
                keepAliveTime = TIMEOUT
            }
        }
    }

    // helpers
    private suspend inline fun <reified T> makeApiCall(
        endpoint: String,
        params: Map<String, String>
    ): ApiResult<T> {
        return try {
            ApiResult.Success(
                client.get(url.appendEndpoint(endpoint)) {
                    addAuthHeader()
                    addParameters(params)
                }
            )
        } catch (exception: Exception) {
            ApiResult.Error(exception.message.orEmpty())
        }
    }

    private fun String.appendEndpoint(endpoint: String): String = "$this$endpoint"

    private fun HttpRequestBuilder.addAuthHeader() {
        headers { append(HttpHeaders.Authorization, apiKey) }
    }

    private fun HttpRequestBuilder.addParameters(parameters: Map<String, String>) {
        parameters.forEach { parameter(it.key, it.value) }
    }

    override suspend fun getNews(page: Int): ApiResult<List<Article>> {
        val result = makeApiCall<NewsEntity>(
            EVERYTHING_ENDPOINT,
            ArrayMap<String, String>().apply { put(PARAM_PAGE, page.toString()) }
        )
        return (result as? ApiResult.Success<NewsEntity>)?.let {
            ApiResult.Success(it.data.articles)
        } ?: ApiResult.Error((result as ApiResult.Error).message)
    }

    companion object {
        const val TIMEOUT = 5_000L
        const val EVERYTHING_ENDPOINT = "everything"
        const val PARAM_PAGE = "page"
    }
}
