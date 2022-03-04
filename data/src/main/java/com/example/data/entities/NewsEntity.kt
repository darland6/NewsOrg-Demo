package com.example.data.entities

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.serialization.Serializable

@Serializable
data class NewsEntity(
    val status: String,
    val totalResults: Int,
    val articles: List<Article>
)

@Entity
@Serializable
data class Article(
    @Embedded val source: Source,
    val author: String?,
    @PrimaryKey val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

@Serializable
data class Source(
    val id: String?,
    val name: String?
)

@Dao
interface ArticleDao {
    // TODO the limit here should be passed in and not hardcoded
    @Query("SELECT * from article limit 40 offset :currentCount")
    suspend fun getNewsByOffset(currentCount: Int): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNews(articles: List<Article>)
}
