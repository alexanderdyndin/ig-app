package com.intergroupapplication.presentation.feature.mainActivity.view

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File
import java.io.IOException


import javax.inject.Inject

class MainActivity : FragmentActivity() {

    companion object {
        private const val EXIT_DELAY = 2000L
    }

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainActivityViewModel

    @Inject
    lateinit var initializerAppodeal: InitializerLocal

    private var exitHandler: Handler? = null

    private var doubleBackToExitPressedOnce = false

    val exitFlag = Runnable { this.doubleBackToExitPressedOnce = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        viewModel = ViewModelProvider(this, modelFactory)[MainActivityViewModel::class.java]
        initializerAppodeal.initialize()
        setContentView(R.layout.activity_main)
        val filepatch = Environment.getExternalStorageDirectory().path+"/RxPaparazzo/"
        val file = File("$filepatch.nomedia")
        try {
            if (!file.exists())
                file.createNewFile()
        } catch (e: IOException) {
            Timber.e(e)
        }
        viewModel.setAdCount()
    }

    override fun onResume() {
        super.onResume()
        //todo починить проверку новых версий
        //viewModel.checkNewVersionAvaliable(supportFragmentManager)
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