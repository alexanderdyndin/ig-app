package com.intergroupapplication.presentation.feature.grouplist.other

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupType
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter3
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_group_category.*
import kotlinx.android.synthetic.main.item_group_in_list.view.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("ValidFragment")
class GroupsFragment (
        private val groupType: GroupType = GroupType.ALL
) : BaseFragment() {

//    @Inject
//    lateinit var adapterGroups: GroupListAdapter3
//
//    @Inject
//    lateinit var adapterGroupsAD: AdmobBannerRecyclerAdapterWrapper
//
//    @Inject
//    lateinit var modelFactory: ViewModelProvider.Factory
//
//    private lateinit var viewModel: GroupListViewModel
//
//    var lastQuery: String? = null

    override fun layoutRes() = R.layout.fragment_group_category

    override fun getSnackBarCoordinator(): ViewGroup? = navigationCoordinator
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        viewModel = ViewModelProvider(requireActivity(), modelFactory)[GroupListViewModel::class.java]
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        val groupsList = view.findViewById<RecyclerView>(R.id.allGroupsList)
//        val emptyText = view.findViewById<TextView>(R.id.emptyText)
//        viewModel.query.observe(viewLifecycleOwner, Observer<String> { query ->
//            fetchGroups(query)
//            lastQuery = query
//        })
//        emptyText.visibility = View.INVISIBLE
//        groupsList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        groupsList.setHasFixedSize(true)
//        groupsList.itemAnimator = null
//        adapterGroups.subscribeClickListener = { id, view ->
//            compositeDisposable.add(viewModel.subscribeGroup(id)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnSubscribe {
//                        view.subscribingProgressBar.show()
//                        view.item_group__text_sub.isEnabled = false
//                    }
//                    .doOnComplete {
//                        //adapterGroups.refresh()
//                        //viewModel.query.value = lastQuery ?: ""
//                        view.subscribingProgressBar.hide()
//                        with(view.item_group__text_sub){
//                            isEnabled = true
//                            setOnClickListener {
//                                adapterGroups.unsubscribeClickListener.invoke(id, view)
//                            }
//                            text = resources.getText(R.string.unsubscribe)
//                            setBackgroundResource(R.drawable.btn_unsub)
//                        }
//                    }
//                    .doOnError {
//                        view.subscribingProgressBar.hide()
//                        view.item_group__text_sub.isEnabled = true
//                    }
//                    .subscribe({ }, {
//                        errorHandler.handle(it)
//                    }))
//        }
//        adapterGroups.unsubscribeClickListener = { id, view ->
//            compositeDisposable.add(viewModel.unsubscribeGroup(id)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnSubscribe {
//                        view.subscribingProgressBar.show()
//                        view.item_group__text_sub.isEnabled = false
//                    }
//                    .doOnComplete {
//                        //adapterGroups.refresh()
//                        //viewModel.query.value = lastQuery ?: ""
//                        view.subscribingProgressBar.hide()
//                        with(view.item_group__text_sub){
//                            isEnabled = true
//                            setOnClickListener {
//                                adapterGroups.subscribeClickListener.invoke(id, view)
//                            }
//                            text = resources.getText(R.string.subscribe)
//                            setBackgroundResource(R.drawable.btn_sub)
//                        }
//                    }
//                    .doOnError {
//                        view.subscribingProgressBar.hide()
//                        view.item_group__text_sub.isEnabled = true
//                    }
//                    .subscribe({ }, {
//                        errorHandler.handle(it)
//                    }))
//        }
//        groupsList.adapter = adapterGroupsAD
//        groupsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    viewModel.scrollingRecycler.value = false
//                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    viewModel.scrollingRecycler.value = true
//                }
//                super.onScrollStateChanged(recyclerView, newState)
//
//            }
//
//        })
//        group_swipe.setOnRefreshListener {
//            adapterGroups.refresh()
//        }
//        lifecycleScope.launch {
//            adapterGroups.loadStateFlow.collectLatest { loadStates ->
//            when (loadStates.refresh) {
//                is LoadState.Loading -> {
//                    if (adapterGroups.itemCount == 0)
//                        progress_loading.show()
//                    else adapterGroups.addLoading()
//                    adapterGroups.removeError()
//                    emptyText.hide()
//                }
//                is LoadState.Error -> {
//                    group_swipe.isRefreshing = false
//                    emptyText.hide()
//                    adapterGroups.addError()
//                    adapterGroups.removeLoading()
//                    progress_loading.hide()
//                    errorHandler.handle((loadStates.refresh as LoadState.Error).error)
//                }
//                is LoadState.NotLoading -> {
//                    if (adapterGroups.itemCount == 0) {
//                        emptyText.show()
//                    } else {
//                        emptyText.hide()
//                    }
//                    adapterGroups.removeError()
//                    adapterGroups.removeLoading()
//                    group_swipe.isRefreshing = false
//                    progress_loading.hide()
//                }
//                else -> {
//                    group_swipe.isRefreshing = false
//                }
//            }
//        }
//        }
//    }
//
//    fun fetchGroups(query: String = "") {
//        when (groupType) {
//            GroupType.ALL ->  {
//                compositeDisposable.add(
//                        viewModel.fetchGroups(query)
//                                .subscribe {
//                                    adapterGroups.submitData(lifecycle, it)
//                                }
//                )
//            }
//            GroupType.FOLLOWED -> {
//                compositeDisposable.add(
//                        viewModel.fetchSubGroups(query)
//                                .subscribe {
//                                    adapterGroups.submitData(lifecycle, it)
//                                }
//                )
//            }
//            GroupType.OWNED -> {
//                compositeDisposable.add(
//                        viewModel.fetchAdmGroups(query)
//                                .subscribe {
//                                    adapterGroups.submitData(lifecycle, it)
//                                }
//                )
//            }
//        }
//    }

}