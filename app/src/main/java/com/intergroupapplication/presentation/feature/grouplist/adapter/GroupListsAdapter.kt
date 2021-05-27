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
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentGroupCategoryBinding
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.exstension.show

class GroupListsAdapter(private val items: List<RecyclerView.Adapter<RecyclerView.ViewHolder>>): RecyclerView.Adapter<GroupListsAdapter.GroupListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        return GroupListViewHolder(parent.inflate(R.layout.fragment_group_category))
    }

    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    class GroupListViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(FragmentGroupCategoryBinding::bind)

        private val list: RecyclerView = viewBinding.allGroupsList
        private val emptyState: TextView = viewBinding.emptyText
        private val progress: ProgressBar = viewBinding.progressLoading

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

