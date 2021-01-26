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
import com.intergroupapplication.initializators.Initializer
import com.intergroupapplication.initializators.InitializerLocal
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
    lateinit var initializerAppodeal: InitializerLocal

    lateinit var compositeDisposable: CompositeDisposable

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        initializerAppodeal.initialize()
        setContentView(R.layout.activity_main)
        compositeDisposable = CompositeDisposable()
        val filepatch = Environment.getExternalStorageDirectory().path+"/RxPaparazzo/"
        val file = File("$filepatch.nomedia")
        try {
            file.createNewFile()
        } catch (e: IOException) {
            Timber.e(e)
        }
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