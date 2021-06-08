package com.intergroupapplication.presentation.feature.grouplist.adapter

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

class GroupListsAdapter(private val items: List<RecyclerView.Adapter<RecyclerView.ViewHolder>>): RecyclerView.Adapter<GroupListsAdapter.GroupListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        return GroupListViewHolder(parent.inflate(R.layout.fragment_group_category))
    }

    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    private data class Coordinate(
        var elementPosition: Int,
        var topPadding: Int
    )

    private val positionValues = hashMapOf(0 to Coordinate(0, 0),
        1 to Coordinate(0, 0),
        2 to Coordinate(0, 0))

    inner class GroupListViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val list: RecyclerView = view.allGroupsList
        val emptyState: TextView = view.emptyText
        val progress: ProgressBar = view.progress_loading
        private val linearLayoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)

        fun bind(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, position: Int) {
            list.adapter = adapter
            list.itemAnimator = null
            list.layoutManager = linearLayoutManager
            addOnScrollListener(position)
            scrollToPosition(position)
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

        private fun addOnScrollListener(typeAdapter: Int) {
            list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    positionValues[typeAdapter]?.apply {
                        elementPosition = linearLayoutManager.findFirstVisibleItemPosition()
                        val currentView = linearLayoutManager.getChildAt(0)
                        currentView?.let { view ->
                            topPadding = view.top - linearLayoutManager.paddingTop
                        }
                    }
                }
            })
        }

        private fun scrollToPosition(typeAdapter: Int) {
            positionValues[typeAdapter]?.run {
                linearLayoutManager.scrollToPositionWithOffset(elementPosition, topPadding)
            }
        }
    }
}

