package com.sap.codelab

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkerFactory
import com.sap.codelab.core.location.GmsGeofenceEventParser
import com.sap.codelab.core.location.GoogleLocationReminderManager
import com.sap.codelab.core.location.ILocationEventParser
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.notification.AndroidMemoNotificationManager
import com.sap.codelab.core.notification.MemoNotificationManager
import com.sap.codelab.core.repository.IMemoRepository
import com.sap.codelab.core.repository.Repository

/**
 * Application-wide dependency container
 */
internal object AppDependencies {

    val repository: IMemoRepository get() = Repository

    val locationEventParser: ILocationEventParser = GmsGeofenceEventParser()

    lateinit var locationReminderManager: LocationReminderManager
        private set

    lateinit var notificationManager: MemoNotificationManager
        private set

    lateinit var viewModelFactory: ViewModelProvider.Factory
        private set

    lateinit var workerFactory: WorkerFactory
        private set

    /**
     * Creates and wires up all dependencies.
     */
    fun initialize(context: Context) {
        Repository.initialize(context)
        locationReminderManager = GoogleLocationReminderManager(context)
        notificationManager = AndroidMemoNotificationManager(context)
        viewModelFactory = AppViewModelFactory(repository, locationReminderManager)
        workerFactory = AppWorkerFactory(repository, locationReminderManager, notificationManager)
    }
}
