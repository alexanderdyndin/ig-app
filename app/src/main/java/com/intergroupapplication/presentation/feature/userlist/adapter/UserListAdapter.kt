package com.intergroupapplication.presentation.feature.userlist.adapter

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.item_subscribers_in_list.view.*

class UserListAdapter : PagingDataAdapter<GroupUserEntity, UserListAdapter.UserListViewHolder>(diffUtil) {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<GroupUserEntity>() {
            override fun areItemsTheSame(oldItem: GroupUserEntity, newItem: GroupUserEntity): Boolean {
                return oldItem.idProfile == newItem.idProfile
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
        return UserListViewHolder(parent.inflate(R.layout.item_subscribers_in_list))
    }

    inner class UserListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(item: GroupUserEntity) {
            itemView.run {
                item_group__list_header.text = item.firstName + " " + item.surName
                idUser.text = "ID: ${item.idProfile}"
                item_group__posts.text = item.postsCount.toString()
                item_group__comments.text = item.commentsCount.toString()
                item_group__dislike.text = item.dislikeCount.toString()
                item_group__like.text = item.dislikeCount.toString()
            }
        }
    }

}