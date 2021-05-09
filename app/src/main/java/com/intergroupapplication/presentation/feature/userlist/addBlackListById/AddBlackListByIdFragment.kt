package com.intergroupapplication.presentation.feature.userlist.addBlackListById

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment.Companion.GROUP_ID
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dialog_add_black_list_by_id.*
import javax.inject.Inject
import javax.inject.Named

class AddBlackListByIdFragment @Inject constructor(
        private val modelFactory: ViewModelProvider.Factory
) : DialogFragment(R.layout.fragment_dialog_add_black_list_by_id) {

    @Inject
    lateinit var adapter: AddUserBlackListAdapter

    @Inject
    @Named("headerBanList")
    lateinit var headerAdapter: PagingLoadingAdapter

    @Inject
    @Named("footerBanList")
    lateinit var footerAdapter: PagingLoadingAdapter

    @Inject
    @Named("blackListDialog")
    lateinit var concatAdapter: ConcatAdapter

    lateinit var viewModel: AddBlackListByIdViewModel
    private var groupId = ""
    private var compositeDisposable = CompositeDisposable()
    private var lastPosition = 0
    private var lastSelectedUser: AddBlackListUserItem? = null

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val str = s.toString()
            addBlackListBtn.isEnabled = str.isNotEmpty()
            getData(str)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity(), modelFactory)[AddBlackListByIdViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments?.getString(GROUP_ID, "") ?: ""

        listUsers.run {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            adapter = concatAdapter
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        getData()
        initAdapterItemClick()
        addBlackListBtn.setOnClickListener {
            lastSelectedUser?.run {
                banUser(idProfile)
            } ?: banUser(inputBlackListAddId.text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        inputBlackListAddId.addTextChangedListener(textWatcher)
    }

    override fun onPause() {
        super.onPause()
        inputBlackListAddId.removeTextChangedListener(textWatcher)
        compositeDisposable.clear()
    }

    private fun getData(searchFilter: String = "") {
        compositeDisposable.clear()
        compositeDisposable.add(
                viewModel.getUsers(groupId, searchFilter)
                        .subscribe(
                                {
                                    adapter.addLoadStateListener {
                                        if (adapter.itemCount < 1) {
                                            listUsers.visibility = View.GONE
                                        } else {
                                            listUsers.visibility = View.VISIBLE
                                        }
                                    }
                                    adapter.submitData(lifecycle, it)
                                },
                                {
                                    it.printStackTrace()
                                }
                        ))
    }

    private fun initAdapterItemClick() {
        AddUserBlackListAdapter.selectItem = { addBlackListUserItem: AddBlackListUserItem, position: Int ->
            addBlackListUserItem.isSelected = !addBlackListUserItem.isSelected
            addBlackListBtn.isEnabled = addBlackListUserItem.isSelected
            if (addBlackListUserItem.isSelected) {
                lastSelectedUser?.isSelected = false
                adapter.notifyItemChanged(lastPosition)
                lastSelectedUser = addBlackListUserItem
                lastPosition = position
            } else {
                lastSelectedUser = null
                addBlackListUserItem.isSelected = false
            }
            adapter.notifyItemChanged(position)
        }
    }

    @SuppressLint("CheckResult")
    private fun banUser(userId: String) {
        viewModel.banUserInGroup(userId, "ban", groupId)
                .subscribe(
                        {
                            textNoFoundId.visibility = View.INVISIBLE
                            Toast.makeText(requireContext(), "ID: $userId", Toast.LENGTH_SHORT).show()
                        },
                        {
                            textNoFoundId.visibility = View.VISIBLE
                            it.printStackTrace()
                        }
                )
    }

}