package com.intergroupapplication.presentation.feature.news.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.InfoForCommentEntity
import com.intergroupapplication.domain.entity.UserEntity
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
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import javax.inject.Inject
import javax.inject.Named

class NewsFragment(): BaseFragment(), NewsView{

    companion object {
        const val LABEL = "fragment_news"
    }

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

    private lateinit var viewModel: NewsViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun layoutRes() = R.layout.fragment_news

    override fun getSnackBarCoordinator(): ViewGroup? = newsCoordinator

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    lateinit var profileAvatarHolder: AvatarImageUploadingView

    var clickedPostId: String? = null

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
        Appodeal.cache(requireActivity(), Appodeal.NATIVE, 5)
        //paging 3
        newPaging()
        //crashing app when provide it by dagger
        //newsPosts.layoutManager = layoutManager
        newsPosts.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        newsPosts.itemAnimator = null
    }

    fun newPaging() {
//        newSwipe.setOnRefreshListener {
//            adapterNews.refresh()
//        }
//        newsPosts.adapter = adapterNews
//        lifecycleScope.launch {
//            adapterNews.loadStateFlow.collectLatest { loadStates ->
//                when(loadStates.refresh) {
//                    is LoadState.Loading -> {
//                        if (adapterNews.itemCount == 0) {
//                            loading_layout.show()
//                        } else adapterNews.addLoading()
//                        adapterNews.removeError()
//                        emptyText.hide()
//                    }
//                    is LoadState.Error -> {
//                        newSwipe.isRefreshing = false
//                        emptyText.hide()
//                        adapterNews.removeLoading()
//                        loading_layout.gone()
//                        adapterNews.addError()
//                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
//                    }
//                    is LoadState.NotLoading -> {
//                        if (adapterNews.itemCount == 0) {
//                            emptyText.show()
//                        } else {
//                            emptyText.hide()
//                        }
//                        loading_layout.gone()
//                        adapterNews.removeError()
//                        adapterNews.removeLoading()
//                        newSwipe.isRefreshing = false
//                    }
//                    else ->{ newSwipe.isRefreshing = false }
//                }
//            }
//        }
        newSwipe.setOnRefreshListener {
            adapter.refresh()
        }
        newsPosts.adapter = concatAdapter
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when(loadStates.refresh) {
                    is LoadState.Loading -> {
                        if (adapter.itemCount == 0) {
                            loading_layout.show()
                        }
                        emptyText.hide()
                    }
                    is LoadState.Error -> {
                        newSwipe.isRefreshing = false
                        emptyText.hide()
                        loading_layout.gone()
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
                        loading_layout.gone()
                        newSwipe.isRefreshing = false
                        //if (!job.isCancelled) progress_first_loading.hide()
                    }
                    else ->{ newSwipe.isRefreshing = false } //if (!job.isCancelled) progress_first_loading.hide()
                }
            }
        }
    }

    fun setAdapter() {
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
                                .doFinally { item.isLoading = false }
                                .subscribe({
                                    item.bells.isActive = false
                                    item.bells.count--
                                    adapter.notifyItemChanged(pos)
                                }, { errorHandler.handle(it) }))
                    } else {
                        compositeDisposable.add(viewModel.setBell(item.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe { item.isLoading = true }
                                .doFinally { item.isLoading = false }
                                .subscribe({
                                    item.bells.isActive = true
                                    item.bells.count++
                                    adapter.notifyItemChanged(pos)
                                }, { errorHandler.handle(it) }))
                    }
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
        viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, navigationCoordinator, false)
        viewDrawer.profileAvatarHolder.setOnClickListener {
                if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                    dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() }, R.id.fromGallery to { presenter.attachFromGallery() }))
                }
            }
        viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, navigationCoordinator, false)
        profileAvatarHolder = viewDrawer.profileAvatarHolder
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        lateinit var drawerItem: PrimaryDrawerItem
        drawer = drawer {
            sliderBackgroundColorRes = R.color.profileTabColor
            headerView = viewDrawer
            actionBarDrawerToggleEnabled = true
            translucentStatusBar = true
            viewDrawer.profileAvatarHolder.setOnClickListener {
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
                onClick { v ->
                    toolbarTittle.text = getString(R.string.news)
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
                onClick { v ->
                    findNavController().navigate(R.id.action_newsFragment2_to_groupListFragment2)
                    toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
            primaryItem(getString(R.string.music)) {
                icon = R.drawable.ic_music
                selectedIcon = R.drawable.ic_music_act
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                onClick { v ->
                    findNavController().navigate(R.id.action_newsFragment2_to_audioListFragment)
                    toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
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
                onClick { v ->
                    userSession.logout()
                    findNavController().navigate(R.id.action_newsFragment2_to_loginActivity)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            viewDrawer.drawerArrow.setOnClickListener { closeDrawer() }
            drawerItem.withOnDrawerItemClickListener { _, _, _ ->
                findNavController().navigate(R.id.action_newsFragment2_self)
                toolbarTittle.text = getString(R.string.news)
                false
            }
        }
        toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
    }

    override fun showImageUploadingStarted(path: String) {
        //profileAvatarHolder.showImageUploadingStarted(path)
        profileAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded(path: String) {
        presenter.changeUserAvatar()
    }

    override fun avatarChanged(url: String) {
        profileAvatarHolder.showAvatar(url)
        profileAvatarHolder.showImageUploaded()
    }

    override fun showLastAvatar(lastAvatar: String?) {
        profileAvatarHolder.clearUploadingState(lastAvatar)
    }

    override fun showImageUploadingProgress(progress: Float, path: String) {
        profileAvatarHolder.showImageUploadingProgress(progress)
    }


    override fun showImageUploadingError(path: String) {
        profileAvatarHolder.clearUploadingState()
        presenter.showLastUserAvatar()
    }

    override fun showUserInfo(userEntity: UserEntity) {
        val userName = userEntity.firstName + " " + userEntity.surName
        viewDrawer.profileName.text = userName
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
