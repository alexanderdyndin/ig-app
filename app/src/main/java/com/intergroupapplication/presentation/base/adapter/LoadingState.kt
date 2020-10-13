package com.intergroupapplication.presentation.base.adapter

import androidx.recyclerview.widget.RecyclerView


class LoadingState<ENTITY, VIEWHOLDER : RecyclerView.ViewHolder>(private val adapter: BasePagingAdapter<ENTITY, VIEWHOLDER>)
    : PagingAdapterState {

    override fun removeLoading() {
        adapter.changeState(DataPageState(adapter))
        adapter.notifyItemRemoved(adapter.itemCount)
    }

    override fun addError() {
        adapter.changeState(ErrorState(adapter))
        adapter.notifyItemInserted(adapter.itemCount)
    }

    override fun getViewType(): InterGroupViewType {
        return InterGroupViewType.LOADING_VIEW_TYPE
    }
}