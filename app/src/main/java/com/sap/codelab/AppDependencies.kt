package com.sap.codelab

import android.content.Context
import com.sap.codelab.core.location.GmsGeofenceEventParser
import com.sap.codelab.core.location.GoogleLocationReminderManager
import com.sap.codelab.core.location.ILocationEventParser
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.notification.AndroidMemoNotificationManager
import com.sap.codelab.core.notification.MemoNotificationManager

/**
 * Application-wide dependency container
 */
internal object AppDependencies {

    val locationEventParser: ILocationEventParser = GmsGeofenceEventParser()

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
