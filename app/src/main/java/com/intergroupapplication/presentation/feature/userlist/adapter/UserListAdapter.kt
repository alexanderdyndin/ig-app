package com.intergroupapplication.presentation.feature.userlist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.ItemSubscribersInListBinding
import com.intergroupapplication.domain.entity.GroupUserEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate

enum class TypeUserList {
    ALL, BLOCKED, ADMINISTRATORS
}

class UserListAdapter(
    private val imageLoadingDelegate: ImageLoadingDelegate,
    private val typeUserList: TypeUserList
) : PagingDataAdapter<GroupUserEntity, UserListAdapter.UserListViewHolder>(diffUtil) {

    companion object {
        var banUserClickListener: (groupUserEntity: GroupUserEntity, position: Int) -> Unit =
            { _, _ -> }
        var deleteBanUserClickListener: (groupUserEntity: GroupUserEntity, position: Int) -> Unit =
            { _, _ -> }
        var assignToAdminsClickListener: (groupUserEntity: GroupUserEntity, position: Int) -> Unit =
            { _, _ -> }
        var demoteFromAdminsClickListener: (groupUserEntity: GroupUserEntity, position: Int) -> Unit =
            { _, _ -> }
        var banAdminFromAdminsClickListener: (groupUserEntity: GroupUserEntity, position: Int) -> Unit =
            { _, _ -> }
        var isAdmin = false
        var currentUserId = ""
        private val diffUtil = object : DiffUtil.ItemCallback<GroupUserEntity>() {
            override fun areItemsTheSame(
                oldItem: GroupUserEntity,
                newItem: GroupUserEntity
            ): Boolean {
                return oldItem.idProfile == newItem.idProfile
            }

            override fun areContentsTheSame(
                oldItem: GroupUserEntity,
                newItem: GroupUserEntity
            ): Boolean {
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

        private val viewBinding by viewBinding(ItemSubscribersInListBinding::bind)

        @SuppressLint("SetTextI18n")
        fun bind(item: GroupUserEntity) {
            with(viewBinding) {
                itemGroupListHeader.text = item.firstName + " " + item.surName
                idUser.text = "ID: ${item.idProfile}"
                itemGroupPosts.text = item.postsCount.toString()
                itemGroupComments.text = item.commentsCount.toString()
                itemGroupDislike.text = item.dislikeCount.toString()
                itemGroupLike.text = item.dislikeCount.toString()

                if (item.avatar.isNotEmpty()) imageLoadingDelegate.loadImageFromUrl(
                    item.avatar,
                    groupAvatarHolder
                )
                else imageLoadingDelegate.loadImageFromResources(
                    R.drawable.variant_10,
                    groupAvatarHolder
                )

                if (item.isAdministrator) {
                    bigAngle.visibility = View.VISIBLE
                    bigAngle.background =
                        ContextCompat.getDrawable(bigAngle.context, R.drawable.bg_angle_admin)
                } else {
                    bigAngle.visibility = View.GONE
                }

                if (item.isBlocked) {
                    bigAngle.visibility = View.VISIBLE
                    bigAngle.background =
                        ContextCompat.getDrawable(bigAngle.context, R.drawable.bg_angle_blocked)
                }

                if (isAdmin) {
                    settingsBtn.visibility = View.VISIBLE
                    when (typeUserList) {
                        TypeUserList.ALL -> {
                            if (!item.isAdministrator) {
                                settingsBtn.setOnClickListener {
                                    createPopMenu(it.resources.getStringArray(R.array.listOfActionFromAll)
                                        .toList(), it.context, it,
                                        {
                                            assignToAdminsClickListener.invoke(item, layoutPosition)
                                        },
                                        {
                                            banUserClickListener.invoke(item, layoutPosition)
                                        })
                                }
                            } else if (!item.isOwner && item.idProfile != currentUserId) {
                                settingsBtn.setOnClickListener {
                                    createPopMenu(listOf(it.resources.getString(R.string.toBlackList)),
                                        it.context,
                                        it,
                                        {
                                            banUserClickListener.invoke(item, layoutPosition)
                                        },
                                        {})
                                }
                            } else settingsBtn.visibility = View.GONE
                        }
                        TypeUserList.BLOCKED -> {
                            settingsBtn.setOnClickListener {
                                createPopMenu(
                                    listOf(it.resources.getString(R.string.unblock)),
                                    it.context,
                                    it,
                                    {
                                        deleteBanUserClickListener.invoke(item, layoutPosition)
                                    },
                                    {})
                            }
                        }
                        TypeUserList.ADMINISTRATORS -> {
                            if (!item.isOwner && item.idProfile != currentUserId) {
                                settingsBtn.setOnClickListener {
                                    createPopMenu(it.resources.getStringArray(R.array.listOfActionFromAdministrators)
                                        .toList(), it.context, it,
                                        {
                                            demoteFromAdminsClickListener.invoke(
                                                item,
                                                layoutPosition
                                            )
                                        },
                                        {
                                            banAdminFromAdminsClickListener.invoke(
                                                item,
                                                layoutPosition
                                            )
                                        })
                                }
                            } else settingsBtn.visibility = View.GONE
                        }
                    }
                }
            }
        }

        // передвать в качестве параметра дейсвие и значение имен в виде списка
        private fun createPopMenu(
            listName: List<String>,
            context: Context,
            view: View,
            action1: () -> Unit,
            action2: () -> Unit = {}
        ) {
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
