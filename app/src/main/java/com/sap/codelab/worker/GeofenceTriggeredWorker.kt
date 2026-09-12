package com.sap.codelab.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sap.codelab.AppDependencies
import com.sap.codelab.KEY_MEMO_ID
import com.sap.codelab.repository.Repository

internal class GeofenceTriggeredWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ARNOLD", "Geofence triggered worker")
        val memoId = inputData.getLong(KEY_MEMO_ID, -1L)
        if (memoId == -1L) return Result.failure()

        val memo = runCatching { Repository.getMemoById(memoId) }.getOrNull()
            ?: return Result.success() // Memo deleted before we arrived

        if (memo.isDone) return Result.success() // Already processed or marked done by user

        // Location was cleared after this event was enqueued
        if (memo.reminderLatitude == 0L || memo.reminderLongitude == 0L) return Result.success()

        AppDependencies.notificationManager.showLocationReminder(memo)

        runCatching { Repository.saveMemo(memo.copy(isDone = true)) }
        runCatching { AppDependencies.locationReminderManager.removeReminder(memoId) }

        return Result.success()
    }
}
