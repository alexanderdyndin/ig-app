package com.intergroupapplication.device.notification.notificationcreators

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.intergroupapplication.R
import com.intergroupapplication.device.notification.NotificationCreatorOptions

/**
 * Created by abakarmagomedov on 26/09/2018 at project InterGroupApplication.
 */
abstract class AbstractNotificationCreator<in Type>(
    private val context: Context,
    private val notificationManager: NotificationManager?
) : NotificationCreator<Type> {

    protected fun createDefaultNotification(notificationCreatorOptions: NotificationCreatorOptions): Notification {
        createDefaultNotificationChanel(notificationCreatorOptions)
        val notificationBuilder =
            NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
                .setAutoCancel(notificationCreatorOptions.autoCancel)
                .setContentTitle(notificationCreatorOptions.contentTitle)
                .setContentText(notificationCreatorOptions.contentText)
                .setSound(notificationCreatorOptions.sound)
                .setContentIntent(notificationCreatorOptions.contentIntent)
                .setPriority(notificationCreatorOptions.priority)
                .setLights(notificationCreatorOptions.lights, 1000, 300)
                .setSmallIcon(notificationCreatorOptions.smallIcon ?: 0)
                .setLargeIcon(notificationCreatorOptions.largeIcon)
                .setColor(notificationCreatorOptions.color)
                .setVibrate(notificationCreatorOptions.vibrationPattern)

        return notificationBuilder.build()
    }

    private fun createDefaultNotificationChanel(notificationCreatorOptions: NotificationCreatorOptions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                context.getString(R.string.notification_channel_id),
                notificationCreatorOptions.channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = notificationCreatorOptions.channelDescription
                setShowBadge(true)
                enableLights(true)
                lightColor = notificationCreatorOptions.lightColor
                vibrationPattern = notificationCreatorOptions.vibrationPattern
                enableVibration(true)
            }.let {
                notificationManager?.createNotificationChannel(it)
            }
        }
    }
}
