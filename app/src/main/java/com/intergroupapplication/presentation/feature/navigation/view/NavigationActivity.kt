package com.intergroupapplication.presentation.feature.navigation.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.appodeal.ads.Appodeal
import com.appodeal.ads.UserSettings
import com.appodeal.ads.utils.PermissionsHelper.AppodealPermissionCallbacks
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.base.BaseFragment
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.navigation.presenter.NavigationPresenter
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import kotlinx.android.synthetic.main.main_toolbar_layout.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class NavigationActivity : BaseFragment(), NavigationView {

    companion object {
        private const val EXIT_DELAY = 2000L

        fun getIntent(context: Context?) = Intent(context, NavigationActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: NavigationPresenter

    @ProvidePresenter
    fun providePresenter(): NavigationPresenter = presenter

    //@Inject
    //override lateinit var navigator: SupportAppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setAppodeal()
        val filepatch = Environment.getExternalStorageDirectory().path+"/RxPaparazzo/"
        val file = File("$filepatch.nomedia")
        try {
            file.createNewFile()
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

//    private fun setAppodeal() {
//        Appodeal.initialize(this, BuildConfig.APPODEAL_APP_KEY, Appodeal.NATIVE, userSession.isAcceptTerms())
//        Appodeal.setTesting(true)
//        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(userSession.user?.birthday)
//        val c = Calendar.getInstance()
//        val year = c.get(Calendar.YEAR)
//        Appodeal.setUserAge(year - date.year)
//        val gender = when (userSession.user?.gender) {
//            "male" -> UserSettings.Gender.MALE
//            "female" -> UserSettings.Gender.FEMALE
//            else -> UserSettings.Gender.OTHER
//        }
//        Appodeal.setUserGender(gender)
//        Appodeal.cache(this, Appodeal.NATIVE, 5)
//        Appodeal.setUserId(userSession.user?.id ?: "123")
//
//    }

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    private var doubleBackToExitPressedOnce = false

    private lateinit var viewDrawer: View

    lateinit var drawer: Drawer

    var newsLaunchCount: Int = 0

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    private var exitHandler: Handler? = null

    val r = Runnable { this.doubleBackToExitPressedOnce = false }

    @LayoutRes
    override fun layoutRes() = R.layout.activity_navigation

    override fun getSnackBarCoordinator(): CoordinatorLayout = navigationCoordinator

    override fun viewCreated() {
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
                    v?.findNavController()?.navigate(R.id.action_groupListFragment2_to_newsFragment2)
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
                    //presenter.goToGroupListScreen()
                    v?.findNavController()?.navigate(R.id.action_newsFragment2_to_groupListFragment2)
                    toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(requireActivity().assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { v ->
                    presenter.goOutFromProfile()
                    //v?.findNavController()?.popBackStack()
                    toolbarTittle.text = getString(R.string.logout)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            //view.drawerArrow.setOnClickListener { closeDrawer() }
        }
        navigationToolbar.toolbarMenu.setOnClickListener {
            drawer.openDrawer()
        }
        presenter.getUserInfo()
        findNavController().navigate(R.id.action_navigationActivity2_to_newsFragment2)
    }

    override fun showImageUploadingStarted(path: String) {
        //profileAvatarHolder.showImageUploadingStarted(path)
        profileAvatarHolder.showImageUploadingStartedWithoutFile()
    }

    override fun showImageUploaded() {
        presenter.changeUserAvatar()
    }


    override fun avatarChanged(url: String) {
        profileAvatarHolder.showAvatar(url)
        profileAvatarHolder.showImageUploaded()
    }

    override fun showLastAvatar(lastAvatar: String?) {
        profileAvatarHolder.clearUploadingState(lastAvatar)
    }

    override fun showImageUploadingProgress(progress: Float) {
        profileAvatarHolder.showImageUploadingProgress(progress)
    }

    override fun showImageUploadingError() {
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

//    override fun onBackPressed() {
//        if (doubleBackToExitPressedOnce) {
//            ExitActivity.exitApplication(requireContext())
//            return
//        }
//        this.doubleBackToExitPressedOnce = true
//        Toast.makeText(requireContext(), getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
//        exitHandler = Handler(Looper.getMainLooper())
//        exitHandler?.postDelayed(r, EXIT_DELAY)
//    }

    override fun onDestroy() {
        exitHandler?.removeCallbacks(r)
        super.onDestroy()
    }
}
