package com.sap.codelab.repository

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.room.Room
import com.sap.codelab.model.Memo

private const val DATABASE_NAME: String = "codelab"

/**
 * The repository is used to retrieve data from a data source.
 */
internal object Repository : IMemoRepository {

    private lateinit var database: Database

    fun initialize(applicationContext: Context) {
        database = Room.databaseBuilder(applicationContext, Database::class.java, DATABASE_NAME).build()
    }

    @WorkerThread
    override fun saveMemo(memo: Memo): Long = database.getMemoDao().insert(memo)

    @WorkerThread
    override fun getOpen(): List<Memo> = database.getMemoDao().getOpen()

    @WorkerThread
    override fun getAll(): List<Memo> = database.getMemoDao().getAll()

    @WorkerThread
    override fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id)

    @WorkerThread
    override fun getActiveLocationReminders(): List<Memo> = database.getMemoDao().getActiveLocationReminders()
}