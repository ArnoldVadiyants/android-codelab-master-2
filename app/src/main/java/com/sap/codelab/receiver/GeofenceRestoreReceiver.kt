package com.sap.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sap.codelab.worker.RestoreGeofencesWorker

/**
 * Re-registers active geofences after the device reboots or the app is updated.
 *
 * GMS geofences do not survive a device reboot or an app update. This receiver listens for
 * [android.content.Intent.ACTION_BOOT_COMPLETED] and [android.content.Intent.ACTION_MY_PACKAGE_REPLACED]
 * and enqueues a [com.sap.codelab.worker.RestoreGeofencesWorker] to restore all active location reminders.
 */
internal class GeofenceRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeofenceRestoreReceiver", "Received: ${intent.action}")
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<RestoreGeofencesWorker>().build())
    }
}
