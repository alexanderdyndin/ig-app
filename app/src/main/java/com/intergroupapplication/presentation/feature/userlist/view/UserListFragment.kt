package com.intergroupapplication.presentation.feature.userlist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.*
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListAdapter
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListsAdapter
import com.intergroupapplication.presentation.feature.userlist.viewModel.UserListViewModel
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.view.*
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.fragment_user_list.navigationToolbar
import kotlinx.android.synthetic.main.fragment_user_list.pager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class UserListFragment : BaseFragment(), UserListView {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    @Named("all")
    lateinit var adapterAll: UserListAdapter

    @Inject
    @Named("blocked")
    lateinit var adapterBlocked: UserListAdapter

    @Inject
    @Named("administrators")
    lateinit var adapterAdministrators: UserListAdapter

    @Inject
    @Named("footerAll")
    lateinit var adapterFooterAll: PagingLoadingAdapter

    @Inject
    @Named("footerBlocked")
    lateinit var adapterFooterBlocked: PagingLoadingAdapter

    @Inject
    @Named("footerAdministrators")
    lateinit var adapterFooterAdministrators: PagingLoadingAdapter

    @Inject
    @Named("all")
    lateinit var adapterAllAdd: ConcatAdapter

    @Inject
    @Named("blocked")
    lateinit var adapterBlockedAdd: ConcatAdapter

    @Inject
    @Named("administrators")
    lateinit var adapterAdministratorAdd: ConcatAdapter

    private lateinit var viewModel: UserListViewModel
    private lateinit var groupId: String

    private var isAdmin = false
    private var currentScreen = 0

    override fun layoutRes() = R.layout.fragment_user_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[UserListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)!!
        isAdmin = requireArguments().getBoolean(GroupFragment.IS_ADMIN)
        showToast(groupId)
        navigationToolbar.toolbarTittle.text = getString(R.string.allUsers)
        navigationToolbar.toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        super.onViewCreated(view, savedInstanceState)

        initPager()

        followers_refresh.setOnRefreshListener {
            when(currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterBlocked.refresh()
                2 -> adapterAdministrators.refresh()
            }
        }
    }

    override fun getSnackBarCoordinator(): ViewGroup? = userListCoordinator

    private fun initPager() {

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> = mutableListOf()
        adapterList.add(adapterAllAdd)

        pager.apply {
            adapter = UserListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, followers_refresh)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        setAdapter(adapterAll, adapterFooterAll)
        if (isAdmin) {
            adapterList.add(adapterBlockedAdd)
            adapterList.add(adapterAdministratorAdd)

            slidingCategories.visibility = View.VISIBLE

            val tabTitles = arrayOf("Followers", "Blocked", "Administrators")
            TabLayoutMediator(slidingCategories, pager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()

            setAdapter(adapterBlocked, adapterFooterBlocked)
            setAdapter(adapterAdministrators, adapterFooterAdministrators)
            getAllData()
        } else {
            getFollowers()
        }
    }

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        followers_refresh.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        followers_refresh.isRefreshing = false
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun getFollowers() {
        compositeDisposable.clear()

        compositeDisposable.add(
                viewModel.getFollowers(groupId).subscribe(
                        {
                            adapterAll.submitData(lifecycle, it)
                        },
                        {
                            it.printStackTrace()
                        })
        )
    }

    @SuppressLint("CheckResult")
    private fun getAllData() {
        getFollowers()

        compositeDisposable.add(
                viewModel.getAdministrators(groupId).subscribe(
                        {
                            adapterAdministrators.submitData(lifecycle, it)
                        },
                        {
                            it.printStackTrace()
                        }
                )
        )

        compositeDisposable.add(
                viewModel.getBans(groupId).subscribe(
                        {
                            adapterAdministrators.submitData(lifecycle, it)
                        },
                        {
                            it.printStackTrace()
                        }
                )
        )
    }

}