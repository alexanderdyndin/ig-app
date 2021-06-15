package com.intergroupapplication.presentation.base

import android.os.Handler
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import javax.inject.Inject
import javax.inject.Named

abstract class BaseMediaListFragment:BaseFragment() {
    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    @Named("all")
    lateinit var adapterCon: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var adapterFooter: PagingLoadingAdapter

    @Inject
    @Named("header")
    lateinit var adapterHeader: PagingLoadingAdapter

    protected var exitHandler: Handler? = null

    protected var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    protected var currentScreen = 1

    abstract fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>)
}