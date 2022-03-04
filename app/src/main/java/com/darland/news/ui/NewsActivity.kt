package com.darland.news.ui

import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.darland.news.R
import com.darland.news.SimplePaging
import com.darland.news.databinding.ActivityNewsBinding
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private val adapter = NewsAdapter { viewModel.onItemClick(it) }
    private val viewModel: NewsViewModel by viewModel()
    private var simplerPager: SimplePaging? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        binding.content.refreshList.setOnRefreshListener {
            simplerPager?.reset()
            viewModel.refresh()
        }
        binding.content.list.adapter = adapter

        // Setup our infinite scrolling
        simplerPager = object : SimplePaging(binding.content.list, get(), lifecycleScope) {
            override fun loadMore(page: Int) {
                viewModel.loadMore(page)
            }
        }

        // Add Margins around Items
        binding.content.list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val padding = resources.getDimension(R.dimen.default_padding).toInt()
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.left = padding
                outRect.right = padding
                outRect.top = padding
            }
        })

        // Observer ViewModel
        viewModel.displayModel.observe(this) {
            when (it) {
                is NewsViewModel.State.Success -> success(it)
                is NewsViewModel.State.Loading -> loading()
            }
        }

        viewModel.errorEvent.observe(this) {
            error(it)
        }

        viewModel.launchUrlEvent.observe(this) {
            CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(it))
        }
        viewModel.noResultsEvent.observe(this) {
            binding.content.refreshList.isRefreshing = false
            simplerPager?.dataLoaded(it.currentSize)
            // Default snackbar doesn't like dark mode, gotta look into
            Snackbar.make(binding.root, it.msg, Snackbar.LENGTH_SHORT).show()
        }

        viewModel.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.content.list.removeOnScrollListener(simplerPager as RecyclerView.OnScrollListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.children?.forEach {
            it.isChecked = when (it.itemId) {
                R.id.forceDarkMode -> AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_YES
                R.id.forceLightMode -> AppCompatDelegate.getDefaultNightMode() == MODE_NIGHT_NO
                else -> false
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        simplerPager?.dataLoading = true
        when (item.itemId) {
            R.id.forceDarkMode -> {
                item.isChecked = !item.isChecked
                AppCompatDelegate.setDefaultNightMode(if (item.isChecked) MODE_NIGHT_YES else MODE_NIGHT_FOLLOW_SYSTEM)
            }
            R.id.forceLightMode -> {
                item.isChecked = !item.isChecked
                AppCompatDelegate.setDefaultNightMode(if (item.isChecked) MODE_NIGHT_NO else MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
        return true
    }

    // After testing it was necessary to sync the paging class with the data size and fetched page,
    // This takes care of edge cases like syncing paging between offline and online db switching
    private fun success(state: NewsViewModel.State.Success) {
        binding.content.refreshList.isRefreshing = false
        simplerPager?.page = state.page
        simplerPager?.dataLoaded(state.items.size)
        adapter.submitList(state.items)
    }

    private fun loading() {
        binding.content.refreshList.isRefreshing = true
    }

    private fun error(msg: String) {
        binding.content.refreshList.isRefreshing = false
        // Default snackbar doesn't like dark mode, gotta look into
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }
}
