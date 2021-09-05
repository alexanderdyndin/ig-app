package com.intergroupapplication.di.module

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import com.intergroupapplication.device.notification.CreatorType
import com.intergroupapplication.device.notification.NotificationTypes
import com.intergroupapplication.device.notification.actions.NotificationAction
import com.intergroupapplication.device.notification.actions.OnNewCommentAction
import com.intergroupapplication.device.notification.notificationcreators.NewCommentNotificationCreator
import com.intergroupapplication.device.notification.notificationcreators.NotificationCreator
import com.intergroupapplication.di.key.NotificationKey
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

/**
 * Created by abakarmagomedov on 04/09/2018 at project InterGroupApplication.
 */
@Module
class NotificationModule {

    @Provides
    @IntoMap
    @NotificationKey(NotificationTypes.NEW_COMMENT)
    fun provideNewCommentAction(notificationManager: NotificationManager,
                                creator: @JvmSuppressWildcards NotificationCreator<CreatorType.Comment>,
                                context: Context): NotificationAction<RemoteMessage> =
            OnNewCommentAction(creator, notificationManager, context)

    @Provides
    fun provideNewCommentCreator(context: Context,
                                 notificationManager: NotificationManager): NotificationCreator<CreatorType.Comment> =
            NewCommentNotificationCreator(context, notificationManager)
}
