package com.intergroupapplication.presentation.feature.userlist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
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

    lateinit var groupId: String

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

    override fun layoutRes() = R.layout.fragment_user_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[UserListViewModel::class.java]
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)!!
        showToast(groupId)
        navigationToolbar.toolbarTittle.text = getString(R.string.allUsers)
        navigationToolbar.toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        super.onViewCreated(view, savedInstanceState)

        val adapterList: MutableList<RecyclerView.Adapter<RecyclerView.ViewHolder>> = mutableListOf()
        adapterList.add(adapterAllAdd)
        adapterList.add(adapterBlockedAdd)
        adapterList.add(adapterAdministratorAdd)
        pager.apply {
            adapter = UserListsAdapter(adapterList)
        }

        viewModel.getUsers(groupId).subscribe(
                {
                    adapterAll.submitData(lifecycle, it)
                },
                {
                    it.printStackTrace()
                })

        val tabTitles = arrayOf("Followers", "Blocked", "Administrators")
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        setAdapter(adapterAll, adapterFooterAll)
        setAdapter(adapterBlocked, adapterFooterBlocked)
        setAdapter(adapterAdministrators, adapterFooterAdministrators)
    }

    override fun getSnackBarCoordinator(): ViewGroup? = userListCoordinator

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    is LoadState.Loading -> {
                    }
                    is LoadState.Error -> {
                        if (adapter.itemCount == 0) {
                            footer.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                    }
                }
            }
        }
    }
}