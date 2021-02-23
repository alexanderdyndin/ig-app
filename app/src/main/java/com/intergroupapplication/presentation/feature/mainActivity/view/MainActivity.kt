package com.intergroupapplication.presentation.feature.mainActivity.view


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.*
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.intergroupapplication.R
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.feature.ExitActivity
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class MainActivity : FragmentActivity() {

    companion object {
        const val MEDIA_CHANNEL_ID = "IGMediaChannel"
        const val MEDIA_FILE_URI = "MediaFileUri"
        const val EXIT_DELAY = 2000L
    }

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainActivityViewModel

    @Inject
    lateinit var initializerAppodeal: InitializerLocal

    private var doubleBackToExitPressedOnce = false


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
        viewModel.getAdCount()
        createNotificationChannel()
    }

    override fun onResume() {
        super.onResume()
        //todo починить проверку новых версий
        //viewModel.checkNewVersionAvaliable(supportFragmentManager)
    }

    //override fun onBackPressed() {
//        val currentFragment = my_nav_host_fragment.findNavController().currentDestination?.label
//        if (currentFragment == "fragment_news" || currentFragment == "GroupListFragment") {
//            if (doubleBackToExitPressedOnce) {
//                ExitActivity.exitApplication(this)
//                return
//            }
//            this.doubleBackToExitPressedOnce = true
//            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
//            exitHandler = Handler(Looper.getMainLooper())
//            exitHandler?.postDelayed(exitFlag, EXIT_DELAY)
//        } else {
            //super.onBackPressed()
        //}
//    }

//    override fun onDestroy() {
//        exitHandler?.removeCallbacks(exitFlag)
//        super.onDestroy()
//    }


    suspend fun bindMediaService(mediaUrl: String):IGMediaService.ServiceBinder? {
        return suspendCoroutine {
            /**
             * Create our connection to the service to be used in our bindService call.
             */
            val connection = object : ServiceConnection {
                override fun onServiceDisconnected(name: ComponentName?) {

                }

                /**
                 * Called after a successful bind with our VideoService.
                 */
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    //We expect the service binder to be the video services binder.
                    //As such we cast.
                    if (service is IGMediaService.ServiceBinder) {
                        //Then we simply set the exoplayer instance on this view.
                        //Notice we are only getting information.
                        it.resumeWith(Result.success(service))
                    }
                }

            }

            val intent = Intent(this, IGMediaService::class.java)
            intent.putExtra(IGMediaService.MEDIA_URL, mediaUrl)
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(MEDIA_CHANNEL_ID, "Media player",
                    NotificationManager.IMPORTANCE_HIGH)
            val attrs = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                    .build()
            channel.setSound(null, attrs)

            channel.description = "Background notification for video and audio player"

            notificationManager.createNotificationChannel(channel)
        }
    }
}