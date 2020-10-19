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
import com.intergroupapplication.R
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.delegate.PagingDelegate


@SuppressLint("ValidFragment")
class GroupsFragment constructor(
        private val adapter: GroupListAdapter
) : Fragment() {

    private var mPage = 0
    var doOnViewCreated: (View) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mPage = arguments!!.getInt(ARG_PAGE)
        }
    }

    companion object {
        const val ARG_PAGE = "ARG_PAGE"
        fun newInstance(page: Int, adapter: GroupListAdapter)
                : GroupsFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)
            val fragment = GroupsFragment(adapter)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_group_category, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val groupsList = view.findViewById<RecyclerView>(R.id.allGroupsList)
        groupsList.adapter = adapter
        doOnViewCreated(view)
    }

}