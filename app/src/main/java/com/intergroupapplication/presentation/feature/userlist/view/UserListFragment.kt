package com.intergroupapplication.presentation.feature.userlist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingViewGroup
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.feature.userlist.viewModel.UserListViewModel
import kotlinx.android.synthetic.main.creategroup_toolbar_layout.view.*
import kotlinx.android.synthetic.main.fragment_user_list.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject

class UserListFragment(): BaseFragment() {

    lateinit var groupId: String

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UserListViewModel

    override fun layoutRes() = R.layout.fragment_user_list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[UserListViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        groupId = requireArguments().getString(GROUP_ID)!!
        showToast(groupId)
        navigationToolbar.toolbarTittle.text = getString(R.string.allUsers)
        navigationToolbar.toolbarBackAction.setOnClickListener { findNavController().popBackStack() }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getSnackBarCoordinator(): ViewGroup? = userListCoordinator


}