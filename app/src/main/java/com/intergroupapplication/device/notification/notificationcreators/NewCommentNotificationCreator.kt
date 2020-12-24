package com.intergroupapplication.device.notification.notificationcreators

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.intergroupapplication.R
import com.intergroupapplication.device.notification.CreatorType
import com.intergroupapplication.device.notification.NotificationCreatorOptions
import com.intergroupapplication.presentation.feature.mainActivity.view.MainActivity

/**
 * Created by abakarmagomedov on 27/09/2018 at project InterGroupApplication.
 */
class NewCommentNotificationCreator constructor(private val context: Context, notificationManager: NotificationManager)
    : AbstractNotificationCreator<CreatorType.Comment>(context, notificationManager) {

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

    private fun createPendingIntent(type: CreatorType.Comment): PendingIntent? {
//        val commentIntent = createCommentIntent(type)
//        val groupIntent = createGroupIntent(type)
        //val navigationIntent = createNavigationIntent()

        val stackBuilder = TaskStackBuilder.create(context).apply {
            addParentStack(MainActivity::class.java)
            //todo rewrite it for fragments
            //addNextIntent(navigationIntent)
//            addNextIntent(groupIntent)
//            addNextIntent(commentIntent)
        }

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
//
//    private fun createCommentIntent(type: CreatorType.Comment) =
//            CommentsDetailsActivity.getIntent(context, type.groupId, type.commentId, type.postId)
//
//    private fun createGroupIntent(type: CreatorType.Comment): Intent =
//            GroupActivity.getIntent(context, type.groupId)
//
    //private fun createNavigationIntent(): Intent = MainActivity.getIntent(context)
}