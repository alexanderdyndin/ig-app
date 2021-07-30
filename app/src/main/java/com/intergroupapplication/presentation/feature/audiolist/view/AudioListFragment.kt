package com.intergroupapplication.presentation.feature.audiolist.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.intergroupapplication.di.scope.PerFragment
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListAdapter
import com.intergroupapplication.presentation.feature.audiolist.adapter.AudioListsAdapter
import com.intergroupapplication.presentation.feature.audiolist.viewModel.AudioListViewModel
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class AudioListFragment(): BaseFragment() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    @Named("all")
    lateinit var adapter: AudioListAdapter

    @Inject
    @Named("all")
    lateinit var adapterCon: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var adapterFooter: PagingLoadingAdapter

    @Inject
    @Named("header")
    lateinit var adapterHeader: PagingLoadingAdapter

    private val viewBinding by viewBinding(FragmentAudiosListBinding::bind)

    private lateinit var viewModel: AudioListViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    private var currentScreen = 1

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
            adapter = AudioListsAdapter(adapterList)
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