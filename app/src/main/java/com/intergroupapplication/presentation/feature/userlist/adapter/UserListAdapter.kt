package com.intergroupapplication.presentation.feature.userlist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.item_subscribers_in_list.view.*

enum class TypeUserList {
    ALL, BLOCKED, ADMINISTRATORS
}

class UserListAdapter(
        private val imageLoadingDelegate: ImageLoadingDelegate,
        private val typeUserList: TypeUserList
) : PagingDataAdapter<GroupUserEntity, UserListAdapter.UserListViewHolder>(diffUtil) {

    companion object {
        var banUserClickListener: (userId: String, position: Int) -> Unit = { _, _ -> }
        var deleteBanUserClickListener: (userId: String, position: Int) -> Unit = { _, _ -> }
        var isAdmin = false
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
                itemGroupListHeader.text = item.firstName + " " + item.surName
                idUser.text = "ID: ${item.idProfile}"
                itemGroupPosts.text = item.postsCount.toString()
                itemGroupComments.text = item.commentsCount.toString()
                itemGroupDislike.text = item.dislikeCount.toString()
                itemGroupLike.text = item.dislikeCount.toString()

                if (item.avatar.isNotEmpty()) imageLoadingDelegate.loadImageFromUrl(item.avatar, groupAvatarHolder)
                else imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, groupAvatarHolder)

                if (isAdmin) {
                    settingsBtn.visibility = View.VISIBLE
                    settingsBtn.setOnClickListener {
                        when (typeUserList) {
                            TypeUserList.ALL -> createPopMenu(resources.getStringArray(R.array.listOfActionFromAll).toList(), context, it, {}, {
                                banUserClickListener.invoke(item.idProfile, layoutPosition)
                            })
                            TypeUserList.BLOCKED -> createPopMenu(listOf(resources.getString(R.string.unblock)), context, it, {
                                deleteBanUserClickListener.invoke(item.idProfile, layoutPosition)
                            }, {})
                            TypeUserList.ADMINISTRATORS -> {
                            }
                        }
                    }
                }
            }
        }


        // передвать в качестве параметра дейсвие и значение имен в виде списка
        private fun createPopMenu(listName: List<String>, context: Context, view: View, action1: () -> Unit, action2: () -> Unit = {}) {
            val popupMenu = PopupMenu(context, view)
            listName.forEachIndexed { index, name ->
                popupMenu.menu.add(index, index, index, name)
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    0 -> {
                        action1()
                        true
                    }
                    1 -> {
                        action2()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}