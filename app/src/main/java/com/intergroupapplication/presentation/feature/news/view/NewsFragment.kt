package com.intergroupapplication.presentation.feature.news.view

import android.annotation.SuppressLint
import android.app.Activity
import androidx.paging.PagedList
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.PagingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.delegate.PagingDelegate
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.isVisible
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.COMMENTS_COUNT_VALUE
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.COMMENTS_DETAILS_REQUEST
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsActivity.Companion.GROUP_ID_VALUE
import com.intergroupapplication.presentation.feature.navigation.view.NavigationActivity
import com.intergroupapplication.presentation.feature.navigation.view.NavigationScreen
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.presenter.NewsPresenter
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.android.synthetic.main.main_toolbar_layout.view.*
import javax.inject.Inject

class NewsFragment @SuppressLint("ValidFragment") constructor(private val pagingDelegate: PagingDelegate)
    : BaseFragment(), NewsView,
        PagingView by pagingDelegate {

    constructor() : this(PagingDelegate())

    companion object {

        fun getInstance() = NewsFragment()

    }

    @Inject
    @InjectPresenter
    lateinit var presenter: NewsPresenter

    @ProvidePresenter
    fun providePresenter(): NewsPresenter = presenter

    @Inject
    lateinit var adapter: NewsAdapter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var diffUtil: DiffUtil.ItemCallback<GroupPostEntity>

    @Inject
    lateinit var layoutManager: RecyclerView.LayoutManager

    @Inject
    lateinit var adapterWrapper: AdmobBannerRecyclerAdapterWrapper

    override fun layoutRes() = R.layout.fragment_news

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagingDelegate.attachPagingView(adapter, newSwipe, emptyText)
        newsPosts.layoutManager = layoutManager
        newsPosts.itemAnimator = null
        adapter.retryClickListener = { presenter.reload() }
        adapter.commentClickListener = { openCommentDerails(InfoForCommentEntity(it, true)) }
        adapter.groupClickListener = { presenter.goToGroupScreen(it) }
        adapter.complaintListener = { presenter.complaintPost(it) }
        newsPosts.adapter = adapterWrapper
        newSwipe.setOnRefreshListener { presenter.refresh() }
        //presenter.getNews()
        //(activity as NavigationActivity).newsLaunchCount++
    }


    override fun onResume() {
        super.onResume()
        presenter.checkNewVersionAvaliable(activity?.supportFragmentManager!!)
        presenter.refresh()
    }


    override fun newsLoaded(posts: PagedList<GroupPostEntity>) {
        adapter.submitList(posts)
    }

    override fun showMessage(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showLoading(show: Boolean) {
        if (show) {
            emptyText.hide()
            adapter.removeError()
            //TODO ПОФИКСИТЬ ПЕРЕСКАКИВАНИЕ В КОНЕЦ СПИСКА ПРИ ПОВТОРНОЙ ЗАГРУЗКЕ ФРАГМЕНТА
            //if ((activity as NavigationActivity).newsLaunchCount == 1) {
                adapter.addLoading()
            //}
            //newsPosts.visibility = View.INVISIBLE
            //progressBar.visibility = View.VISIBLE
        } else {
            newSwipe.isRefreshing = false
            adapter.removeLoading()
            //progressBar.visibility = View.INVISIBLE
            //newsPosts.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                COMMENTS_DETAILS_REQUEST -> {
                    val groupId = data?.getStringExtra(GROUP_ID_VALUE).orEmpty()
                    val commentCount = data?.getStringExtra(COMMENTS_COUNT_VALUE).orEmpty()
                    adapter.itemUpdate(groupId, commentCount)
                }
            }
        }
    }

    override fun onDestroy() {
        adapterWrapper.release()
        super.onDestroy()
    }

    fun openCommentDerails(entity: InfoForCommentEntity) {
        startActivityForResult(CommentsDetailsActivity.getIntent(context, entity), COMMENTS_DETAILS_REQUEST)
    }

}
