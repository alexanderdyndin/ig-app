package com.intergroupapplication.presentation.feature.addBlackListById.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AddBlackListUserModel
import com.intergroupapplication.databinding.DialogAddBlackListByIdBinding
import com.intergroupapplication.presentation.base.BaseFragment.Companion.GROUP_ID
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.addBlackListById.adapter.AddUserBlackListAdapter
import com.intergroupapplication.presentation.feature.addBlackListById.viewmodel.AddBlackListByIdViewModel
import com.intergroupapplication.presentation.feature.userlist.view.DialogFragmentCallBack
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class AddBlackListByIdFragment : DialogFragment(R.layout.dialog_add_black_list_by_id) {

    companion object {
        private const val BAN_REASON = "ban reason"
        fun newInstance(vararg data: Pair<String, Any?>): AddBlackListByIdFragment {
            val fragment = AddBlackListByIdFragment()
            fragment.arguments = bundleOf(*data)
            return fragment
        }
    }

    @Inject
    lateinit var userAdapter: AddUserBlackListAdapter

    @Inject
    lateinit var modelFactory: ViewModelFactory

    var callBack: DialogFragmentCallBack? = null
    private val viewModel: AddBlackListByIdViewModel by viewModels { modelFactory }
    private var groupId = ""
    private var compositeDisposable = CompositeDisposable()
    private var lastPosition = 0
    private var lastSelectedUser: AddBlackListUserModel? = null
    private val viewBinding by viewBinding(DialogAddBlackListByIdBinding::bind)

    private lateinit var addBlackListBtn: TextView
    private lateinit var listUsers: RecyclerView
    private lateinit var inputBlackListAddId: EditText
    private lateinit var btnClose: ImageButton
    private lateinit var textNoFoundId: TextView


    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val str = s.toString()
            addBlackListBtn.changeStateAddBlackList(str.isNotEmpty())
            getData(str)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.BlockByIdDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments?.getString(GROUP_ID, "") ?: ""

        initViewBinding()

        listUsers.run {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            adapter = userAdapter
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
        callBack?.updateList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callBack = null
    }

    private fun initViewBinding() {
        addBlackListBtn = viewBinding.addBlackListBtn
        listUsers = viewBinding.listUsers
        inputBlackListAddId = viewBinding.inputBlackListAddId
        btnClose = viewBinding.btnClose
        textNoFoundId = viewBinding.textNoFoundId
    }

    private fun getData(searchFilter: String = "") {
        compositeDisposable.clear()
        compositeDisposable.add(
            viewModel.getUsers(groupId, searchFilter)
                .subscribe(
                    {
                        if (it.isEmpty()) {
                            listUsers.visibility = View.GONE
                        } else {
                            listUsers.visibility = View.VISIBLE
                        }
                        userAdapter.setData(it)
                    },
                    {
                        it.printStackTrace()
                    }
                ))
    }

    private fun initAdapterItemClick() {
        AddUserBlackListAdapter.selectItem =
            { addBlackListUserModel: AddBlackListUserModel, position: Int ->
                addBlackListUserModel.isSelected = !addBlackListUserModel.isSelected
                addBlackListBtn.changeStateAddBlackList(addBlackListUserModel.isSelected)
                if (addBlackListUserModel.isSelected) {
                    lastSelectedUser?.run {
                        isSelected = false
                        userAdapter.notifyItemChanged(lastPosition)
                    }
                    lastSelectedUser = addBlackListUserModel
                    lastPosition = position
                } else {
                    lastSelectedUser = null
                    addBlackListUserModel.isSelected = false
                }
                userAdapter.notifyItemChanged(lastPosition)
            }
    }

    @SuppressLint("CheckResult")
    private fun banUser(userId: String) {
        compositeDisposable.add(
            viewModel.banUserInGroup(userId, BAN_REASON, groupId)
                .subscribe(
                    {
                        textNoFoundId.visibility = View.INVISIBLE
                        getData(inputBlackListAddId.text.toString())
                        Toast.makeText(requireContext(), "ID: $userId", Toast.LENGTH_SHORT).show()
                    },
                    {
                        textNoFoundId.visibility = View.VISIBLE
                        it.printStackTrace()
                    }
                )
        )
    }

    private fun TextView.changeStateAddBlackList(enable: Boolean) {
        isEnabled = enable
        if (enable) setTextAppearance(R.style.TextDark12sp)
        else setTextAppearance(R.style.TextHelp12sp)
    }
}
