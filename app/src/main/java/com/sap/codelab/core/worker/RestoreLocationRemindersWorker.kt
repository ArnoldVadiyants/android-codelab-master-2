package com.sap.codelab.core.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sap.codelab.AppDependencies
import com.sap.codelab.core.repository.Repository

/**
 * Re-registers geofences for all active location-reminder memos after a device reboot.
 *
 * Enqueued by [com.sap.codelab.core.receiver.LocationReminderRestoreReceiver] on
 * [android.content.Intent.ACTION_BOOT_COMPLETED].
 */
internal class RestoreLocationRemindersWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("RestoreLocationReminders", "RestoreLocationRemindersWorker started")

        val memos = runCatching { Repository.getActiveLocationReminders() }
            .getOrElse { return Result.retry() }

        memos.forEach { memo ->
            runCatching {
                val location = memo.reminderLocation ?: return@runCatching
                AppDependencies.locationReminderManager.addReminder(
                    memoId = memo.id,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            }
            // Individual failures are swallowed so one bad entry doesn't block the rest
        }

        return Result.success()
    }
}
