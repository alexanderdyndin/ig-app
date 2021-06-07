package com.intergroupapplication.presentation.feature.mainActivity.view


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.android.billingclient.api.*
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.domain.usecase.AvatarUploadingUseCase
import com.intergroupapplication.initializators.ErrorHandlerInitializer
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.workable.errorhandler.Action
import com.workable.errorhandler.ErrorHandler
import com.yalantis.ucrop.UCrop
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
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

    @Inject
    lateinit var errorHandler: ErrorHandler

    @Inject
    lateinit var errorHandlerInitializer: ErrorHandlerInitializer

    @Inject
    lateinit var dialogDelegate: DialogDelegate

    @Inject
    lateinit var imageLoadingDelegate: ImageLoadingDelegate

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var cropOptions: UCrop.Options

    lateinit var drawer: Drawer

    lateinit var profileAvatarHolder: AvatarImageUploadingView

    private lateinit var navController: NavController

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
        //Appodeal.setTesting(true)
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
        initErrorHandler(errorHandler)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        createDrawer()
    }

    fun createDrawer() {
        val viewDrawer = layoutInflater.inflate(R.layout.layout_profile_header, findViewById(R.id.navigationCoordinator), false)
        viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder).setOnClickListener {
            if (profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.UPLOADED
                || profileAvatarHolder.state == AvatarImageUploadingView.AvatarUploadingState.NONE) {
                dialogDelegate.showDialog(R.layout.dialog_camera_or_gallery,
                    mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
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
                        mapOf(R.id.fromCamera to { loadFromCamera() }, R.id.fromGallery to { loadFromGallery() }))
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
                    navController.navigate(R.id.action_global_newsFragment2)
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
                    navController.navigate(R.id.action_global_groupListFragment2)
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
                typeface = Typeface.createFromAsset(assets, "roboto.regular.ttf")
                selectable = false
                onClick { _ ->
                    bill()
                    false
                }
            }
            primaryItem(getString(R.string.logout)) {
                typeface = Typeface.createFromAsset(assets, "roboto.regular.ttf")
                textColorRes = R.color.whiteTextColor
                selectedColorRes = R.color.profileTabColor
                selectedTextColorRes = R.color.selectedItemTabColor
                onClick { _ ->
                    userSession.logout()
                    navController.navigate(R.id.action_global_loginActivity)
                    false
                }
            }
        }.apply {
            setSelection(drawerItem)
            viewDrawer.findViewById<ImageView>(R.id.drawerArrow).setOnClickListener { closeDrawer() }
        }
        compositeDisposable.add(viewModel.getUserProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userEntity ->
                val userName = userEntity.firstName + " " + userEntity.surName
                viewDrawer.findViewById<TextView>(R.id.profileName).text = userName
                doOrIfNull(userEntity.avatar,
                    { profileAvatarHolder.showAvatar(it) },
                    { profileAvatarHolder.showAvatar(R.drawable.application_logo) })
            }, {
                profileAvatarHolder.showImageUploadingError()
                errorHandler.handle(it)
            }))
        viewModel.imageUploadingState.observe(this, {
            profileAvatarHolder.imageState = it
        })
    }

    override fun onStart() {
        super.onStart()
        dialogDelegate.coordinator = this.findViewById(R.id.navigationCoordinator)
    }

    override fun onResume() {
        super.onResume()
        //viewModel.checkNewVersionAvaliable(supportFragmentManager)
    }

    override fun onStop() {
        super.onStop()
        dialogDelegate.coordinator = null
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

    private fun initErrorHandler(errorHandler: ErrorHandler) {
        errorHandler.clear()
        val errorMap = mapOf(
            BadRequestException::class.java to
                    Action { throwable, _ -> dialogDelegate.showErrorSnackBar((throwable as BadRequestException).message) },
            UserBlockedException::class.java to getActionForBlockedUser(),
            ServerException::class.java to
                    Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(R.string.server_error)) },
            NotFoundException::class.java to
                    Action { throwable, _ -> dialogDelegate.showErrorSnackBar((throwable as NotFoundException).message.orEmpty()) },
            UnknownHostException::class.java to createSnackBarAction(R.string.no_network_connection),
            CanNotUploadPhoto::class.java to createToast(R.string.can_not_change_avatar),
            CanNotUploadVideo::class.java to createToast(R.string.can_not_upload_video),
            CanNotUploadAudio::class.java to createToast(R.string.can_not_upload_audio),
            UserNotProfileException::class.java to openCreateProfile(),
            GroupBlockedException::class.java to getActionForBlockedGroup(),
            UserNotVerifiedException::class.java to openConfirmationEmail(),
            ForbiddenException::class.java to createSnackBarAction(R.string.forbidden_error),
            ImeiException::class.java to getActionForBlockedImei(),
            InvalidRefreshException::class.java to openAutorize(),
            GroupAlreadyFollowingException::class.java to Action { _, _ ->},
            PageNotFoundException::class.java to
                    Action { throwable, _ ->
                        dialogDelegate.showErrorSnackBar((throwable as PageNotFoundException).message.orEmpty())},
            UnknowServerException::class.java to Action { _, _ ->
                createSnackBarAction(R.string.unknown_error)},
            ConnectException::class.java to Action { _, _ ->
                dialogDelegate.showErrorSnackBar(this.getString(R.string.no_network_connection))
            }
        )

        errorHandlerInitializer.initializeErrorHandler(errorMap,
            createSnackBarAction(R.string.unknown_error))
    }

    private fun getActionForBlockedImei() = Action { throwable, _ ->
        userSession.clearAllData()
        finish()
    }

    private fun getActionForBlockedUser() =
        if (userSession.isLoggedIn()) {
            actionForBlockedUser()
        } else {
            createSnackBarAction(R.string.user_blocked)
        }


    private fun getActionForBlockedGroup() = actionForBlockedGroup

    private val actionForBlockedGroup = Action { _, _ ->
        dialogDelegate.showErrorSnackBar("Группа заблокирована")
        navController.popBackStack()
    }

    fun actionForBlockedUser() = Action { _, _ ->
        userSession.logout()
        finish()
    }

    private fun createSnackBarAction(message: Int) =
        Action { _, _ -> dialogDelegate.showErrorSnackBar(getString(message)) }

    private fun createToast(message: Int) =
        Action { _, _ -> Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show() }


    private fun openCreateProfile() = Action { _, _ ->
        Timber.e("403 catched")
        navController.navigate(R.id.action_global_createUserProfileActivity)
    }

    private fun openConfirmationEmail() = Action { _, _ ->
        val email = userSession.email?.email.orEmpty()
        val data = bundleOf("entity" to email)
        navController.navigate(R.id.action_global_confirmationMailActivity, data)
        Timber.e("403 catched")
    }

    private fun openAutorize() = Action { _, _ ->
        userSession.logout()
        navController.navigate(R.id.action_global_loginActivity)
    }

    private fun loadFromCamera() {
        compositeDisposable.add(RxPaparazzo.single(this)
            .crop(cropOptions)
            .usingCamera()
            .map {response ->
                response.data()?.file?.path
            }
            .filter { it.isNotEmpty() }
            .subscribe {
                it?.let {
                    viewModel.uploadImageFromGallery(it)
                }
            }
        )
    }

    private fun loadFromGallery() {
        compositeDisposable.add(RxPaparazzo.single(this)
            .crop(cropOptions)
            .usingGallery()
            .map {response ->
                response.data()?.file?.path
            }
            .filter { it.isNotEmpty() }
            .subscribe {
                it?.let {
                    viewModel.uploadImageFromGallery(it)
                }
            }
        )
    }
}