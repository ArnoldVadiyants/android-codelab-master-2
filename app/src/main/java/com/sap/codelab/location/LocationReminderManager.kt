package com.sap.codelab.location

/**
 * Manages geofence-based location reminders tied to memos.
 *
 * Abstracts the underlying Geofencing API
 */
internal interface LocationReminderManager {

    /**
     * Registers a geofence for the given memo.
     *
     * @param memoId    ID of the memo to associate with the geofence.
     * @param latitude  latitude of the reminder.
     * @param longitude longitude of the reminder.
     */
    suspend fun addReminder(memoId: Long, latitude: Double, longitude: Double)

    /**
     * Removes the geofence previously registered for the given memo.
     *
     * @param memoId ID of the memo whose geofence should be removed.
     */
    suspend fun removeReminder(memoId: Long)
}
