package com.intergroupapplication.di.key

import com.intergroupapplication.device.notification.NotificationTypes
import dagger.MapKey

/**
 * Created by abakarmagomedov on 04/09/2018 at project InterGroupApplication.
 */
@MapKey
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotificationKey(val value: NotificationTypes)