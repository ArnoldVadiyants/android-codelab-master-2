package com.sap.codelab

import android.content.Context
import com.sap.codelab.location.GoogleLocationReminderManager
import com.sap.codelab.location.LocationReminderManager
import com.sap.codelab.notification.AndroidMemoNotificationManager
import com.sap.codelab.notification.MemoNotificationManager

internal object AppDependencies {

    lateinit var locationReminderManager: LocationReminderManager
        private set

    lateinit var notificationManager: MemoNotificationManager
        private set

    fun initialize(context: Context) {
        locationReminderManager = GoogleLocationReminderManager(context)
        notificationManager = AndroidMemoNotificationManager(context)
    }
}
