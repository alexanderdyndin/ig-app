package com.intergroupapplication.presentation.feature.grouplist.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.fragment_group_category.view.*

class GroupListsAdapter(private val items: List<AdmobBannerRecyclerAdapterWrapper>): RecyclerView.Adapter<GroupListsAdapter.GroupListViewHolder>() {

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

        val list: RecyclerView = view.allGroupsList
        val emptyState: TextView = view.emptyText
        val progress: ProgressBar = view.progress_loading

        fun bind(adapter: AdmobBannerRecyclerAdapterWrapper) {
            list.adapter = adapter
            list.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
        }
    }
}

