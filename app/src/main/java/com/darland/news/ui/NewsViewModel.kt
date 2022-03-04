package com.darland.news.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darland.domain.everything.GetNewsUseCase
import com.darland.news.helpers.SingleLiveEvent
import com.example.data.ApiResult
import com.example.data.entities.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewsViewModel(private val getNewsUseCase: GetNewsUseCase) : ViewModel() {
    private val _displayModel = MutableLiveData<State>()
    val displayModel: LiveData<State>
        get() = _displayModel
    private val articles = mutableListOf<AdapterItem>()
    val launchUrlEvent = SingleLiveEvent<String>()
    val noResultsEvent = SingleLiveEvent<NoResults>()
    val errorEvent = SingleLiveEvent<String>()
    private var lastResultCount = 0

    fun loadMore(nextPage: Int) {
        getNewsUseCase.page = nextPage
        fetchData()
    }

    fun onItemClick(position: Int) {
        articles.getOrNull(position)?.let {
            launchUrlEvent.value =
                when (it) {
                    is AdapterItem.Big -> it.article.url
                    is AdapterItem.Small -> it.article.url
                    is AdapterItem.Stacked -> it.article.url
                }
        }
    }

    fun onCreate() {
        if (articles.size == 0) {
            fetchData()
        } else {
            updateDisplay()
        }
    }

    fun refresh() {
        articles.clear()
        getNewsUseCase.page = 1
        getNewsUseCase.currentCount = 0
        fetchData()
    }

    private fun fetchData() {
        _displayModel.postValue(State.Loading)
        viewModelScope.launch {
            when (val data = withContext(Dispatchers.IO) { getNewsUseCase.execute() }) {
                is ApiResult.Success -> {
                    lastResultCount = data.data.size
                    data.data.forEachIndexed { index, article ->
                        run {
                            val appendedIndex = articles.size + index
                            // Generate random number between 0 and 2 to choose a layout
                            val item = when ((0..2).shuffled().last()) {
                                0 -> AdapterItem.Small(article.toItem(), appendedIndex)
                                1 -> AdapterItem.Stacked(article.toItem(), appendedIndex)
                                2 -> AdapterItem.Big(article.toItem(), appendedIndex)
                                else -> throw java.lang.Exception("The shuffling didn't work")
                            }
                            articles.add(item)
                        }
                    }
                    if (data.data.isEmpty()) {
                        noResultsEvent.value = NoResults("No more results", articles.size)
                    }
                }
                is ApiResult.Error -> {
                    errorEvent.postValue(data.message)
                }
            }
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        _displayModel.postValue(State.Success(articles.toList(), getNewsUseCase.page))
        // currentCount helps sync data when switching between dbs
        getNewsUseCase.currentCount = articles.size
    }

    private fun String.toDate(): Date? {
        return iso8601Format.parse(this)
    }

    private fun Article.toItem(): Item {
        val date = publishedAt?.toDate()?.let { dateFormat.format(it) }.orEmpty()
        return Item(title.trim(), description.orEmpty().trim(), date, urlToImage.orEmpty(), url)
    }

    /**
     * A highlevel view of the state of the UI
     */
    sealed class State {
        object Loading : State()
        data class Success(val items: List<AdapterItem>, val page: Int) : State()
    }

    data class NoResults(val msg: String, val currentSize: Int) : State()

    /**
     * Possible Item types for the adapter
     */
    sealed class AdapterItem(val id: Int) {
        data class Big(val article: Item, val index: Int) : AdapterItem(index)
        data class Small(val article: Item, val index: Int) : AdapterItem(index)
        data class Stacked(val article: Item, val index: Int) : AdapterItem(index)
    }

    data class Item(
        val title: String,
        val summary: String,
        val date: String,
        val urlToImage: String,
        val url: String
    )

    companion object {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    }
}
