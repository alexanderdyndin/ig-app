package com.intergroupapplication.presentation.base.adapter

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.presentation.exstension.show
import kotlinx.android.synthetic.main.item_loading.view.*

class ViewHolderLoadingState(itemView: View, retry: () -> Unit) :
    RecyclerView.ViewHolder(itemView) {

    private val errorLayout: ConstraintLayout = itemView.error_layout
    private val loadingLayout: FrameLayout = itemView.loading_layout
    private val btnRetry: Button = itemView.buttonRetry

    init {
        btnRetry.setOnClickListener {
            retry.invoke()
        }
    }

    fun bindState(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            errorLayout.show()
        }
        loadingLayout.isVisible = loadState is LoadState.Loading
        errorLayout.isVisible = loadState !is LoadState.Loading
    }

}
