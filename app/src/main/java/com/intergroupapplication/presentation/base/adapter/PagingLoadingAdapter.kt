package com.intergroupapplication.presentation.base.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.inflate

class PagingLoadingAdapter(private val retry: () -> Unit): LoadStateAdapter<ViewHolderLoadingState>() {

    override fun onBindViewHolder(holder: ViewHolderLoadingState, loadState: LoadState) {
        holder.bindState(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ViewHolderLoadingState {
        return ViewHolderLoadingState(parent.inflate(R.layout.item_loading), retry)
    }

}