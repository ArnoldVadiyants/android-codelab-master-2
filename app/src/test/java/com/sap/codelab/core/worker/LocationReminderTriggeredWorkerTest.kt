package com.sap.codelab.core.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.notification.MemoNotificationManager
import com.sap.codelab.core.repository.IMemoRepository
import com.sap.codelab.core.utils.KEY_MEMO_ID
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocationReminderTriggeredWorkerTest {

    private lateinit var repository: IMemoRepository
    private lateinit var notificationManager: MemoNotificationManager
    private lateinit var locationManager: LocationReminderManager

    private fun buildWorker(memoId: Long): LocationReminderTriggeredWorker {
        val context = mockk<Context>(relaxed = true)
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.inputData } returns workDataOf(KEY_MEMO_ID to memoId)
        return LocationReminderTriggeredWorker(context, params, repository, notificationManager, locationManager)
    }

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        notificationManager = mockk(relaxed = true)
        locationManager = mockk(relaxed = true)
    }

    @Test
    fun `doWork returns failure when memoId input is missing`() = runTest {
        val result = buildWorker(memoId = -1L).doWork()
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `doWork returns success when memo has been deleted`() = runTest {
        coEvery { repository.getMemoById(any()) } throws RuntimeException("not found")
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success without notification when memo is already done`() = runTest {
        coEvery { repository.getMemoById(1L) } returns testMemo(isDone = true)
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
        verify(exactly = 0) { notificationManager.showLocationReminder(any()) }
    }

    @Test
    fun `doWork returns success without notification when memo has no location reminder`() = runTest {
        coEvery { repository.getMemoById(1L) } returns testMemo(lat = 0.0, lng = 0.0)
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
        verify(exactly = 0) { notificationManager.showLocationReminder(any()) }
    }

    @Test
    fun `doWork shows location reminder notification for active memo`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        verify { notificationManager.showLocationReminder(memo) }
    }

    @Test
    fun `doWork marks memo as done after notification`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        coVerify { repository.saveMemo(memo.copy(isDone = true)) }
    }

    @Test
    fun `doWork removes geofence after notification`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        coVerify { locationManager.removeReminder(memo.id) }
    }

    @Test
    fun `doWork returns success on the happy path`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } returns memo.id

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success even when saveMemo throws`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } throws RuntimeException("DB error")

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success even when removeReminder throws`() = runTest {
        val memo = testMemo()
        coEvery { repository.getMemoById(memo.id) } returns memo
        coEvery { repository.saveMemo(any()) } returns memo.id
        coEvery { locationManager.removeReminder(any()) } throws RuntimeException("GMS error")

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    private fun testMemo(
        id: Long = 1L,
        lat: Double = 52.0,
        lng: Double = 13.0,
        isDone: Boolean = false
    ) = Memo(
        id = id,
        title = "Test memo",
        description = "Description",
        reminderDate = 0L,
        reminderLatitude = lat,
        reminderLongitude = lng,
        isDone = isDone
    )
}
