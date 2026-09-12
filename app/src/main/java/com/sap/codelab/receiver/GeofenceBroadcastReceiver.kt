package com.sap.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.KEY_MEMO_ID
import com.sap.codelab.worker.GeofenceTriggeredWorker

internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ARNOLD", "Geofence broadcast received for ${intent.action}")
        val event = GeofencingEvent.fromIntent(intent) ?: return

        Log.d("ARNOLD", "Geofence event: $event")
        if (event.hasError()) return
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return
        val geofences = event.triggeringGeofences ?: return

        val workManager = WorkManager.getInstance(context)
        Log.d("ARNOLD", "Geofences: $geofences")
        geofences.forEach { geofence ->
            val memoId = geofence.requestId.toLongOrNull() ?: return@forEach

            val request = OneTimeWorkRequestBuilder<GeofenceTriggeredWorker>()
                .setInputData(workDataOf(KEY_MEMO_ID to memoId))
                .build()

            // KEEP ensures a duplicate broadcast for the same memo enqueues at most one worker
            workManager.enqueueUniqueWork(memoId.toString(), ExistingWorkPolicy.KEEP, request)
        }
    }
}
