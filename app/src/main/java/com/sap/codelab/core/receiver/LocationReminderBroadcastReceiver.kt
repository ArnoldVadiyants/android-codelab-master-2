package com.sap.codelab.core.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sap.codelab.AppDependencies
import com.sap.codelab.core.utils.KEY_MEMO_ID
import com.sap.codelab.core.worker.LocationReminderTriggeredWorker

/**
 * Receives location reminder broadcasts and dispatches background work.
 */
internal class LocationReminderBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val REQUEST_CODE = 1001
        private const val ACTION_LOCATION_REMINDER_EVENT = "com.sap.codelab.ACTION_LOCATION_REMINDER_EVENT"

        fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context.applicationContext, LocationReminderBroadcastReceiver::class.java).apply {
                action = ACTION_LOCATION_REMINDER_EVENT
            }
            // FLAG_MUTABLE is required on Android 12+ because GMS mutates the PendingIntent to
            // attach the geofencing extras before broadcasting it
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context.applicationContext, REQUEST_CODE, intent, flags)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("LocationReminderReceiver", "Location reminder broadcast received for ${intent.action}")
        when (intent.action) {
            ACTION_LOCATION_REMINDER_EVENT -> handleLocationReminderEvent(context, intent)
            else -> Log.w("LocationReminderReceiver", "Unknown action received: ${intent.action}")
        }
    }

    private fun handleLocationReminderEvent(context: Context, intent: Intent) {
        val memoIds = AppDependencies.locationEventParser.parseTriggeringMemoIds(intent)
        if (memoIds.isEmpty()) return

        val workManager = WorkManager.getInstance(context)
        Log.d("LocationReminderReceiver", "Triggered memo ids: $memoIds")
        memoIds.forEach { memoId ->
            // KEEP ensures a duplicate broadcast for the same memo enqueues at most one worker
            workManager.enqueueUniqueWork(memoId.toString(), ExistingWorkPolicy.KEEP, buildWorkRequest(memoId))
        }
    }

    private fun buildWorkRequest(memoId: Long) =
        OneTimeWorkRequestBuilder<LocationReminderTriggeredWorker>()
            .setInputData(workDataOf(KEY_MEMO_ID to memoId))
            .build()
}
