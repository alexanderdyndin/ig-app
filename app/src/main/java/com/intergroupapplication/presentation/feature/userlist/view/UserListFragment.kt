package com.intergroupapplication.presentation.feature.userlist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.databinding.FragmentUserListBinding
import com.intergroupapplication.di.qualifier.*
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.addBlackListById.view.AddBlackListByIdFragment
import com.intergroupapplication.presentation.feature.group.view.GroupFragment
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListAdapter
import com.intergroupapplication.presentation.feature.userlist.adapter.UserListsAdapter
import com.intergroupapplication.presentation.feature.userlist.viewModel.UserListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserListFragment : BaseFragment(), DialogFragmentCallBack {

    private companion object {
        private const val BAN_REASON = "ban reason"
    }

    @Inject
    lateinit var modelFactory: ViewModelFactory

    @Inject
    @All
    lateinit var adapterAll: UserListAdapter

    @Inject
    @Blocked
    lateinit var adapterBlocked: UserListAdapter

    @Inject
    @Administrators
    lateinit var adapterAdministrators: UserListAdapter

    @Inject
    @FooterAll
    lateinit var adapterFooterAll: PagingLoadingAdapter

    @Inject
    @FooterBlocked
    lateinit var adapterFooterBlocked: PagingLoadingAdapter

    @Inject
    @FooterAdministrators
    lateinit var adapterFooterAdministrators: PagingLoadingAdapter

    @Inject
    @All
    lateinit var adapterAllAdd: ConcatAdapter

    @Inject
    @Blocked
    lateinit var adapterBlockedAdd: ConcatAdapter

    @Inject
    @Administrators
    lateinit var adapterAdministratorAdd: ConcatAdapter

    private val viewModel: UserListViewModel by viewModels { modelFactory }
    private var groupId: String? = null

    private var isAdmin = false
    private var currentScreen = 0
    private val viewBinding by viewBinding(FragmentUserListBinding::bind)

    @ExperimentalCoroutinesApi
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            getAllData(s.toString())
        }
    }

    private lateinit var followersRefresh: SwipeRefreshLayout
    private lateinit var searchEditText: EditText
    private lateinit var pager: ViewPager2
    private lateinit var btnAddId: TextView
    private lateinit var slidingCategories: TabLayout
    private lateinit var toolbarTittle: TextView
    private lateinit var toolbarBackAction: ImageButton

    override fun layoutRes() = R.layout.fragment_user_list

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)
        isAdmin = requireArguments().getBoolean(GroupFragment.IS_ADMIN)
        UserListAdapter.isAdmin = isAdmin

        initViewBinding()
        initPager()

        toolbarTittle.text = getString(R.string.allUsers)
        toolbarBackAction.setOnClickListener { findNavController().popBackStack() }

        super.onViewCreated(view, savedInstanceState)

        followersRefresh.setOnRefreshListener {
            when (currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterAdministrators.refresh()
                2 -> adapterBlocked.refresh()
            }
        }
    }

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.userListCoordinator

    @ExperimentalCoroutinesApi
    override fun onResume() {
        super.onResume()
        searchEditText.addTextChangedListener(textWatcher)
    }

    @ExperimentalCoroutinesApi
    override fun onPause() {
        super.onPause()
        searchEditText.removeTextChangedListener(textWatcher)
    }

    private fun initViewBinding() {
        followersRefresh = viewBinding.followersRefresh
        searchEditText = viewBinding.searchEditText
        pager = viewBinding.pager
        btnAddId = viewBinding.navigationToolbar.btnAddId
        slidingCategories = viewBinding.slidingCategories
        toolbarTittle = viewBinding.navigationToolbar.toolbarTittle
        toolbarBackAction = viewBinding.navigationToolbar.toolbarBackAction
    }

    @ExperimentalCoroutinesApi
    private fun initPager() {
        compositeDisposable.add(viewModel.getCurrentUserId().subscribe(
            {
                UserListAdapter.currentUserId = it.toString()
            },
            {
                errorHandler.handle(it)
            }
        ))

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> =
            mutableListOf()
        adapterList.add(adapterAllAdd)

        pager.apply {
            adapter = UserListsAdapter(adapterList)
            val handler = ViewPager2Circular(this, followersRefresh)
            handler.pageChanged = {
                currentScreen = it
                if (currentScreen == 2) btnAddId.visibility = View.VISIBLE
                else btnAddId.visibility = View.GONE
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        setAdapter(adapterAll, adapterFooterAll)
        if (isAdmin) {
            initActionAdmin()

            adapterList.add(adapterAdministratorAdd)
            adapterList.add(adapterBlockedAdd)

            slidingCategories.visibility = View.VISIBLE

            val tabTitles = arrayOf(
                getString(R.string.followers),
                getString(R.string.administrators),
                getString(R.string.blocked)
            )
            TabLayoutMediator(slidingCategories, pager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()

            setAdapter(adapterAdministrators, adapterFooterAdministrators)
            setAdapter(adapterBlocked, adapterFooterBlocked)
            getAllData()

            btnAddId.setOnClickListener {
                val addBlackListByIdFragment = AddBlackListByIdFragment
                    .newInstance(GROUP_ID to groupId)
                addBlackListByIdFragment.callBack = this
                addBlackListByIdFragment.show(childFragmentManager, "TAG")
                addBlackListByIdFragment.lifecycle
            }
        } else {
            getFollowers()
        }
    }

    private fun initActionAdmin() {
        UserListAdapter.banUserClickListener = { groupUserEntity, position ->
            groupId?.let { id ->
                compositeDisposable.add(
                    viewModel.setUserBans(groupUserEntity.idProfile, BAN_REASON, id)
                        .subscribe({
                            groupUserEntity.isBlocked = true
                            adapterAll.notifyItemChanged(position)
                            adapterBlocked.refresh()
                        }, {
                            errorHandler.handle(it)
                        })
                )
            }
        }

        UserListAdapter.deleteBanUserClickListener = { groupUserEntity, position ->
            compositeDisposable.add(
                viewModel.deleteUserFromBansGroup(groupUserEntity.banId)
                    .subscribe({
                        groupUserEntity.isBlocked = false
                        adapterBlocked.notifyItemChanged(position)
                        adapterAll.refresh()
                    }, {
                        errorHandler.handle(it)
                    })
            )
        }

        UserListAdapter.assignToAdminsClickListener = { groupUserEntity, position ->
            compositeDisposable.add(
                viewModel.assignToAdmins(groupUserEntity.subscriptionId)
                    .subscribe({
                        groupUserEntity.isAdministrator = true
                        adapterAll.notifyItemChanged(position)
                        adapterAdministrators.refresh()
                    }, {
                        errorHandler.handle(it)
                    })
            )
        }

        UserListAdapter.demoteFromAdminsClickListener = { groupUserEntity, position ->
            compositeDisposable.add(
                viewModel.demoteFromAdmins(groupUserEntity.subscriptionId)
                    .subscribe({
                        groupUserEntity.isAdministrator = false
                        adapterAdministrators.notifyItemChanged(position)
                        adapterAll.refresh()
                    }, {
                        errorHandler.handle(it)
                    })
            )
        }

        UserListAdapter.banAdminFromAdminsClickListener = { groupUserEntity, position ->
            groupId?.let { id ->
                compositeDisposable.add(
                    viewModel.setUserBans(groupUserEntity.idProfile, BAN_REASON, id)
                        .subscribe({
                            groupUserEntity.isBlocked = true
                            groupUserEntity.isAdministrator = false
                            adapterAdministrators.notifyItemChanged(position)
                            adapterBlocked.refresh()
                            adapterAll.refresh()
                        }, {
                            errorHandler.handle(it)
                        })
                )
            }
        }
    }

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        followersRefresh.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState =
                                LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        followersRefresh.isRefreshing = false
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("CheckResult")
    private fun getFollowers(searchFilter: String = "") {
        compositeDisposable.clear()
        groupId?.let { id ->
            compositeDisposable.add(
                viewModel.getFollowers(id, searchFilter).subscribe(
                    {
                        adapterAll.submitData(lifecycle, it)
                    },
                    {
                        errorHandler.handle(it)
                    })
            )
        }
    }

    @ExperimentalCoroutinesApi
    @SuppressLint("CheckResult")
    private fun getAllData(searchFilter: String = "") {
        getFollowers(searchFilter)
        groupId?.let { id ->
            compositeDisposable.add(
                viewModel.getBans(id, searchFilter).subscribe(
                    {
                        adapterBlocked.submitData(lifecycle, it)
                    },
                    {
                        errorHandler.handle(it)
                    }
                )
            )
            compositeDisposable.add(
                viewModel.getAdministrators(id, searchFilter).subscribe(
                    {
                        adapterAdministrators.submitData(lifecycle, it)
                    },
                    {
                        errorHandler.handle(it)
                    }
                )
            )
        }
    }

    override fun updateList() {
        adapterBlocked.refresh()
        adapterAll.refresh()
        adapterAdministrators.refresh()
    }

}