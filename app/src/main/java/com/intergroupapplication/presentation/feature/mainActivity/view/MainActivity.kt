package com.intergroupapplication.presentation.feature.mainActivity.view

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.materialdrawerkt.builders.drawer
import com.android.billingclient.api.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.intergroupapplication.R
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.di.qualifier.DashDateFormatter
import com.intergroupapplication.domain.exception.*
import com.intergroupapplication.initializators.ErrorHandlerInitializer
import com.intergroupapplication.initializators.InitializerLocal
import com.intergroupapplication.presentation.base.ImageUploadingState
import com.intergroupapplication.presentation.customview.AvatarImageUploadingView
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.factory.ViewModelFactory
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.feature.mainActivity.adapter.NavigationAdapter
import com.intergroupapplication.presentation.feature.mainActivity.other.NavigationEntity
import com.intergroupapplication.presentation.feature.mainActivity.viewModel.MainActivityViewModel
import com.intergroupapplication.presentation.feature.mediaPlayer.IGMediaService
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo
import com.mikepenz.materialdrawer.Drawer
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
import java.text.DateFormat
import java.util.*
import java.util.Calendar.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.coroutines.suspendCoroutine

class MainActivity : FragmentActivity() {

    companion object {
        private const val DISABLE_ADS_ID = "disable_ads"
        const val MEDIA_CHANNEL_ID = "IGMediaChannel"
        const val EXIT_DELAY = 2000L
    }

    @Inject
    lateinit var modelFactory: ViewModelFactory

    private val viewModel: MainActivityViewModel by viewModels { modelFactory }

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

    @Inject
    @DashDateFormatter
    lateinit var dateFormatter: DateFormat

    lateinit var drawer: Drawer

    private val navigationAdapter = NavigationAdapter()

    lateinit var profileAvatarHolder: AvatarImageUploadingView

    private lateinit var navController: NavController

    private lateinit var lifecycleDisposable: CompositeDisposable

    private var lastUploadedAvatar: String? = null

