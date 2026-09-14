package com.sap.codelab.core.repository

import com.sap.codelab.core.model.Memo

/**
 * Interface for a repository offering memo related CRUD operations.
 */
internal interface IMemoRepository {

    /**
     * Saves the given memo to the database.
     * @return the row id of the saved memo.
     */
    suspend fun saveMemo(memo: Memo): Long

    /**
     * @return all memos currently in the database.
     */
    suspend fun getAll(): List<Memo>

    /**
     * @return all memos currently in the database, except those that have been marked as "done".
     */
    suspend fun getOpen(): List<Memo>

    /**
     * @return the memo whose id matches the given id.
     */
    suspend fun getMemoById(id: Long): Memo

    /**
     * @return all memos that have an active location reminder (have coordinates and are not done).
     */
    suspend fun getActiveLocationReminders(): List<Memo>
}
