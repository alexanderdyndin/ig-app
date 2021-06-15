package com.intergroupapplication.presentation.feature.audiolist.view

import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.intergroupapplication.databinding.FragmentAudiosListBinding
import com.intergroupapplication.presentation.base.BaseMediaListFragment
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListAdapter
import com.intergroupapplication.presentation.feature.audiolist.adapter.MediaListsAdapter
import com.intergroupapplication.presentation.feature.audiolist.viewModel.AudioListViewModel
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class AudioListFragment: BaseMediaListFragment() {

    @Inject
    @Named("all")
    lateinit var adapter: AudioListAdapter

    private val viewBinding by viewBinding(FragmentAudiosListBinding::bind)

    private lateinit var viewModel: AudioListViewModel

    override fun layoutRes() = R.layout.fragment_audios_list

    override fun getSnackBarCoordinator(): CoordinatorLayout? = viewBinding.audioListCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this, modelFactory)[AudioListViewModel::class.java]
//        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (doubleBackToExitPressedOnce) {
//                    ExitActivity.exitApplication(requireContext())
//                    return
//                }
//                doubleBackToExitPressedOnce = true
//                Toast.makeText(requireContext(), getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
//                exitHandler = Handler(Looper.getMainLooper())
//                exitHandler?.postDelayed(exitFlag, MainActivity.EXIT_DELAY)
//            }
//        })
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

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> = mutableListOf()
        adapterList.add(adapterCon)
        setAdapter(adapter, adapterFooter)

        viewBinding.pager.apply {
            adapter = MediaListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, viewBinding.swipeLayout)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val tabTitles = arrayOf(resources.getString(R.string.music)/*, resources.getString(R.string.my_music)*/)

        TabLayoutMediator(viewBinding.slidingCategories, viewBinding.pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()


    }

    override fun onDestroyView() {
        super.onDestroyView()
        AudioGalleryView.mediaPlayerList.clear()
    }

    override fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        viewBinding.swipeLayout.setOnRefreshListener { adapter.refresh() }
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        viewBinding.swipeLayout.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
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