    /**
     *  Billing
     */
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { _, purchases ->
            val disableAdsPurchase = purchases?.find { it.sku == DISABLE_ADS_ID }
            disableAdsPurchase?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = handlePurchase(disableAdsPurchase)
                    withContext(Main) {
                        Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                    if (result?.responseCode == BillingClient.BillingResponseCode.OK) {
                        withContext(Main) {
                            Toast.makeText(
                                this@MainActivity, "BILLING RESPONCE OK",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    if (disableAdsPurchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        withContext(Main) {
                            Toast.makeText(
                                this@MainActivity, "DISABLE ADS SUBSCRIBED",
                                Toast.LENGTH_LONG
                            ).show()
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
        lifecycleDisposable = CompositeDisposable()
        //Appodeal.setTesting(true)
        viewModel = ViewModelProvider(this, modelFactory)[MainActivityViewModel::class.java]
        initializerAppodeal.initialize()
        setTheme(R.style.ActivityTheme)
        setContentView(R.layout.activity_main)
        try {
            val filePatch = externalCacheDir?.path
            val file = File("/$filePatch.nomedia")
            if (!file.exists())
                file.createNewFile()
        } catch (e: IOException) {
            Timber.e(e)
        }
        viewModel.getAdCount()
        createNotificationChannel()
        initBilling()
        initErrorHandler(errorHandler)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun createDrawer() {
        navController.navigate(R.id.action_global_newsFragment2)
        val viewDrawer = layoutInflater.inflate(
            R.layout.layout_left_drawer_header,
            findViewById(R.id.navigationCoordinator),
            false
        )
        val navRecycler = viewDrawer.findViewById<RecyclerView>(R.id.navigationRecycler)
        navRecycler.adapter = navigationAdapter
        navRecycler.itemAnimator = null
        navigationAdapter.items = listOf(
            NavigationEntity(R.string.news, R.drawable.ic_bell_menu, {
                navController.navigate(R.id.action_global_newsFragment2)
                drawer.closeDrawer()
            }, checked = true),
            NavigationEntity(R.string.groups, R.drawable.ic_groups, {
                navController.navigate(R.id.action_global_groupListFragment2)
                drawer.closeDrawer()
            }),
            NavigationEntity(R.string.buy_premium, R.drawable.icon_like, {
                bill()
                drawer.closeDrawer()
            }, null),
            NavigationEntity(R.string.logout, 0, {
                userSession.logout()
                navController.navigate(R.id.action_global_loginActivity)
                drawer.closeDrawer()
            }, null),
        )
        profileAvatarHolder =
            viewDrawer.findViewById<AvatarImageUploadingView>(R.id.profileAvatarHolder)
        profileAvatarHolder.imageLoaderDelegate = imageLoadingDelegate
        drawer = drawer {
            fullscreen = false
            sliderBackgroundColorRes = R.color.mainBlack
            headerView = viewDrawer
            translucentStatusBar = true
            profileAvatarHolder.setOnClickListener {
                when (profileAvatarHolder.state) {
                    AvatarImageUploadingView.AvatarUploadingState.UPLOADED -> {
                        dialogDelegate.showDialog(
                            R.layout.dialog_camera_or_gallery,
                            mapOf(
                                R.id.fromCamera to { loadFromCamera() },
                                R.id.fromGallery to { loadFromGallery() })
                        )
                    }
                    AvatarImageUploadingView.AvatarUploadingState.ERROR -> {
                        lastUploadedAvatar?.let {
                            viewModel.uploadImageFromGallery(it)
                        }
                    }
                    AvatarImageUploadingView.AvatarUploadingState.NONE -> {
                        dialogDelegate.showDialog(
                            R.layout.dialog_camera_or_gallery,
                            mapOf(
                                R.id.fromCamera to { loadFromCamera() },
                                R.id.fromGallery to { loadFromGallery() })
                        )
                    }
                    else -> {
                    }
                }
            }
        }.apply {
            viewDrawer.findViewById<ImageView>(R.id.drawerArrow)
                .setOnClickListener { closeDrawer() }
        }
        lifecycleDisposable.add(viewModel.getUserProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userEntity ->
                val date = dateFormatter.parse(userEntity.birthday)
                val current = Date()
                date?.let {
                    viewDrawer.findViewById<TextView>(R.id.ageText).text =
                        getString(R.string.years, getDiffYears(it, current).toString())
                }
                when (userEntity.gender) {
                    "male" ->
                        viewDrawer.findViewById<TextView>(R.id.ageText)
                            .setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_male,
                                0,
                                0,
                                0
                            )
                    "female" ->
                        viewDrawer.findViewById<TextView>(R.id.ageText)
                            .setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_male,
                                0,
                                0,
                                0
                            )
                }
                viewDrawer.findViewById<TextView>(R.id.profileName).text = userEntity.firstName
                viewDrawer.findViewById<TextView>(R.id.profileSurName).text = userEntity.surName
                viewDrawer.findViewById<TextView>(R.id.countPublicationsTxt).text =
                    userEntity.stats.posts.toString()
                viewDrawer.findViewById<TextView>(R.id.countCommentsTxt).text =
                    userEntity.stats.comments.toString()
                viewDrawer.findViewById<TextView>(R.id.countDislikesTxt).text =
                    userEntity.stats.dislikes.toString()
                viewDrawer.findViewById<TextView>(R.id.countLikesTxt).text =
                    userEntity.stats.likes.toString()
                doOrIfNull(userEntity.avatar,
                    { profileAvatarHolder.showAvatar(it) },
                    { profileAvatarHolder.showAvatar(R.drawable.application_logo) })
            }, {
                profileAvatarHolder.showImageUploadingError()
                errorHandler.handle(it)
            })
        )
        lifecycleDisposable.add(
            viewModel.imageUploadingState
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    profileAvatarHolder.imageState = it
                }, {
                    errorHandler.handle(it)
                    profileAvatarHolder.imageState =
                        ImageUploadingState.ImageUploadingError(exception = it)
                })
        )
    }

    override fun onStart() {
        super.onStart()
        dialogDelegate.coordinator = this.findViewById(R.id.navigationCoordinator)
    }

    override fun onResume() {
        super.onResume()
        settingDeepLink()
    }


    private fun settingDeepLink() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData: PendingDynamicLinkData? ->
                val deepLink: Uri? = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    val postId = deepLink.toString().substringAfterLast("/")
                    val data = bundleOf(CommentsDetailsFragment.POST_ID to postId)
                    val commentsDetailsFragment = CommentsDetailsFragment()
                    commentsDetailsFragment.arguments = data
                    try {
                        if (userSession.isLoggedIn())
                            findNavController(R.id.my_nav_host_fragment)
                                .navigate(R.id.commentsDetailsActivity, data)
                    } catch (e: Exception) {
                        Timber.tag("tut_error").e(e)
                    }
                }
            }
            .addOnFailureListener(this) { e -> e.printStackTrace() }
    }

    override fun onStop() {
        super.onStop()
        dialogDelegate.coordinator = null
    }

    suspend fun bindMediaService(): IGMediaService.ServiceBinder? {
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
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MEDIA_CHANNEL_ID, "Media player",
                NotificationManager.IMPORTANCE_HIGH
            )
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

    private fun initBilling() {
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
                billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
                    skuDetails = skuDetailsList ?: listOf()
                    // Process the result.
                }
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase): BillingResult? {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                return withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
            }
        }
        return null
    }

    private fun bill() {
        val disableAdsSubscriptionSku = skuDetails.find { it.sku == DISABLE_ADS_ID }
        disableAdsSubscriptionSku?.let { skuDet ->
            BillingFlowParams.newBuilder()
                .setSkuDetails(skuDet)
                .build()
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
            GroupAlreadyFollowingException::class.java to Action { _, _ -> },
            PageNotFoundException::class.java to
                    Action { throwable, _ ->
                        dialogDelegate.showErrorSnackBar((throwable as PageNotFoundException).message.orEmpty())
                    },
            UnknownServerException::class.java to Action { _, _ ->
                createSnackBarAction(R.string.unknown_error)
            },
            ConnectException::class.java to Action { _, _ ->
                dialogDelegate.showErrorSnackBar(this.getString(R.string.no_network_connection))
            }
        )

        errorHandlerInitializer.initializeErrorHandler(
            errorMap,
            createSnackBarAction(R.string.unknown_error)
        )
    }

    private fun getActionForBlockedImei() = Action { _, _ ->
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

    private fun actionForBlockedUser() = Action { _, _ ->
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
        compositeDisposable.add(
            RxPaparazzo.single(this)
                .crop(cropOptions)
                .usingCamera()
                .map { response ->
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
            .map { response ->
                response.data()?.file?.path
            }
            .filter { it.isNotEmpty() }
            .subscribe({
                lastUploadedAvatar = it
                it?.let {
                    viewModel.uploadImageFromGallery(it)
                }
            }, {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
            })
        )
    }

    private fun getDiffYears(first: Date, last: Date): Int {
        val a: Calendar = getCalendar(first)
        val b: Calendar = getCalendar(last)
        var diff: Int = b.get(YEAR) - a.get(YEAR)
        if (a.get(MONTH) > b.get(MONTH) ||
            a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE)
        ) {
            diff--
        }
        return diff
    }

    private fun getCalendar(date: Date): Calendar {
        val cal: Calendar = getInstance(Locale.US)
        cal.time = date
        return cal
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleDisposable.dispose()
    }
}
