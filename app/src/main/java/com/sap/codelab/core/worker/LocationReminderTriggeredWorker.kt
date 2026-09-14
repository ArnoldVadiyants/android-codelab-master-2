package com.sap.codelab.core.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.notification.MemoNotificationManager
import com.sap.codelab.core.repository.IMemoRepository
import com.sap.codelab.core.utils.KEY_MEMO_ID

/**
 * Handles a location reminder event for a single memo.
 *
 * Expects [KEY_MEMO_ID][KEY_MEMO_ID] in the input data. The worker:
 * 1. Loads the memo from the repository.
 * 2. Shows a location-reminder notification.
 * 3. Marks the memo as done and removes its geofence.
 */
internal class LocationReminderTriggeredWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: IMemoRepository,
    private val notificationManager: MemoNotificationManager,
    private val locationReminderManager: LocationReminderManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("LocationReminderWorker", "Location reminder triggered worker")
        val memoId = inputData.getLong(KEY_MEMO_ID, -1L)
        if (memoId == -1L) return Result.failure()

        val memo = runCatching { repository.getMemoById(memoId) }.getOrNull()
            ?: return Result.success() // Memo deleted before we arrived

        if (memo.isDone) return Result.success() // Already processed or marked done by user

        if (!memo.hasLocationReminder) return Result.success()

        notificationManager.showLocationReminder(memo)

        // Failures here are swallowed so the worker always returns success and is never retried.
        // A retry would re-show the notification (already sent above), spamming the user.
        runCatching { repository.saveMemo(memo.copy(isDone = true)) }
        runCatching { locationReminderManager.removeReminder(memoId) }

        return Result.success()
    }
}
