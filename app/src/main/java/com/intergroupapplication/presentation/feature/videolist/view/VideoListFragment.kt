package com.intergroupapplication.presentation.feature.videolist.view

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentVideosListBinding
import com.intergroupapplication.presentation.base.BaseMediaListFragment
import com.intergroupapplication.presentation.feature.audiolist.adapter.MediaListsAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.videolist.adapter.VideoListAdapter
import com.intergroupapplication.presentation.feature.videolist.viewmodel.VideoListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class VideoListFragment: BaseMediaListFragment(){

    @Inject
    @Named("all")
    lateinit var adapter: VideoListAdapter

    private val videoListBinding by viewBinding(FragmentVideosListBinding::bind)

    private lateinit var viewModel: VideoListViewModel

    override fun layoutRes() = R.layout.fragment_videos_list

    override fun getSnackBarCoordinator() = videoListBinding.videoListCoordinator


    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, modelFactory)[VideoListViewModel::class.java]
        compositeDisposable.add(
            viewModel.getVideos()
                .subscribe {
                    adapter.submitData(lifecycle, it)
                }
        )
        super.onCreate(savedInstanceState)
    }

    override fun viewCreated() {
        videoListBinding.navigationToolbar.toolbarTittle.setText(R.string.music)
        videoListBinding.navigationToolbar.toolbarBackAction.setOnClickListener {
            findNavController().popBackStack()
        }

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> = mutableListOf()
        adapterList.add(adapterCon)
        setAdapter(adapter, adapterFooter)

        videoListBinding.pager.apply {
            adapter = MediaListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, videoListBinding.swipeLayout)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val tabTitles = arrayOf(resources.getString(R.string.music)/*, resources.getString(R.string.my_music)*/)

        TabLayoutMediator(videoListBinding.slidingCategories, videoListBinding.pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        videoListBinding.swipeLayout.setOnRefreshListener { adapter.refresh() }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        videoListBinding.swipeLayout.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        videoListBinding.swipeLayout.isRefreshing = false
                    }
                }
            }
        }
    }
}