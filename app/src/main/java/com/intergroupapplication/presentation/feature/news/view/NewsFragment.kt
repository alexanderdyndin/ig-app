package com.intergroupapplication.presentation.feature.news.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentNewsBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.group.di.GroupViewModule
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import com.intergroupapplication.presentation.feature.news.presenter.NewsPresenter
import com.intergroupapplication.presentation.feature.news.viewmodel.NewsViewModel
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.workable.errorhandler.Action
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class NewsFragment(): BaseFragment(), NewsView, CoroutineScope{

    companion object {
        const val LABEL = "fragment_news"
    }

    private val viewBinding by viewBinding(FragmentNewsBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: NewsPresenter

    @ProvidePresenter
    fun providePresenter(): NewsPresenter = presenter
    
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

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var job : Job = Job()

    private lateinit var viewModel: NewsViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun layoutRes() = R.layout.fragment_news

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.newsCoordinator

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    lateinit var profileAvatarHolder: AvatarImageUploadingView

    var clickedPostId: String? = null

    private lateinit var newsPosts: RecyclerView
    private lateinit var newSwipe: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[NewsViewModel::class.java]
        lifecycleScope.newCoroutineContext(this.coroutineContext)
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
    }

    override fun onResume() {
        super.onResume()
        job = Job()
    }

    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    private fun newPaging() {
        newSwipe.setOnRefreshListener {
            adapter.refresh()
        }
        newsPosts.adapter = concatAdapter
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
            if (job.isCancelled) return@collectLatest
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
            complaintListener = { presenter.complaintPost(it) }
            commentClickListener = {
                clickedPostId = it.id
                openCommentDetails(InfoForCommentEntity(it, true))
            }
            groupClickListener = {
                val data = bundleOf(GROUP_ID to it)
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

    override fun showMessage(resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }

    override fun showMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        //adapterWrapper.release()
        presenter.unsubscribe()
        super.onDestroy()
    }

    fun openCommentDetails(entity: InfoForCommentEntity) {
        val data = bundleOf(GroupViewModule.COMMENT_POST_ENTITY to entity)
        findNavController().navigate(R.id.action_newsFragment2_to_commentsDetailsActivity, data)
    }


    override fun viewCreated() {
        viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, viewBinding.newsCoordinator, false)
        viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder).setOnClickListener {
                if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                    dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
                }
            }
        profileAvatarHolder = viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder)
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        lateinit var drawerItem: PrimaryDrawerItem
        drawer = drawer {
            sliderBackgroundColorRes = R.color.profileTabColor
            headerView = viewDrawer
            actionBarDrawerToggleEnabled = true
            translucentStatusBar = true
            viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder).setOnClickListener {
                if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                    dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
                }
            }
            drawerItem = primaryItem(getString(R.string.news)) {
                icon = R.drawable.ic_news
                selectedIcon = R.drawable.ic_news_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { _ ->
                    viewBinding.navigationToolbar.toolbarTittle.text = getString(R.string.news)
                    false
                }
            }
            primaryItem(getString(R.string.groups)) {
                icon = R.drawable.ic_groups
                selectedIcon = R.drawable.ic_groups_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { _ ->
                    findNavController().navigate(R.id.action_newsFragment2_to_groupListFragment2)
                    viewBinding.navigationToolbar.toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
//            primaryItem(getString(R.string.music)) {
//                icon = R.drawable.ic_music
//                selectedIcon = R.drawable.ic_music_act
//                textColorRes = R.color.whiteTextColor
//                selectedColorRes = R.color.profileTabColor
//                selectedTextColorRes = R.color.selectedItemTabColor
//                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
//                onClick { v ->
//                    findNavController().navigate(R.id.action_newsFragment2_to_audioListFragment)
//                    toolbarTittle.text = getString(R.string.groups)
//                    false
//                }
//            }
            primaryItem(getString(R.string.buy_premium)) {
                icon = R.drawable.icon_like
                selectedIcon = R.drawable.icon_like
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                selectable = false
                onClick { _ ->
                    (requireActivity() as MainActivity).bill()
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { _ ->
                    userSession.logout()
                    findNavController().navigate(R.id.action_newsFragment2_to_loginActivity)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            viewDrawer.findViewById<ImageView>(R.id.drawerArrow).setOnClickListener { closeDrawer() }
            drawerItem.withOnDrawerItemClickListener { _, _, _ ->
                findNavController().navigate(R.id.action_newsFragment2_self)
                viewBinding.navigationToolbar.toolbarTittle.text = getString(R.string.news)
                false
            }
        }
        viewBinding.navigationToolbar.toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
    }

    override fun showImageUploadingStarted(chooseMedia: ChooseMedia) {
        profileAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded(chooseMedia: ChooseMedia) {
        presenter.changeUserAvatar()
    }

    override fun avatarChanged(url: String) {
        profileAvatarHolder.showAvatar(url)
        profileAvatarHolder.showImageUploaded()
    }

    override fun showLastAvatar(lastAvatar: String?) {
        profileAvatarHolder.clearUploadingState(lastAvatar)
    }

    override fun showImageUploadingProgress(progress: Float, chooseMedia: ChooseMedia) {
        profileAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError(chooseMedia: ChooseMedia) {
        profileAvatarHolder.clearUploadingState()
        presenter.showLastUserAvatar()
    }

    override fun showUserInfo(userEntity: UserEntity) {
        val userName = userEntity.firstName + " " + userEntity.surName
        viewDrawer.findViewById<TextView>(R.id.profileName).text = userName
        doOrIfNull(userEntity.avatar,
                { profileAvatarHolder.showAvatar(it) },
                { profileAvatarHolder.showAvatar(R.drawable.application_logo) })
    }

    override fun openConfirmationEmail() = Action { _, _ ->
        if (findNavController().currentDestination?.label == LABEL) {
            val email = userSession.email?.email.orEmpty()
            val data = bundleOf("entity" to email)
            findNavController().navigate(R.id.action_newsFragment2_to_confirmationMailActivity, data)
        }
    }

    override fun openCreateProfile()  = Action { _, _ ->
        if (findNavController().currentDestination?.label == LABEL)
            findNavController().navigate(R.id.action_newsFragment2_to_createUserProfileActivity)
    }

}
