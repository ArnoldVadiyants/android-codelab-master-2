package com.sap.codelab.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Dao representation of a Memo.
 */
@Dao
internal interface MemoDao {

    /**
     * @return all memos that are currently in the database.
     */
    @Query("SELECT * FROM memo")
    suspend fun getAll(): List<MemoEntity>

    /**
     * @return all memos that are currently in the database and have not yet been marked as "done".
     */
    @Query("SELECT * FROM memo WHERE isDone = 0")
    suspend fun getOpen(): List<MemoEntity>

    /**
     * Inserts the given Memo into the database. We currently do not support updating of memos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: MemoEntity): Long

    /**
     * @return all memos that have a location and have not yet fired their location reminder.
     */
    @Query("SELECT * FROM memo WHERE isDone = 0 AND (reminderLatitude != 0 OR reminderLongitude != 0)")
    suspend fun getActiveLocationReminders(): List<MemoEntity>

    /**
     * @return the memo whose id matches the given id.
     */
    @Query("SELECT * FROM memo WHERE id = :memoId")
    suspend fun getMemoById(memoId: Long): MemoEntity
}
