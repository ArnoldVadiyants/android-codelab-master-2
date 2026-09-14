package com.sap.codelab.core.model

import com.sap.codelab.core.location.LatLng

/**
 * Represents a memo.
 */
internal data class Memo(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val reminderDate: Long = 0L,
    val reminderLatitude: Double = 0.0,
    val reminderLongitude: Double = 0.0,
    val isDone: Boolean = false
) {
    val hasLocationReminder: Boolean get() = reminderLatitude != 0.0 || reminderLongitude != 0.0

    val reminderLocation: LatLng?
        get() = if (hasLocationReminder) LatLng(reminderLatitude, reminderLongitude) else null
}
