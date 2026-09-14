package com.sap.codelab.core.notification

import com.sap.codelab.core.model.Memo

/**
 * Posts user-visible notifications related to memo reminders.
 *
 * Abstracts the Android notification system so callers and workers are not coupled
 * to the concrete implementation.
 */
internal interface MemoNotificationManager {

    /**
     * Posts a notification informing the user that they have entered the location
     * associated with [memo].
     *
     * The notification displays the memo title and up to 140 characters of the description.
     * It is a no-op if the [android.Manifest.permission.POST_NOTIFICATIONS] permission has
     * not been granted on Android 13+.
     */
    fun showLocationReminder(memo: Memo)
}
