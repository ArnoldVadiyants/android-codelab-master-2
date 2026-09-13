package com.sap.codelab.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.sap.codelab.location.LatLng

/**
 * Represents a memo.
 */
@Entity(tableName = "memo")
internal data class Memo(
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        @ColumnInfo(name = "title")
        val title: String,
        @ColumnInfo(name = "description")
        val description: String,
        @ColumnInfo(name = "reminderDate")
        val reminderDate: Long,
        @ColumnInfo(name = "reminderLatitude")
        val reminderLatitude: Long,
        @ColumnInfo(name = "reminderLongitude")
        val reminderLongitude: Long,
        @ColumnInfo(name = "isDone")
        val isDone: Boolean = false
) {
    @get:Ignore
    val hasLocationReminder: Boolean get() = reminderLatitude != 0L || reminderLongitude != 0L

    // Coordinates are stored as Long via Double.toBits(); 0L is the sentinel for "no location set"
    @get:Ignore
    val reminderLocation: LatLng?
        get() = if (hasLocationReminder)
            LatLng(Double.fromBits(reminderLatitude), Double.fromBits(reminderLongitude))
        else null
}
