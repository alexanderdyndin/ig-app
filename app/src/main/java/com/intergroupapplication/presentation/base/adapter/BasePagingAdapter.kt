package com.intergroupapplication.presentation.base.adapter

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.domain.entity.GroupPostEntity

abstract class BasePagingAdapter<ENTITY, VIEWHOLDER :
RecyclerView.ViewHolder>(diffCallback: DiffUtil.ItemCallback<ENTITY>) :
        PagedListAdapter<ENTITY, VIEWHOLDER>(diffCallback) {
    abstract fun changeState(state: PagingAdapterState)
}