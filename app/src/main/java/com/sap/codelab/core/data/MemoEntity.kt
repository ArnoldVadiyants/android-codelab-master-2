package com.sap.codelab.core.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memo")
internal data class MemoEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String = "",
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "reminderDate")
    val reminderDate: Long = 0L,
    @ColumnInfo(name = "reminderLatitude")
    val reminderLatitude: Double = 0.0,
    @ColumnInfo(name = "reminderLongitude")
    val reminderLongitude: Double = 0.0,
    @ColumnInfo(name = "isDone")
    val isDone: Boolean = false
)
