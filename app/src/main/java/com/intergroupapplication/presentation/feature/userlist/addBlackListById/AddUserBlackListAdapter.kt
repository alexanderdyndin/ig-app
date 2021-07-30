package com.intergroupapplication.presentation.feature.userlist.addBlackListById

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.databinding.ItemAddUserBlackListBinding
import by.kirich1409.viewbindingdelegate.viewBinding

class AddUserBlackListAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : RecyclerView.Adapter<AddUserBlackListAdapter.AddUserBlackListViewHolder>() {
    companion object {
        var selectItem: (userItem: AddBlackListUserItem, position: Int) -> Unit = { _, _ -> }
    }

    private val data = mutableListOf<AddBlackListUserItem>()

    override fun onBindViewHolder(holder: AddUserBlackListViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddUserBlackListViewHolder {
        return AddUserBlackListViewHolder(parent.inflate(R.layout.item_add_user_black_list))
    }

    inner class AddUserBlackListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val viewBinding by viewBinding(ItemAddUserBlackListBinding::bind)

        @SuppressLint("SetTextI18n")
        fun bind(userEntity: AddBlackListUserItem) {
            viewBinding.run {
                nameTxt.text = userEntity.fullName
                profileIdTxt.text = "ID: ${userEntity.idProfile}"
                if (userEntity.avatar.isNotEmpty()) imageLoadingDelegate.loadImageFromUrl(userEntity.avatar, userAvatarHolder)
                else imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, userAvatarHolder)

                if (userEntity.isSelected) {
                    nameUsersAddBlackList.background = ContextCompat.getDrawable(nameUsersAddBlackList.context, R.drawable.bg_dark_element_radius_2dp)
                    icCheckUser.setBackgroundResource(R.drawable.ic_check_red)
                } else {
                    nameUsersAddBlackList.background = ContextCompat.getDrawable(nameUsersAddBlackList.context, R.drawable.bg_greyelement_radius_2dp)
                    icCheckUser.setBackgroundResource(R.drawable.ic_check_black)
                }

                root.setOnClickListener {
                    selectItem(userEntity, layoutPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: List<AddBlackListUserItem>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}