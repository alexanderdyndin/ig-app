package com.intergroupapplication.presentation.feature.userlist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.intergroupapplication.presentation.feature.userlist.addBlackListById.AddBlackListByIdFragment
import com.intergroupapplication.presentation.feature.userlist.viewModel.UserListViewModel
import kotlinx.android.synthetic.main.blacklist_toolbar_layout.*
import kotlinx.android.synthetic.main.fragment_user_list.*
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

    @Inject
    lateinit var addBlackListDialog: AddBlackListByIdFragment

    private lateinit var viewModel: UserListViewModel
    private lateinit var groupId: String

    private var isAdmin = false
    private var currentScreen = 0
    private val BAN_REASON = "ban reason"

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            getAllData(s.toString())
        }
    }

    override fun layoutRes() = R.layout.fragment_user_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[UserListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)!!
        isAdmin = requireArguments().getBoolean(GroupFragment.IS_ADMIN)

        UserListAdapter.isAdmin = isAdmin

        toolbarTittle.text = getString(R.string.allUsers)
        toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        btnAddId.setOnClickListener {
            val args = Bundle()
            args.putString(GROUP_ID, groupId)
            addBlackListDialog.arguments = args
            addBlackListDialog.show(childFragmentManager, "TAG")
        }

        super.onViewCreated(view, savedInstanceState)

        initPager()

        followers_refresh.setOnRefreshListener {
            when(currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterAdministrators.refresh()
                2 -> adapterBlocked.refresh()
            }
        }
    }

    override fun getSnackBarCoordinator(): ViewGroup? = userListCoordinator

    override fun onResume() {
        super.onResume()
        searchEditText.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        searchEditText.removeTextChangedListener(textWatcher)
    }

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
            initActionAdmin()

            adapterList.add(adapterAdministratorAdd)
            adapterList.add(adapterBlockedAdd)

            slidingCategories.visibility = View.VISIBLE

            val tabTitles = arrayOf(getString(R.string.followers), getString(R.string.administrators), getString(R.string.blocked))
            TabLayoutMediator(slidingCategories, pager) { tab, position ->
                tab.text = tabTitles[position]
            }.attach()

            setAdapter(adapterAdministrators, adapterFooterAdministrators)
            setAdapter(adapterBlocked, adapterFooterBlocked)
            getAllData()
        } else {
            getFollowers()
        }
    }

    private fun initActionAdmin() {
        UserListAdapter.banUserClickListener = { groupUserEntity, position ->
            compositeDisposable.add(
                    viewModel.setUserBans(groupUserEntity.idProfile, BAN_REASON, groupId)
                            .subscribe({
                                groupUserEntity.isBlocked = true
                                adapterAll.notifyItemChanged(position)
                                adapterBlocked.refresh()
                            },{
                                errorHandler.handle(it)
                            })
            )
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
    private fun getFollowers(searchFilter: String = "") {
        compositeDisposable.clear()

        compositeDisposable.add(
                viewModel.getFollowers(groupId, searchFilter).subscribe(
                        {
                            adapterAll.submitData(lifecycle, it)
                        },
                        {
                            it.printStackTrace()
                        })
        )
    }

    @SuppressLint("CheckResult")
    private fun getAllData(searchFilter: String = "") {
        getFollowers(searchFilter)

        compositeDisposable.add(
                viewModel.getBans(groupId, searchFilter).subscribe(
                        {
                            adapterBlocked.submitData(lifecycle, it)
                        },
                        {
                            it.printStackTrace()
                        }
                )
        )

        compositeDisposable.add(
                viewModel.getAdministrators(groupId, searchFilter).subscribe(
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