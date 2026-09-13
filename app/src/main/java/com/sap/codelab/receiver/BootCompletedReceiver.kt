package com.sap.codelab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sap.codelab.worker.RestoreGeofencesWorker

internal class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "Boot completed received for ${intent.action}")
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        WorkManager.getInstance(context)
            .enqueue(OneTimeWorkRequestBuilder<RestoreGeofencesWorker>().build())
    }
}
