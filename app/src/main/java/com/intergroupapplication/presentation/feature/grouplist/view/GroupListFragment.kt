package com.intergroupapplication.presentation.feature.grouplist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentGroupListBinding
import com.intergroupapplication.di.qualifier.*
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.GroupAlreadyFollowingException
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListsAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GroupListFragment : BaseFragment() {

    private val viewBinding by viewBinding(FragmentGroupListBinding::bind)

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var modelFactory: ViewModelFactory

    private val viewModel: GroupListViewModel by viewModels { modelFactory }

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    private val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    var groupId: String? = null

    var currentScreen = 0

    @Inject
    @All
    lateinit var adapterAll: GroupListAdapter

    @Inject
    @Subscribed
    lateinit var adapterSubscribed: GroupListAdapter

    @Inject
    @Owned
    lateinit var adapterOwned: GroupListAdapter

    @Inject
    @FooterAll
    lateinit var adapterFooterAll: PagingLoadingAdapter

    @Inject
    @FooterSub
    lateinit var adapterFooterSub: PagingLoadingAdapter

    @Inject
    @FooterAdm
    lateinit var adapterFooterAdm: PagingLoadingAdapter

    @Inject
    lateinit var viewPagerAdapter: GroupListsAdapter

    override fun layoutRes() = R.layout.fragment_group_list

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.groupListCoordinator

    private lateinit var pager: ViewPager2
    private lateinit var slidingCategories: TabLayout
    private lateinit var swipeGroups: SwipeRefreshLayout
    private lateinit var createGroup: Button
    private lateinit var activityMainBtnFilter: ImageButton
    private lateinit var activityMainSearchInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchGroups()
        prepareAdapter()
        activity?.onBackPressedDispatcher?.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        ExitActivity.exitApplication(requireContext())
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitHandler = Handler(Looper.getMainLooper())
                    exitHandler?.postDelayed(exitFlag, MainActivity.EXIT_DELAY)
                }
            })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager = viewBinding.pager
        slidingCategories = viewBinding.slidingCategories
        swipeGroups = viewBinding.swipeGroups
        createGroup = viewBinding.navigationToolbar.createGroup
        activityMainBtnFilter = viewBinding.activityMainBtnFilter
        activityMainSearchInput = viewBinding.activityMainSearchInput

        pager.apply {
            adapter = viewPagerAdapter
            val handler = ViewPager2Circular(this, swipeGroups)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        val tabTitles = arrayOf(
            resources.getString(R.string.allGroups),
            resources.getString(R.string.subGroups),
            resources.getString(R.string.admGroups)
        )
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        activityMainBtnFilter.setOnClickListener {
            //todo
        }
        createGroup.visibility = View.VISIBLE
        createGroup.setOnClickListener { openCreateGroup() }

        swipeGroups.setOnRefreshListener {
            when (currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterSubscribed.refresh()
                2 -> adapterOwned.refresh()
            }
        }
        setAdapter(adapterAll, adapterFooterAll)
        setAdapter(adapterSubscribed, adapterFooterSub)
        setAdapter(adapterOwned, adapterFooterAdm)

        viewBinding.navigationToolbar.toolbarTittle.setText(R.string.groups)
        viewBinding.navigationToolbar.toolbarMenu.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.drawer.openDrawer()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun prepareAdapter() {
        with(GroupListAdapter) {
            userID = userSession.user?.id
            groupClickListener = { groupId ->
                val data = bundleOf(GROUP_ID to groupId)
                findNavController().navigate(R.id.action_groupListFragment_to_groupFragment, data)
            }
            subscribeClickListener = { group, pos ->
                if (!group.isSubscribing) {
                    compositeDisposable.add(viewModel.subscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            group.isSubscribing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .doFinally {
                            group.isSubscribing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .subscribe({
                            group.isFollowing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                }
                            }
                        }, { exception ->
                            if (exception is CompositeException) {
                                exception.exceptions.forEach { ex ->
                                    (ex as? FieldException)?.let {
                                        if (it.field == "group") {
                                            group.isFollowing = !group.isFollowing
                                        }
                                    }
                                }
                            }
                            errorHandler.handle(exception)
                        })
                    )
                }
            }
            unsubscribeClickListener = { group, pos ->
                if (!group.isSubscribing) {
                    compositeDisposable.add(viewModel.unsubscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            group.isSubscribing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .doFinally {
                            group.isSubscribing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .subscribe({
                            group.isFollowing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                }
                            }
                        }, {
                            if (it is GroupAlreadyFollowingException) {
                                group.isFollowing = !group.isFollowing
                                when (currentScreen) {
                                    0 -> {
                                        adapterAll.notifyDataSetChanged()
                                    }
                                    1 -> {
                                        adapterSubscribed.notifyDataSetChanged()
                                    }
                                }
                            }
                            errorHandler.handle(it)
                        })
                    )
                }
            }
        }
    }

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        lifecycleScope.launchWhenResumed {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        swipeGroups.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState =
                                LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        swipeGroups.isRefreshing = false
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun fetchGroups(query: String = "") {
        compositeDisposable.clear()
        compositeDisposable.add(
            viewModel.fetchGroups(query)
                .subscribe {
                    adapterAll.submitData(lifecycle, it)
                }
        )
        compositeDisposable.add(
            viewModel.fetchSubGroups(query)
                .subscribe {
                    adapterSubscribed.submitData(lifecycle, it)
                }
        )
        compositeDisposable.add(
            viewModel.fetchAdmGroups(query)
                .subscribe {
                    adapterOwned.submitData(lifecycle, it)
                }
        )
    }

    override fun onResume() {
        super.onResume()
        //activityMainSearchInput.addTextChangedListener(textWatcher)
        activityMainSearchInput.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap {
                val query = it.toString()
                viewModel.fetchGroups(query)
            }
            .subscribe {
                adapterAll.submitData(lifecycle, it)
            }
            .also(compositeDisposable::add)
        activityMainSearchInput.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap {
                val query = it.toString()
                viewModel.fetchSubGroups(query)
            }
            .subscribe {
                adapterSubscribed.submitData(lifecycle, it)
            }
            .also(compositeDisposable::add)
        activityMainSearchInput.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .toFlowable(BackpressureStrategy.LATEST)
            .observeOn(AndroidSchedulers.mainThread())
            .switchMap {
                val query = it.toString()
                viewModel.fetchAdmGroups(query)
            }
            .subscribe {
                adapterOwned.submitData(lifecycle, it)
            }
            .also(compositeDisposable::add)
    }

    private fun openCreateGroup() {
        findNavController().navigate(R.id.action_groupListFragment_to_createGroupFragment)
    }
}
