package com.sap.codelab.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sap.codelab.AppDependencies
import com.sap.codelab.repository.Repository

/**
 * Re-registers geofences for all active location-reminder memos after a device reboot.
 *
 * Enqueued by [com.sap.codelab.receiver.BootCompletedReceiver] on
 * [android.content.Intent.ACTION_BOOT_COMPLETED].
 */
internal class RestoreGeofencesWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RestoreGeofences", "RestoreGeofencesWorker started")

        val memos = runCatching { Repository.getActiveLocationReminders() }
            .getOrElse { return Result.retry() }

        memos.forEach { memo ->
            runCatching {
                // Coordinates are stored as Long (Double.toBits) in the DB; convert back before use
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
