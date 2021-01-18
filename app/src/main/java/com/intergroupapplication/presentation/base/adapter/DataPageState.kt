package com.intergroupapplication.presentation.base.adapter

import androidx.recyclerview.widget.RecyclerView


class DataPageState<ENTITY: Any, VIEWHOLDER : RecyclerView.ViewHolder>(private val adapter: BasePagingAdapter<ENTITY, VIEWHOLDER>)
    : PagingAdapterState {

    override fun addLoading() {
        adapter.changeState(LoadingState(adapter))
        adapter.notifyItemInserted(adapter.itemCount)
    }

    override fun addError() {
        adapter.changeState(ErrorState(adapter))
        adapter.notifyItemInserted(adapter.itemCount)
    }

    override fun getViewType(): InterGroupViewType {
        return InterGroupViewType.DATA_PAGE_STATE
    }
}