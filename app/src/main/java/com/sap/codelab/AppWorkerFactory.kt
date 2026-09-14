package com.sap.codelab

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.notification.MemoNotificationManager
import com.sap.codelab.core.repository.IMemoRepository
import com.sap.codelab.core.worker.LocationReminderTriggeredWorker
import com.sap.codelab.core.worker.RestoreLocationRemindersWorker

internal class AppWorkerFactory(
    private val repository: IMemoRepository,
    private val locationReminderManager: LocationReminderManager,
    private val notificationManager: MemoNotificationManager
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = when (workerClassName) {
        LocationReminderTriggeredWorker::class.java.name ->
            LocationReminderTriggeredWorker(appContext, workerParameters, repository, notificationManager, locationReminderManager)
        RestoreLocationRemindersWorker::class.java.name ->
            RestoreLocationRemindersWorker(appContext, workerParameters, repository, locationReminderManager)
        else -> null
    }
}
