package com.intergroupapplication.presentation.feature.userlist.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.item_user_list.view.*

class UserListAdapter : PagingDataAdapter<GroupUserEntity, UserListAdapter.UserListViewHolder>(diffUtil) {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<GroupUserEntity>() {
            override fun areItemsTheSame(oldItem: GroupUserEntity, newItem: GroupUserEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: GroupUserEntity, newItem: GroupUserEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        getItem(position)?.run {
            holder.bind(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        return UserListViewHolder(parent.inflate(R.layout.item_user_list))
    }

    inner class UserListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: GroupUserEntity) {
            itemView.run {
                nameTxt.text = item.surName
            }
        }
    }

}