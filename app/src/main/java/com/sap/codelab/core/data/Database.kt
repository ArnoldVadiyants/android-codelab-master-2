package com.sap.codelab.core.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The database that is used to store information.
 */
@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
internal abstract class Database : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao
}
