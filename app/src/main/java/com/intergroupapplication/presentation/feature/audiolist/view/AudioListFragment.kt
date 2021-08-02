package com.intergroupapplication.presentation.feature.audiolist.view

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentAudiosListBinding
import com.intergroupapplication.di.qualifier.All
import com.intergroupapplication.di.qualifier.Footer
import com.intergroupapplication.di.qualifier.Header
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListAdapter
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListsAdapter
import com.intergroupapplication.presentation.feature.audiolist.viewModel.AudioListViewModel
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class AudioListFragment : BaseFragment() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    @All
    lateinit var adapter: AudioListAdapter

    @Inject
    @All
    lateinit var adapterCon: ConcatAdapter

    @Inject
    @Footer
    lateinit var adapterFooter: PagingLoadingAdapter

    @Inject
    @Header
    lateinit var adapterHeader: PagingLoadingAdapter

    private val viewBinding by viewBinding(FragmentAudiosListBinding::bind)

    private lateinit var viewModel: AudioListViewModel

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    private var currentScreen = 1

    override fun layoutRes() = R.layout.fragment_audios_list

    override fun getSnackBarCoordinator(): CoordinatorLayout = viewBinding.audioListCoordinator

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, modelFactory)[AudioListViewModel::class.java]
        compositeDisposable.add(
            viewModel.getAudios()
                .subscribe {
                    adapter.submitData(lifecycle, it)
                }
        )
        super.onCreate(savedInstanceState)
    }

    override fun viewCreated() {
        viewBinding.navigationToolbar.toolbarTittle.setText(R.string.music)
        viewBinding.navigationToolbar.toolbarBackAction.setOnClickListener {
            findNavController().popBackStack()
        }

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> =
            mutableListOf()
        adapterList.add(adapterCon)
        setAdapter(adapter, adapterFooter)

        viewBinding.pager.apply {
            adapter = AudioListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, viewBinding.swipeLayout)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val tabTitles =
            arrayOf(resources.getString(R.string.music)/*, resources.getString(R.string.my_music)*/)

        TabLayoutMediator(viewBinding.slidingCategories, viewBinding.pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()


    }

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        viewBinding.swipeLayout.setOnRefreshListener { adapter.refresh() }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        viewBinding.swipeLayout.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState =
                                LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        viewBinding.swipeLayout.isRefreshing = false
                    }
                }
            }
        }
    }

}