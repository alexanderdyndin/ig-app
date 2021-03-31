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
import com.android.billingclient.api.*
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class MainActivity : FragmentActivity() {

    companion object {
        const val DISABLE_ADS_ID = "disable_ads"

        const val MEDIA_CHANNEL_ID = "IGMediaChannel"
        const val MEDIA_FILE_URI = "MediaFileUri"
        const val EXIT_DELAY = 2000L
    }

    private val TAG: String = "MainActivity"

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainActivityViewModel

    @Inject
    lateinit var initializerAppodeal: InitializerLocal

    @Inject
    lateinit var userSession: UserSession

    /**
     *  Billing
     */
    private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                val disableAdsPurchase = purchases?.find { it.sku == DISABLE_ADS_ID }
                disableAdsPurchase?.let{
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = handlePurchase(disableAdsPurchase)
                        withContext(Main) {
                            Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_LONG).show()
                        }
                        if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                            withContext(Main) {
                                Toast.makeText(this@MainActivity, "BILLING RESPONCE OK", Toast.LENGTH_LONG).show()
                            }
                        }
                        if (disableAdsPurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            withContext(Main) {
                                Toast.makeText(this@MainActivity, "DISABLE ADS SUBSCRIBED", Toast.LENGTH_LONG).show()
                            }
                            userSession.isAdEnabled = false
                        }
                    }
                }
            }

    private lateinit var billingClient: BillingClient
    private var skuDetails: List<SkuDetails> = listOf()

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
        initBilling()
    }

    override fun onResume() {
        super.onResume()
        //viewModel.checkNewVersionAvaliable(supportFragmentManager)
    }


    suspend fun bindMediaService():IGMediaService.ServiceBinder? {
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
            //intent.putExtra(IGMediaService.MEDIA_URL, mediaUrl)
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

    fun initBilling() {
        billingClient = BillingClient.newBuilder(applicationContext)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
                    restoreSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun restoreSubscriptions() {
        val purchases = billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
        val disableAdsPurchase = purchases?.find { it.sku == DISABLE_ADS_ID }
        val isDisableAdsActive = disableAdsPurchase?.isAutoRenewing ?: false
        userSession.isAdEnabled = !isDisableAdsActive
    }

    private fun querySkuDetails() {
        CoroutineScope(Main).launch {
            val skuList = ArrayList<String>()
            skuList.add("disable_ads")
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
            withContext(Dispatchers.IO) {
                billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                    skuDetails = skuDetailsList ?: listOf()
                    // Process the result.
                }
            }
        }
    }

    suspend fun handlePurchase(purchase: Purchase): BillingResult? {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                val ackPurchaseResult = withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
                return ackPurchaseResult
            }
        }
        return null
    }

    fun bill() {
        val disableAdsSubscriptionSku = skuDetails.find { it.sku == DISABLE_ADS_ID }
        disableAdsSubscriptionSku?.let { skuDet ->
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDet)
                    .build()
            val responseCode = billingClient.launchBillingFlow(this, flowParams).responseCode
        }
    }
}