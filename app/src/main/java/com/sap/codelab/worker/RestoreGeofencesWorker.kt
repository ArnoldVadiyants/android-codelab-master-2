package com.sap.codelab.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sap.codelab.AppDependencies
import com.sap.codelab.repository.Repository

internal class RestoreGeofencesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ARNOLD", "RestoreGeofencesWorker started")

        val memos = runCatching { Repository.getActiveLocationReminders() }
            .getOrElse { return Result.retry() }

        memos.forEach { memo ->
            runCatching {
                AppDependencies.locationReminderManager.addReminder(
                    memoId = memo.id,
                    latitude = Double.fromBits(memo.reminderLatitude),
                    longitude = Double.fromBits(memo.reminderLongitude)
                )
            }
            // Individual failures are swallowed so one bad entry doesn't block the rest
        }

        return Result.success()
    }
}
