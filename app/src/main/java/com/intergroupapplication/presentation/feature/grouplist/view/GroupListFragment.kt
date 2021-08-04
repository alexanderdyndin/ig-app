package com.intergroupapplication.presentation.feature.grouplist.view

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.databinding.FragmentGroupListBinding
import com.intergroupapplication.di.qualifier.*
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.GroupAlreadyFollowingException
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.base.adapter.PagingLoadingAdapter
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListAdapter
import com.intergroupapplication.presentation.feature.grouplist.adapter.GroupListsAdapter
import com.intergroupapplication.presentation.feature.grouplist.other.ViewPager2Circular
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.grouplist.viewModel.GroupListViewModel
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.lang.Runnable
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class GroupListFragment : BaseFragment(), GroupListView, CoroutineScope {


    companion object {
        const val CREATED_GROUP_ID = "created_group_id"
        private const val TYPEFACE_TEXT = "roboto.regular.ttf"
    }

    private val viewBinding by viewBinding(FragmentGroupListBinding::bind)

    @Inject
    @InjectPresenter
    lateinit var presenter: GroupListPresenter

    @ProvidePresenter
    fun providePresenter(): GroupListPresenter = presenter

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @Named
    private var job: Job = Job()

    private lateinit var viewModel: GroupListViewModel

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    private val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    private lateinit var viewDrawer: View

    private lateinit var drawer: Drawer

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    private var currentScreen = 0

    @Inject
    @All
    lateinit var adapterAll: GroupListAdapter

    @Inject
    @Subscribed
    lateinit var adapterSubscribed: GroupListAdapter

    @Inject
    @Owned
    lateinit var adapterOwned: GroupListAdapter

    @Inject
    @FooterAll
    lateinit var adapterFooterAll: PagingLoadingAdapter

    @Inject
    @FooterSub
    lateinit var adapterFooterSub: PagingLoadingAdapter

    @Inject
    @FooterAdm
    lateinit var adapterFooterAdm: PagingLoadingAdapter

    @Inject
    lateinit var viewPagerAdapter: GroupListsAdapter


    @ExperimentalCoroutinesApi
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            fetchGroups(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun layoutRes() = R.layout.fragment_group_list

    override fun getSnackBarCoordinator(): ViewGroup = viewBinding.groupListCoordinator

    private lateinit var pager: ViewPager2
    private lateinit var slidingCategories: TabLayout
    private lateinit var swipeGroups: SwipeRefreshLayout
    private lateinit var createGroup: Button
    private lateinit var activityMainBtnFilter: ImageButton
    private lateinit var activityMainSearchInput: EditText
    private lateinit var toolbarMenu: TextView

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, modelFactory)[GroupListViewModel::class.java]
        lifecycleScope.newCoroutineContext(this.coroutineContext)
        fetchGroups()
        prepareAdapter()
        activity?.onBackPressedDispatcher?.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (doubleBackToExitPressedOnce) {
                        ExitActivity.exitApplication(requireContext())
                        return
                    }
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exitHandler = Handler(Looper.getMainLooper())
                    exitHandler?.postDelayed(exitFlag, MainActivity.EXIT_DELAY)
                }
            })
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pager = viewBinding.pager
        slidingCategories = viewBinding.slidingCategories
        swipeGroups = viewBinding.swipeGroups
        createGroup = viewBinding.navigationToolbar.createGroup
        activityMainBtnFilter = viewBinding.activityMainBtnFilter
        activityMainSearchInput = viewBinding.activityMainSearchInput

        pager.apply {
            adapter = viewPagerAdapter
            val handler = ViewPager2Circular(this, swipeGroups)
            handler.pageChanged = {
                currentScreen = it
            }
            registerOnPageChangeCallback(handler)
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
        val tabTitles = arrayOf(
            resources.getString(R.string.allGroups),
            resources.getString(R.string.subGroups),
            resources.getString(R.string.admGroups)
        )
        TabLayoutMediator(slidingCategories, pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        activityMainBtnFilter.setOnClickListener {
            //todo
        }
        createGroup.visibility = View.VISIBLE
        createGroup.setOnClickListener { openCreateGroup() }

        swipeGroups.setOnRefreshListener {
            when (currentScreen) {
                0 -> adapterAll.refresh()
                1 -> adapterSubscribed.refresh()
                2 -> adapterOwned.refresh()
            }
        }
        setAdapter(adapterAll, adapterFooterAll)
        setAdapter(adapterSubscribed, adapterFooterSub)
        setAdapter(adapterOwned, adapterFooterAdm)
    }

    fun prepareAdapter() {
        with(GroupListAdapter) {
            userID = userSession.user?.id
            groupClickListener = { groupId ->
                val data = bundleOf(GROUP_ID to groupId)
                findNavController().navigate(R.id.action_groupListFragment_to_groupFragment, data)
            }
            //todo не всегда подписка/отписка отображается в UI
            subscribeClickListener = { group, pos ->
                if (!group.isSubscribing) {
                    compositeDisposable.add(viewModel.subscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            group.isSubscribing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .doFinally {
                            group.isSubscribing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .subscribe({
                            group.isFollowing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                }
                            }
                        }, { exception ->
                            if (exception is CompositeException) {
                                exception.exceptions.forEach { ex ->
                                    (ex as? FieldException)?.let {
                                        if (it.field == "group") {
                                            group.isFollowing = !group.isFollowing
                                        }
                                    }
                                }
                            }
                            errorHandler.handle(exception)
                        })
                    )
                }
            }
            unsubscribeClickListener = { group, pos ->
                if (!group.isSubscribing) {
                    compositeDisposable.add(viewModel.unsubscribeGroup(group.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            group.isSubscribing = true
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .doFinally {
                            group.isSubscribing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterAll.notifyItemChanged(pos)
                                }
                                1 -> {
                                    adapterSubscribed.notifyItemChanged(pos)
                                }
                            }
                        }
                        .subscribe({
                            group.isFollowing = false
                            when (currentScreen) {
                                0 -> {
                                    adapterSubscribed.refresh()
                                }
                                1 -> {
                                    adapterAll.refresh()
                                }
                            }
                        }, {
                            if (it is GroupAlreadyFollowingException) {
                                group.isFollowing = !group.isFollowing
                                when (currentScreen) {
                                    0 -> {
                                        adapterAll.notifyDataSetChanged()
                                    }
                                    1 -> {
                                        adapterSubscribed.notifyDataSetChanged()
                                    }
                                }
                            }
                            errorHandler.handle(it)
                        })
                    )
                }
            }
        }
    }

    private fun setAdapter(adapter: PagingDataAdapter<*, *>, footer: LoadStateAdapter<*>) {
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                if (job.isCancelled) return@collectLatest
                when (loadStates.refresh) {
                    is LoadState.Loading -> {

                    }
                    is LoadState.Error -> {
                        swipeGroups.isRefreshing = false
                        if (adapter.itemCount == 0) {
                            footer.loadState =
                                LoadState.Error((loadStates.refresh as LoadState.Error).error)
                        }
                        errorHandler.handle((loadStates.refresh as LoadState.Error).error)
                    }
                    is LoadState.NotLoading -> {
                        swipeGroups.isRefreshing = false
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun fetchGroups(query: String = "") {
        compositeDisposable.clear()
        compositeDisposable.add(
            viewModel.fetchGroups(query)
                .subscribe {
                    adapterAll.submitData(lifecycle, it)
                }
        )
        compositeDisposable.add(
            viewModel.fetchSubGroups(query)
                .subscribe {
                    adapterSubscribed.submitData(lifecycle, it)
                }
        )
        compositeDisposable.add(
            viewModel.fetchAdmGroups(query)
                .subscribe {
                    adapterOwned.submitData(lifecycle, it)
                }
        )
    }


    @ExperimentalCoroutinesApi
    override fun onResume() {
        super.onResume()
        activityMainSearchInput.addTextChangedListener(textWatcher)
        job = Job()
    }

    @ExperimentalCoroutinesApi
    override fun onPause() {
        super.onPause()
        activityMainSearchInput.removeTextChangedListener(textWatcher)
        job.cancel()
    }

    private fun openCreateGroup() {
        findNavController().navigate(R.id.action_groupListFragment_to_createGroupFragment)
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

    override fun viewCreated() {
        toolbarMenu = viewBinding.navigationToolbar.toolbarMenu
        viewDrawer = layoutInflater.inflate(
            R.layout.layout_profile_header,
            viewBinding.groupListCoordinator, false
        )
        profileAvatarHolder = viewDrawer.findViewById(R.id.profileAvatarHolder)
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        lateinit var drawerItem: PrimaryDrawerItem
        drawer = drawer {                           //FIXME ЧТО ЗА БЛЯДСКИЙ ГОВНОКОД??
            sliderBackgroundColorRes = R.color.profileTabColor
            headerView = viewDrawer
            actionBarDrawerToggleEnabled = true
            translucentStatusBar = true
            viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder)
                .setOnClickListener {
                    if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                        || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE
                    ) {
                        dialogDelegate.showDialog(
                            R.layout.dialog_camera_or_gallery,
                            mapOf(R.id.fromCamera to { presenter.attachFromCamera() },
                                R.id.fromGallery to { presenter.attachFromGallery() })
                        )
                    }
                }
            primaryItem(getString(R.string.news)) {
                icon = R.drawable.ic_news
                selectedIcon = R.drawable.ic_news_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, TYPEFACE_TEXT)
                onClick { _ ->
                    findNavController().navigate(R.id.action_groupListFragment_to_newsFragment)
                    viewBinding.navigationToolbar.toolbarTittle.text = getString(R.string.news)
                    false
                }
            }
            drawerItem = primaryItem(getString(R.string.groups)) {
                icon = R.drawable.ic_groups
                selectedIcon = R.drawable.ic_groups_blue
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, TYPEFACE_TEXT)
            }
            primaryItem(getString(R.string.buy_premium)) {
                icon = R.drawable.icon_like
                selectedIcon = R.drawable.icon_like
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                typeface = Typeface.createFromAsset(requireActivity().assets, TYPEFACE_TEXT)
                selectable = false
                onClick { _ ->
                    (requireActivity() as MainActivity).bill()
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(
                    requireActivity().assets,
                    TYPEFACE_TEXT
                )
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { _ ->
                    presenter.goOutFromProfile()
                    findNavController().navigate(R.id.action_groupListFragment_to_loginFragment)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            drawerItem.withOnDrawerItemClickListener { _, _, _ ->
                findNavController().navigate(R.id.action_groupListFragment_self)
                viewBinding.navigationToolbar.toolbarTittle.text = getString(R.string.groups)
                false
            }
            viewDrawer.findViewById<ImageView>(R.id.drawerArrow)
                .setOnClickListener { closeDrawer() }
        }
        toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
    }

}
