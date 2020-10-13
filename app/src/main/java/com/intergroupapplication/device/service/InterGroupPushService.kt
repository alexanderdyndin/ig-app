package com.intergroupapplication.device.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.intergroupapplication.data.model.DeviceModel
import com.intergroupapplication.data.repository.FbTokenRepository
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.device.notification.NotificationTypes
import com.intergroupapplication.device.notification.actions.NotificationAction
import com.intergroupapplication.domain.entity.FirebaseTokenEntity
import dagger.android.AndroidInjection
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 04/09/2018 at project InterGroupApplication.
 */
class InterGroupPushService : FirebaseMessagingService() {

    @Inject
    lateinit var messageActions: Map<NotificationTypes, @JvmSuppressWildcards NotificationAction<RemoteMessage>>

    @Inject
    lateinit var session: UserSession

    @Inject
    lateinit var fbTokenRepository: FbTokenRepository

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Timber.d("NOTIFICATION: ")
        Timber.d("From: ${message?.from}")
        Timber.d("Data payload: ${message?.data}")
        Timber.d("Notification body: ${message?.notification?.body}")

        //FIXME -> bug if it receives notification without group_id, comment_id etc.
        //temporary solution -> check if contains key group_id
        message?.takeIf { it.data.containsKey("group_id") }?.let { messageActions[NotificationTypes.NEW_COMMENT]?.proceed(it) }
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        val idUser = session.user?.id.orEmpty()
        //todo при релизе проверить
        token?.let { t ->
            fbTokenRepository.refreshToken(DeviceModel(token), idUser)
                    .subscribeOn(Schedulers.io())
                    .subscribe({},
                            { it.printStackTrace() })
            session.firebaseToken = FirebaseTokenEntity(t)
        }
    }
}
