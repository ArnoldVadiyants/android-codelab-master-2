package com.sap.codelab.receiver

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
import com.sap.codelab.KEY_MEMO_ID
import com.sap.codelab.worker.GeofenceTriggeredWorker

internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val REQUEST_CODE = 1001
        private const val ACTION_GEOFENCE_EVENT = "com.sap.codelab.ACTION_GEOFENCE_EVENT"

        fun createPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context.applicationContext, GeofenceBroadcastReceiver::class.java).apply {
                action = ACTION_GEOFENCE_EVENT
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            return PendingIntent.getBroadcast(context.applicationContext, REQUEST_CODE, intent, flags)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ARNOLD", "Geofence broadcast received for ${intent.action}")
        when (intent.action) {
            ACTION_GEOFENCE_EVENT -> handleGeofenceEvent(context, intent)
            else -> Log.w("ARNOLD", "Unknown action received: ${intent.action}")
        }
    }

    private fun handleGeofenceEvent(context: Context, intent: Intent) {
        val memoIds = AppDependencies.geofenceEventParser.parseTriggeringMemoIds(intent)
        if (memoIds.isEmpty()) return

        val workManager = WorkManager.getInstance(context)
        Log.d("ARNOLD", "Triggered memo ids: $memoIds")
        memoIds.forEach { memoId ->
            val request = OneTimeWorkRequestBuilder<GeofenceTriggeredWorker>()
                .setInputData(workDataOf(KEY_MEMO_ID to memoId))
                .build()

            // KEEP ensures a duplicate broadcast for the same memo enqueues at most one worker
            workManager.enqueueUniqueWork(memoId.toString(), ExistingWorkPolicy.KEEP, request)
        }
    }
}
