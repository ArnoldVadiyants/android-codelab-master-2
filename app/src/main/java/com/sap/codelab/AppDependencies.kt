package com.sap.codelab

import android.content.Context
import com.sap.codelab.location.GmsGeofenceEventParser
import com.sap.codelab.location.GoogleLocationReminderManager
import com.sap.codelab.location.IGeofenceEventParser
import com.sap.codelab.location.LocationReminderManager
import com.sap.codelab.notification.AndroidMemoNotificationManager
import com.sap.codelab.notification.MemoNotificationManager

/**
 * Application-wide dependency container
 */
internal object AppDependencies {

    val geofenceEventParser: IGeofenceEventParser = GmsGeofenceEventParser()

    lateinit var locationReminderManager: LocationReminderManager
        private set

    lateinit var notificationManager: MemoNotificationManager
        private set

    /**
     * Creates and wires up all dependencies.
     */
    fun initialize(context: Context) {
        locationReminderManager = GoogleLocationReminderManager(context)
        notificationManager = AndroidMemoNotificationManager(context)
    }
}
