package com.sap.codelab.core.repository

import android.content.Context
import androidx.room.Room
import com.sap.codelab.core.data.Database
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.model.toDomain
import com.sap.codelab.core.model.toEntity

private const val DATABASE_NAME: String = "codelab"

/**
 * The repository is used to retrieve data from a data source.
 */
internal object Repository : IMemoRepository {

    private lateinit var database: Database

    fun initialize(applicationContext: Context) {
        database = Room.databaseBuilder(applicationContext, Database::class.java, DATABASE_NAME).build()
    }

    override suspend fun saveMemo(memo: Memo): Long = database.getMemoDao().insert(memo.toEntity())

    override suspend fun getOpen(): List<Memo> = database.getMemoDao().getOpen().map { it.toDomain() }

    override suspend fun getAll(): List<Memo> = database.getMemoDao().getAll().map { it.toDomain() }

    override suspend fun getMemoById(id: Long): Memo = database.getMemoDao().getMemoById(id).toDomain()

    override suspend fun getActiveLocationReminders(): List<Memo> =
        database.getMemoDao().getActiveLocationReminders().map { it.toDomain() }
}
