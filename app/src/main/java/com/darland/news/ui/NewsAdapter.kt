package com.darland.news.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.darland.news.R
import com.darland.news.databinding.ItemNewCardBigBinding
import com.darland.news.databinding.ItemNewCardSmallBinding
import com.darland.news.databinding.ItemNewCardStackedBinding
import java.lang.Exception

class NewsAdapter(private val onItemClick: OnItemClick) : ListAdapter<NewsViewModel.AdapterItem, RecyclerView.ViewHolder>(Differ()) {
    private var inflater: LayoutInflater? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (inflater == null) inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            Type.SMALL.value -> NewsViewHolderSmall(
                ItemNewCardSmallBinding.inflate(
                    inflater!!,
                    parent, false
                ),
                onItemClick
            )
            Type.STACKED.value -> NewsViewHolderStacked(
                ItemNewCardStackedBinding.inflate(
                    inflater!!,
                    parent, false
                ),
                onItemClick
            )
            Type.BIG.value -> NewsViewHolderBig(
                ItemNewCardBigBinding.inflate(
                    inflater!!,
                    parent, false
                ),
                onItemClick
            )
            else -> throw Exception("Unknown ViewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NewsViewModel.AdapterItem.Stacked -> Type.STACKED.value
            is NewsViewModel.AdapterItem.Big -> Type.BIG.value
            is NewsViewModel.AdapterItem.Small -> Type.SMALL.value
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val adapterItem = getItem(position)) {
            is NewsViewModel.AdapterItem.Stacked -> holder.asBinder().bind(adapterItem.article)
            is NewsViewModel.AdapterItem.Big -> holder.asBinder().bind(adapterItem.article)
            is NewsViewModel.AdapterItem.Small -> holder.asBinder().bind(adapterItem.article)
        }
    }

    private fun RecyclerView.ViewHolder.asBinder(): Binder {
        return this as Binder
    }

    private class Differ : DiffUtil.ItemCallback<NewsViewModel.AdapterItem>() {
        override fun areItemsTheSame(
            oldItem: NewsViewModel.AdapterItem,
            newItem: NewsViewModel.AdapterItem
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: NewsViewModel.AdapterItem,
            newItem: NewsViewModel.AdapterItem
        ): Boolean =
            oldItem == newItem
    }

    enum class Type(val value: Int) {
        SMALL(0),
        STACKED(1),
        BIG(2)
    }

    interface Binder {
        fun bind(article: NewsViewModel.Item)
    }
}

typealias OnItemClick = (Int) -> Unit

class NewsViewHolderSmall(private val binding: ItemNewCardSmallBinding, onItemClick: OnItemClick) : NewsAdapter.Binder,
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener { onItemClick.invoke(absoluteAdapterPosition) }
    }

    override fun bind(article: NewsViewModel.Item) {
        binding.image.load(article.urlToImage) {
            crossfade(true)
            memoryCacheKey(article.title)
            error(R.drawable.news_api)
        }
        binding.timestamp.text = article.date
        binding.title.text = article.title
        binding.summary.text = article.summary
    }
}

class NewsViewHolderStacked(private val binding: ItemNewCardStackedBinding, onItemClick: OnItemClick) : NewsAdapter.Binder,
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener { onItemClick.invoke(absoluteAdapterPosition) }
    }

    override fun bind(article: NewsViewModel.Item) {
        binding.image.load(article.urlToImage) {
            crossfade(true)
            memoryCacheKey(article.title)
            error(R.drawable.news_api)
        }
        binding.title.text = article.title
        binding.summary.text = article.summary
    }
}

class NewsViewHolderBig(private val binding: ItemNewCardBigBinding, onItemClick: OnItemClick) : NewsAdapter.Binder,
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener { onItemClick.invoke(absoluteAdapterPosition) }
    }

    override fun bind(article: NewsViewModel.Item) {
        binding.image.load(article.urlToImage) {
            crossfade(true)
            memoryCacheKey(article.title)
            error(R.drawable.news_api)
        }
        binding.title.text = article.title
    }
}
