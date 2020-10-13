package com.intergroupapplication.presentation.base
import android.util.Log
import androidx.annotation.Nullable
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Created by abakarmagomedov on 31/07/2018 at project InterGroupApplication.
 */
class BaseCrashlyticsTree : Timber.Tree() {

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    override fun log(priority: Int, @Nullable tag: String?, @Nullable message: String, @Nullable t: Throwable?) {
        if (isDegugging(priority)) {
            return
        }

        Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority)
        Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag)
        Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message)

        if (t == null) {
            Crashlytics.logException(Exception(message))
        } else {
            Crashlytics.logException(t)
        }
    }

    private fun isDegugging(priority: Int): Boolean =
            isVerbose(priority) || isDebug(priority) || isInfo(priority)

    private fun isVerbose(priority: Int): Boolean =
            priority == Log.VERBOSE


    private fun isDebug(priority: Int): Boolean =
            priority == Log.DEBUG


    private fun isInfo(priority: Int): Boolean =
            priority == Log.INFO

}
