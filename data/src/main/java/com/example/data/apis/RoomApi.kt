package com.example.data.apis

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.ApiResult
import com.example.data.OfflineApi
import com.example.data.entities.Article
import com.example.data.entities.ArticleDao
import java.lang.Exception

/**
 * Module Private Api Implementation
 */
class RoomApi(private val db: NewsDatabase) : OfflineApi {
    @Database(entities = [Article::class], version = 1)
    abstract class NewsDatabase : RoomDatabase() {
        abstract fun articleDao(): ArticleDao
    }

    override suspend fun getNews(currentCount: Int): ApiResult<List<Article>> {
        val dao = db.articleDao()
        val pagedNews = dao.getNewsByOffset(currentCount)
        return ApiResult.Success(pagedNews)
    }

    override suspend fun saveNews(articles: List<Article>) {
        try {
            db.articleDao().insertNews(articles)
        } catch (e: Exception) {
            throw Exception(e)
        }
    }
}
