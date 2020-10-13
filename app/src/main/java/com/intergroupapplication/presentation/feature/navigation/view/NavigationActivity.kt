package com.intergroupapplication.presentation.feature.navigation.view

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.UserEntity
import com.intergroupapplication.presentation.base.BaseActivity
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.navigation.presenter.NavigationPresenter
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import kotlinx.android.synthetic.main.activity_navigation.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import kotlinx.android.synthetic.main.main_toolbar_layout.*
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import javax.inject.Inject


class NavigationActivity : BaseActivity(), NavigationView {

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

    @Inject
    override lateinit var navigator: SupportAppNavigator

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    private var doubleBackToExitPressedOnce = false

    private lateinit var view: View

    private lateinit var profileAvatarHolder: AvatarImageUploadingView

    private var exitHandler: Handler? = null

    @LayoutRes
    override fun layoutRes() = R.layout.activity_navigation

    override fun getSnackBarCoordinator(): CoordinatorLayout = navigationCoordinator

    override fun viewCreated() {
        view = layoutInflater.inflate(R.layout.layout_profile_header, navigationCoordinator, false)
        profileAvatarHolder = view.profileAvatarHolder
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        lateinit var drawerItem: PrimaryDrawerItem
        drawer {
            sliderBackgroundColorRes = R.color.profileTabColor
            headerView = view
            actionBarDrawerToggleEnabled = true
            translucentStatusBar = true
            toolbar = navigationToolbar as Toolbar
            view.profileAvatarHolder.setOnClickListener {
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
                typeface = Typeface.createFromAsset(assets, "roboto.regular.ttf")
                onClick { _ ->
                    presenter.goToNewsScreen()
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
                typeface = Typeface.createFromAsset(assets, "roboto.regular.ttf")
                onClick { _ ->
                    presenter.goToGroupListScreen()
                    toolbarTittle.text = getString(R.string.groups)
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { _ ->
                    presenter.goOutFromProfile()
                    toolbarTittle.text = getString(R.string.logout)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            view.drawerArrow.setOnClickListener { closeDrawer() }
        }
        presenter.getUserInfo()
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
        view.profileName.text = userName
        doOrIfNull(userEntity.avatar,
                { profileAvatarHolder.showAvatar(it) },
                { profileAvatarHolder.showAvatar(R.drawable.application_logo) })
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            ExitActivity.exitApplication(this)
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
        exitHandler = Handler()
        exitHandler?.postDelayed({ doubleBackToExitPressedOnce = false }, EXIT_DELAY)
    }

    override fun onDestroy() {
        exitHandler?.removeCallbacks(null)
        super.onDestroy()
    }
}
