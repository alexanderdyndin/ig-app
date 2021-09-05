package com.intergroupapplication.presentation.base.adapter

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.show

class ViewHolderLoadingState(itemView: View, retry: () -> Unit) :
    RecyclerView.ViewHolder(itemView) {

    private val errorLayout: ConstraintLayout by lazy(LazyThreadSafetyMode.NONE) {
        itemView.findViewById(R.id.error_layout)
    }
    private val loadingLayout: FrameLayout by lazy(LazyThreadSafetyMode.NONE) {
        itemView.findViewById(R.id.loading_layout)
    }
    private val btnRetry: Button by lazy(LazyThreadSafetyMode.NONE) {
        itemView.findViewById(R.id.buttonRetry)
    }

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
