package com.sap.codelab.location

internal interface LocationReminderManager {

    suspend fun addReminder(memoId: Long, latitude: Double, longitude: Double)

    suspend fun removeReminder(memoId: Long)
}
