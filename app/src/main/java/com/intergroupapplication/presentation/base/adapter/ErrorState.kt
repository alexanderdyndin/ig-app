package com.intergroupapplication.presentation.base.adapter

import androidx.recyclerview.widget.RecyclerView


class ErrorState<ENTITY, VIEWHOLDER : RecyclerView.ViewHolder>(private val adapter: BasePagingAdapter<ENTITY, VIEWHOLDER>)
    : PagingAdapterState {

    override fun addLoading() {
        adapter.changeState(LoadingState(adapter))
        adapter.notifyItemInserted(adapter.itemCount)
    }

    override fun removeError() {
        adapter.changeState(DataPageState(adapter))
        adapter.notifyItemRemoved(adapter.itemCount)
    }

    override fun getViewType(): InterGroupViewType {
        return InterGroupViewType.ERROR_VIEW_TYPE
    }
}