package com.intergroupapplication.presentation.feature.mainActivity.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.ItemDrawerMenuBinding
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.feature.mainActivity.other.NavigationEntity

class NavigationAdapter: RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder>() {

    var items: List<NavigationEntity> = emptyList()
    set(value) {
        val diff = DiffUtil.calculateDiff(NavigationDiff(field, value))
        field = value
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavigationViewHolder(parent.inflate(R.layout.item_drawer_menu))

    override fun onBindViewHolder(holder: NavigationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class NavigationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val viewBinging by viewBinding(ItemDrawerMenuBinding::bind)

        private val name = viewBinging.nameTxt
        private val icon = viewBinging.iconImg
        private val container = viewBinging.container

        fun bind(item: NavigationEntity) {
            itemView.setOnClickListener {
                item.action.invoke()
                if (item.checked == false) {
                    item.checked = true
                    items.forEach {
                        if (it != item && it.checked == true) {
                            it.checked = false
                            notifyDataSetChanged()
                        }
                    }
                }
            }
            name.setText(item.name)
            icon.setImageResource(item.icon)
            if (item.checked == true) {
                container.setBackgroundResource(R.drawable.bg_menu_item_act)
            } else {
                container.setBackgroundResource(R.drawable.bg_menu_item)
            }
        }
    }

    internal class NavigationDiff(
            private val oldList: List<NavigationEntity>,
            private val newList: List<NavigationEntity>
    ): DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition].name == newList[newItemPosition].name

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                oldList[oldItemPosition] == newList[newItemPosition]

    }

}