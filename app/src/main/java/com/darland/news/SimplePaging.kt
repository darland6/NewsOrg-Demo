package com.darland.news

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darland.domain.ApiHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simple Paging implementation, to let you know when the last items in the list are visible
 */
abstract class SimplePaging(recyclerView: RecyclerView?, private val networkHelper: ApiHelper, private val coroutineScope: CoroutineScope) :
    RecyclerView.OnScrollListener() {
    var page = 1
    private var layoutManager: RecyclerView.LayoutManager?
    private var thresholdToEnd = 5
    private var tempCount = 0
    private var lastHasInternetStatus = networkHelper.hasInternet()

    // Ive added this boolean because the room DB loads so fast its actually slower than the scroll
    var dataLoading = false

    init {
        recyclerView?.addOnScrollListener(this)
        layoutManager = recyclerView?.layoutManager
    }

    fun reset() {
        tempCount = 0
        page = 1
    }

    /**
     * Its important to know when data loaded
     * so that if no more date came we need to rollback our page pre-increment
     */
    fun dataLoaded(count: Int) {
        dataLoading = false
        if (page == 1) {
            tempCount = count
        }
        // If the new count is the same as previous we can rollback the page increase
        else if (tempCount == count && page > 1) {
            page--
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        // Noticed this was code below was causing jank in scrolling so lets put it in a coroutine
        coroutineScope.launch(Dispatchers.Default) {
            if (lastHasInternetStatus != networkHelper.hasInternet()) {
                tempCount = 0
                lastHasInternetStatus = networkHelper.hasInternet()
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val total = layoutManager?.itemCount ?: 0
                val lastVisible = (layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition() ?: 0
                if (total - thresholdToEnd <= lastVisible && total != 0 && !dataLoading) {
                    dataLoading = true
                    tempCount = total
                    loadMore(++page)
                }
            }
        }
    }

    abstract fun loadMore(page: Int)
}
