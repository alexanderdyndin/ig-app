package com.intergroupapplication.presentation.feature.grouplist.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegateGroup
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import moxy.InjectViewState
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject
import javax.inject.Named

@SuppressLint("ValidFragment")
class GroupsFragment constructor(
        private val adapter: AdmobBannerRecyclerAdapterWrapper
) : Fragment() {

    private var mPage = 0
    var doOnViewCreated: (View) -> Unit = { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = requireArguments().getInt(ARG_PAGE)
        }
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        fun newInstance(page: Int, adapter: AdmobBannerRecyclerAdapterWrapper)
                : GroupsFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = GroupsFragment(adapter)
            fragment.arguments = args
            return fragment
        }
    }

    fun layoutRes() = R.layout.fragment_group_category

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(layoutRes(), container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupsList = view.findViewById<RecyclerView>(R.id.allGroupsList)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        emptyText.visibility = View.INVISIBLE
        groupsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        groupsList.setHasFixedSize(true)
        groupsList.itemAnimator = null
        groupsList.adapter = adapter
        doOnViewCreated.invoke(view)
    }

}