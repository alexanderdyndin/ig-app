package com.intergroupapplication.presentation.feature.audiolist.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.exstension.show
import kotlinx.android.synthetic.main.fragment_group_category.view.*

class AudioListsAdapter(private val items: List<RecyclerView.Adapter<RecyclerView.ViewHolder>>): RecyclerView.Adapter<AudioListsAdapter.AudioListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioListViewHolder {
        return AudioListViewHolder(parent.inflate(R.layout.fragment_group_category))
    }

    override fun onBindViewHolder(holder: AudioListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    inner class AudioListViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val list: RecyclerView = view.allGroupsList
        val emptyState: TextView = view.emptyText
        val progress: ProgressBar = view.progress_loading

        fun bind(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
            list.adapter = adapter
            list.itemAnimator = null
            list.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
            if (adapter is ConcatAdapter) {
                adapter.adapters.forEach { adapter ->
                    if (adapter is PagingDataAdapter<*, *> ) {
                        adapter.addLoadStateListener {
                            when (it.refresh) {
                                is LoadState.Loading -> {
                                    if (adapter.itemCount == 0) {
                                        progress.show()
                                    }
                                    emptyState.hide()
                                }
                                is LoadState.Error -> {
                                    progress.hide()
                                    emptyState.hide()
                                }
                                is LoadState.NotLoading -> {
                                    progress.hide()
                                    if (adapter.itemCount == 0) {
                                        emptyState.show()
                                    } else {
                                        emptyState.hide()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
