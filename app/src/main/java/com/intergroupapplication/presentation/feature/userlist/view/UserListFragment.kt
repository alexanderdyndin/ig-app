package com.intergroupapplication.presentation.feature.userlist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.feature.userlist.presenter.UserListPresenter
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.view.*
import kotlinx.android.synthetic.main.fragment_user_list.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class UserListFragment constructor(private val pagingDelegate: PagingDelegateGroup)
    : BaseFragment(), UserListView, PagingViewGroup by pagingDelegate {

    constructor() : this(PagingDelegateGroup())

    lateinit var groupId: String

    @Inject
    @InjectPresenter
    lateinit var presenter: UserListPresenter

    @ProvidePresenter
    fun providePresenter(): UserListPresenter = presenter

    override fun layoutRes() = R.layout.fragment_user_list

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)!!
        showToast(groupId)
        presenter.groupFollows(groupId)
        navigationToolbar.toolbarTittle.text = getString(R.string.allUsers)
        navigationToolbar.toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        swipeLayout.setOnRefreshListener { presenter.refresh(groupId) }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getSnackBarCoordinator(): ViewGroup? = userListCoordinator


    override fun userListLoaded(users: PagedList<GroupEntity>) {

    }

    override fun userListAdmLoaded(users: PagedList<GroupEntity>) {

    }

    override fun showLoading(show: Boolean) {

    }
}