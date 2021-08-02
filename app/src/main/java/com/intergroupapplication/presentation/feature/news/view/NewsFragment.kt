package com.intergroupapplication.presentation.feature.news.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentNewsBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule
import com.intergroupapplication.presentation.feature.group.view.GroupFragment.Companion.GROUP
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.viewmodel.NewsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import javax.inject.Inject
import javax.inject.Named

class NewsFragment: BaseFragment() {

    private val viewBinding by viewBinding(FragmentNewsBinding::bind)
    
    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var adapter: NewsAdapter

    @Inject
    lateinit var concatAdapter: ConcatAdapter

    @Inject
    @Named("footer")
    lateinit var footerAdapter: PagingLoadingAdapter

    private lateinit var viewModel: NewsViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun layoutRes() = R.layout.fragment_news

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.newsCoordinator

    var clickedPostId: String? = null

    private lateinit var newsPosts: RecyclerView
    private lateinit var newSwipe: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[NewsViewModel::class.java]
        setAdapter()
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    ExitActivity.exitApplication(requireContext())
                    return
                }
                doubleBackToExitPressedOnce = true
                Toast.makeText(requireContext(),getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
                exitHandler = Handler(Looper.getMainLooper())
                exitHandler?.postDelayed(exitFlag, MainActivity.EXIT_DELAY)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newsPosts = viewBinding.newsPosts
        newSwipe = viewBinding.newSwipe
        progressBar = viewBinding.loadingLayout.progressBar
        emptyText = viewBinding.emptyText
        newPaging()
        newsPosts.itemAnimator = null

        viewBinding.navigationToolbar.toolbarTittle.setText(R.string.news)
        viewBinding.navigationToolbar.toolbarMenu.setOnClickListener {
            val activity = requireActivity()
            if (activity is MainActivity) {
                activity.drawer.openDrawer()
            }
        }
    }

    private fun newPaging() {
        newSwipe.setOnRefreshListener {
            adapter.refresh()
        }
        newsPosts.adapter = concatAdapter
        lifecycleScope.launchWhenResumed {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) {
                            progressBar.show()
                        }
                        emptyText.hide()
                    }
                    is LoadState.Error -> {
                        newSwipe.isRefreshing = false
                        emptyText.hide()
                        progressBar.gone()
                        if (adapter.itemCount == 0) {
                            footerAdapter.loadState = LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        if (adapter.itemCount == 0) {
                            emptyText.show()
                        } else {
                            emptyText.hide()
                        }
                        progressBar.gone()
                        newSwipe.isRefreshing = false
                    }
                    else ->{ newSwipe.isRefreshing = false }
                }
            }
        }
    }

    private fun setAdapter() {
        NewsAdapter.apply {
            complaintListener = {  id ->
                compositeDisposable.add(viewModel.sendComplaint(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showToast(R.string.complaint_send)
                    }, {
                        errorHandler.handle(it)
                    }))
            }
            commentClickListener = {
                clickedPostId = it.id
                openCommentDetails(InfoForCommentEntity(it, true))
            }
            groupClickListener = {
                val data = bundleOf(GROUP_ID to it.id, GROUP to it)
                findNavController().navigate(R.id.action_newsFragment2_to_groupActivity, data)
            }
            imageClickListener = { list: List<FileEntity>, i: Int ->
                val data = bundleOf("images" to list.toTypedArray(), "selectedId" to i)
                findNavController().navigate(R.id.action_newsFragment2_to_imageFragment, data)
            }
            likeClickListener = { like, dislike, item, position ->
                if (!item.isLoading) {
                    compositeDisposable.add(viewModel.setReact(isLike = like, isDislike = dislike, postId = item.id)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe {
                                item.isLoading = true
                            }
                            .doFinally {
                                item.isLoading = false
                            }
                            .subscribe({
                                item.reacts = it
                                adapter.notifyItemChanged(position)
                            },
                                    {
                                        errorHandler.handle(it)
                                    }))
                }
            }
            deleteClickListener = { id: Int, pos: Int ->
                compositeDisposable.add(viewModel.deletePost(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            adapter.notifyItemRemoved(pos) //todo сделать человеческое удаление
                        }, { errorHandler.handle(it) })
                )
            }
            bellClickListener = { item: GroupPostEntity.PostEntity, pos: Int ->
                if (!item.isLoading) {
                    if (item.bells.isActive) {
                        compositeDisposable.add(viewModel.deleteBell(item.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { item.isLoading = true }
                                .doFinally {
                                    item.isLoading = false
                                    adapter.notifyItemChanged(pos)
                                }
                                .subscribe({
                                    item.bells.isActive = false
                                    item.bells.count--
                                }, { exception ->
                                    if (exception is NotFoundException) {
                                        item.bells.isActive = false
                                        item.bells.count--
                                    } else
                                        errorHandler.handle(exception)
                                }))
                    } else {
                        compositeDisposable.add(viewModel.setBell(item.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { item.isLoading = true }
                                .doFinally {
                                    item.isLoading = false
                                    adapter.notifyItemChanged(pos)
                                }
                                .subscribe({
                                    item.bells.isActive = true
                                    item.bells.count++
                                }, { exception ->
                                    if (exception is CompositeException) {
                                        exception.exceptions.forEach { ex ->
                                            (ex as? FieldException)?.let {
                                                if (it.field == "post") {
                                                    item.bells.isActive = true
                                                    item.bells.count++
                                                }
                                            }
                                        }
                                    } else
                                        errorHandler.handle(exception)
                                }))
                    }
                }
            }
            progressBarVisibility = {visibility ->
                if (visibility){
                    viewBinding.progressDownload.show()
                }
                else{
                    viewBinding.progressDownload.gone()
                }
            }
            USER_ID = userSession.user?.id?.toInt()
            compositeDisposable.add(
                    viewModel.getNews()
                            .subscribe {
                                adapter.submitData(lifecycle, it)
                            }
            )
        }
    }

    private fun openCommentDetails(entity: InfoForCommentEntity) {
        val data = bundleOf(GroupViewModule.COMMENT_POST_ENTITY to entity)
        findNavController().navigate(R.id.action_newsFragment2_to_commentsDetailsActivity, data)
    }

}
