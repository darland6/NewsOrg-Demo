package com.example.data.apis

import com.example.data.ApiResult
import com.example.data.entities.Article
import com.example.data.entities.ArticleDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class RoomApiTest {
    private val mockDb = mock(RoomApi.NewsDatabase::class.java)
    private val mockDao = mock(ArticleDao::class.java)

    @Test
    fun `when getting news then it should return successfully`() {
        val api = RoomApi(mockDb)
        mockDao.stub {
            onBlocking { mockDao.getNewsByOffset(0) }.doReturn(emptyList())
        }
        `when`(mockDb.articleDao()).thenReturn(mockDao)
        val result = runBlocking { api.getNews(0) }
        verify(mockDb).articleDao()
        Assert.assertTrue(result is ApiResult.Success)
        Assert.assertTrue((result as? ApiResult.Success<List<Article>>)?.data?.isEmpty() ?: false)
    }
}
