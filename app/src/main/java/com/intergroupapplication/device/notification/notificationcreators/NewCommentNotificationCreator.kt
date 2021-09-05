package com.intergroupapplication.device.notification.notificationcreators

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.intergroupapplication.R
import com.intergroupapplication.device.notification.CreatorType
import com.intergroupapplication.device.notification.NotificationCreatorOptions
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment.Companion.COMMENT_ID
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment.Companion.COMMENT_PAGE
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment.Companion.POST_ID
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity

/**
 * Created by abakarmagomedov on 27/09/2018 at project InterGroupApplication.
 */
class NewCommentNotificationCreator constructor(
    private val context: Context,
    notificationManager: NotificationManager
) : AbstractNotificationCreator<CreatorType.Comment>(context, notificationManager) {

    companion object {
        private const val COMMENTS_NOTIFICATION_ID = 0
    }

    override fun create(type: CreatorType.Comment): Notification {

        val notificationCreatorOptions = NotificationCreatorOptions.Builder()
            .autoCancel(true)
            .contentTitle(context.getString(R.string.response_to_comment))
            .contentText(type.notificationMessage)
            .lightColor(Color.WHITE)
            .sound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .contentIntent(createPendingIntent(type))
            .priority(NotificationCompat.PRIORITY_MAX)
            .lights(Color.RED)
            .smallIcon(R.drawable.avatar_fill)
            .largeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.avatar_fill))
            .color(ContextCompat.getColor(context, R.color.colorPrimary))
            .vibrationPattern(longArrayOf(500, 500))
            .build()

        return createDefaultNotification(notificationCreatorOptions)
    }

    override fun getId(): Int = COMMENTS_NOTIFICATION_ID

    private fun createPendingIntent(type: CreatorType.Comment): PendingIntent {

        val bundle = bundleOf(
            POST_ID to type.postId,
            COMMENT_PAGE to type.page,
            COMMENT_ID to type.commentId
        )

        return NavDeepLinkBuilder(context)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.commentsDetailsActivity)
            .setArguments(bundle)
            .createPendingIntent()
    }
}
