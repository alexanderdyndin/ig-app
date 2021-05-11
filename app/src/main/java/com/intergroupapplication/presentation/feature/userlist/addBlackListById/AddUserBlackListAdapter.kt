package com.intergroupapplication.presentation.feature.userlist.addBlackListById

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.item_add_user_black_list.view.*

class AddUserBlackListAdapter : PagingDataAdapter<AddBlackListUserItem, AddUserBlackListAdapter.AddUserBlackListViewHolder>(diffUtils) {
    companion object {
        var selectItem: (userItem: AddBlackListUserItem, position: Int) -> Unit = { _, _ -> }
        private val diffUtils = object : DiffUtil.ItemCallback<AddBlackListUserItem>() {
            override fun areItemsTheSame(oldItem: AddBlackListUserItem, newItem: AddBlackListUserItem): Boolean {
                return oldItem.idProfile == newItem.idProfile
            }

            override fun areContentsTheSame(oldItem: AddBlackListUserItem, newItem: AddBlackListUserItem): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onBindViewHolder(holder: AddUserBlackListViewHolder, position: Int) {
        getItem(position)?.run {
            holder.bind(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddUserBlackListViewHolder {
        return AddUserBlackListViewHolder(parent.inflate(R.layout.item_add_user_black_list))
    }

    inner class AddUserBlackListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        @SuppressLint("SetTextI18n")
        fun bind(userEntity: AddBlackListUserItem) {
            itemView.run {
                nameTxt.text = userEntity.fullName
                profileIdTxt.text = "ID: ${userEntity.idProfile}"
                if (userEntity.avatar.isNotEmpty()) userAvatarHolder.showAvatar(userEntity.avatar)
                else userAvatarHolder.showAvatar(R.drawable.variant_10)

                if (userEntity.isSelected) {
                    nameUsersAddBlackList.background = ContextCompat.getDrawable(context, R.drawable.bg_dark_element_radius_2dp)
                    icCheckUser.setBackgroundResource(R.drawable.ic_check_red)
                } else {
                    nameUsersAddBlackList.background = ContextCompat.getDrawable(context, R.drawable.bg_greyelement_radius_2dp)
                    icCheckUser.setBackgroundResource(R.drawable.ic_check_black)
                }

                setOnClickListener {
                    selectItem(userEntity, layoutPosition)
                }
            }
        }
    }
}