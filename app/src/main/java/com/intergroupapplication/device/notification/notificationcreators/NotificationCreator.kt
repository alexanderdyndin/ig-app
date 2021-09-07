package com.intergroupapplication.device.notification.notificationcreators

import android.app.Notification

/**
 * Created by abakarmagomedov on 26/09/2018 at project InterGroupApplication.
 */
interface NotificationCreator<in Type> {
    fun create(type: Type): Notification
    fun getId(): Int
}
