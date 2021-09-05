package com.intergroupapplication.device.notification

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.core.app.NotificationCompat

/**
 * Created by abakarmagomedov on 27/09/2018 at project InterGroupApplication.
 */
class NotificationCreatorOptions private constructor(
    val channelName: String,
    val channelDescription: String,
    val lightColor: Int,
    val autoCancel: Boolean,
    val contentTitle: String,
    val contentText: String,
    val sound: Uri,
    val contentIntent: PendingIntent?,
    val priority: Int,
    val lights: Int,
    val smallIcon: Int?,
    val largeIcon: Bitmap?,
    val color: Int,
    val vibrationPattern: LongArray
) {

    companion object {
        private const val CHANNEL_NAME = "FCM"
        private const val CHANNEL_DESC = "Firebase Cloud Messaging"
        private const val LIGHT_COLOR = Color.WHITE
        private const val AUTO_CANCEL = true
        private const val CONTENT_TITLE = ""
        private const val CONTENT_TEXT = ""
        private const val COLOR = Color.CYAN
        private const val PRIORITY = NotificationCompat.PRIORITY_MAX
        private const val LIGHTS = Color.RED
        private val SOUND = Uri.EMPTY
        private val CONTENT_INTENT = null
        private val SMALL_ICON = null
        private val LARGE_ICON = null
        private val VIBRATION_PATTER = longArrayOf(500, 500)
    }

    class Builder {
        private var channelName = CHANNEL_NAME
        private var channelDescription = CHANNEL_DESC
        private var lightColor = LIGHT_COLOR
        private var autoCancel: Boolean = AUTO_CANCEL
        private var contentTitle: String = CONTENT_TITLE
        private var contentText: String = CONTENT_TEXT
        private var sound: Uri = SOUND
        private var contentIntent: PendingIntent? = CONTENT_INTENT
        private var priority: Int = PRIORITY
        private var lights = LIGHTS
        private var smallIcon: Int? = SMALL_ICON
        private var largeIcon: Bitmap? = LARGE_ICON
        private var color: Int = COLOR
        private var vibrationPattern: LongArray = VIBRATION_PATTER

        fun channelDescription(channelDescription: String) =
            apply { this.channelDescription = channelDescription }

        fun lightColor(lightColor: Int) =
            apply { this.lightColor = lightColor }

        fun autoCancel(autoCancel: Boolean) =
            apply { this.autoCancel = autoCancel }

        fun contentTitle(contentTitle: String) =
            apply { this.contentTitle = contentTitle }

        fun contentText(contentText: String) =
            apply { this.contentText = contentText }

        fun sound(sound: Uri) = apply { this.sound = sound }

        fun contentIntent(contentIntent: PendingIntent?) =
            apply { this.contentIntent = contentIntent }

        fun priority(priority: Int) = apply { this.priority = priority }

        fun lights(lights: Int) = apply { this.lights = lights }

        fun smallIcon(smallIcon: Int?) = apply { this.smallIcon = smallIcon }

        fun largeIcon(largeIcon: Bitmap?) = apply { this.largeIcon = largeIcon }

        fun color(color: Int) = apply { this.color = color }

        fun vibrationPattern(vibrationPattern: LongArray) =
            apply { this.vibrationPattern = vibrationPattern }

        fun build() = NotificationCreatorOptions(
            channelName,
            channelDescription,
            lightColor,
            autoCancel,
            contentTitle,
            contentText,
            sound,
            contentIntent,
            priority,
            lights,
            smallIcon,
            largeIcon,
            color,
            vibrationPattern

        )
    }
}
