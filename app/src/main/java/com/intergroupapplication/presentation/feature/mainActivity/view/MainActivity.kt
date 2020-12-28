package com.intergroupapplication.presentation.feature.mainActivity.view

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.appodeal.ads.Appodeal
import com.appodeal.ads.UserSettings
import com.intergroupapplication.BuildConfig
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.mainActivity.presenter.MainActivityPresenter
import com.intergroupapplication.presentation.feature.news.view.NewsFragment
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_profile_header.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


import javax.inject.Inject

class MainActivity : FragmentActivity(), MainActivityView {

    companion object {
        private const val EXIT_DELAY = 2000L
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: MainActivityPresenter

    @ProvidePresenter
    fun providePresenter(): MainActivityPresenter = presenter

    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var dialogDelegate: DialogDelegate

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    lateinit var compositeDisposable: CompositeDisposable

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
        setAppodeal()
        val filepatch = Environment.getExternalStorageDirectory().path+"/RxPaparazzo/"
        val file = File("$filepatch.nomedia")
        try {
            file.createNewFile()
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun setAppodeal() {
        Appodeal.initialize(this, BuildConfig.APPODEAL_APP_KEY, Appodeal.NATIVE, userSession.isAcceptTerms())
        Appodeal.setTesting(true)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(userSession.user?.birthday)
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        Appodeal.setUserAge(year - date.year)
        val gender = when (userSession.user?.gender) {
            "male" -> UserSettings.Gender.MALE
            "female" -> UserSettings.Gender.FEMALE
            else -> UserSettings.Gender.OTHER
        }
        Appodeal.setUserGender(gender)
        Appodeal.cache(this, Appodeal.NATIVE, 5)
        Appodeal.setUserId(userSession.user?.id ?: "123")
    }

    override fun onBackPressed() {
        val currentFragment = my_nav_host_fragment.findNavController().currentDestination?.label
        if (currentFragment == "fragment_news" || currentFragment == "GroupListFragment") {
            if (doubleBackToExitPressedOnce) {
                ExitActivity.exitApplication(this)
                return
            }
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
            exitHandler = Handler(Looper.getMainLooper())
            exitHandler?.postDelayed(exitFlag, EXIT_DELAY)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        exitHandler?.removeCallbacks(exitFlag)
        super.onDestroy()
    }


}