package com.sap.codelab.worker

import android.content.Context
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.sap.codelab.AppDependencies
import com.sap.codelab.KEY_MEMO_ID
import com.sap.codelab.location.LocationReminderManager
import com.sap.codelab.model.Memo
import com.sap.codelab.notification.MemoNotificationManager
import com.sap.codelab.repository.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GeofenceTriggeredWorkerTest {

    private lateinit var notificationManager: MemoNotificationManager
    private lateinit var locationManager: LocationReminderManager

    private fun buildWorker(memoId: Long): GeofenceTriggeredWorker {
        val context = mockk<Context>(relaxed = true)
        val params = mockk<WorkerParameters>(relaxed = true)
        every { params.inputData } returns workDataOf(KEY_MEMO_ID to memoId)
        return GeofenceTriggeredWorker(context, params)
    }

    @Before
    fun setUp() {
        mockkObject(Repository)
        mockkObject(AppDependencies)
        notificationManager = mockk(relaxed = true)
        locationManager = mockk(relaxed = true)
        every { AppDependencies.notificationManager } returns notificationManager
        every { AppDependencies.locationReminderManager } returns locationManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `doWork returns failure when memoId input is missing`() = runTest {
        val result = buildWorker(memoId = -1L).doWork()
        assertEquals(Result.failure(), result)
    }

    @Test
    fun `doWork returns success when memo has been deleted`() = runTest {
        coEvery { Repository.getMemoById(any()) } throws RuntimeException("not found")
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success without notification when memo is already done`() = runTest {
        coEvery { Repository.getMemoById(1L) } returns testMemo(isDone = true)
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
        verify(exactly = 0) { notificationManager.showLocationReminder(any()) }
    }

    @Test
    fun `doWork returns success without notification when memo has no location reminder`() = runTest {
        // 0L coordinates → hasLocationReminder == false
        coEvery { Repository.getMemoById(1L) } returns testMemo(lat = 0L, lng = 0L)
        val result = buildWorker(memoId = 1L).doWork()
        assertEquals(Result.success(), result)
        verify(exactly = 0) { notificationManager.showLocationReminder(any()) }
    }

    @Test
    fun `doWork shows location reminder notification for active memo`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        verify { notificationManager.showLocationReminder(memo) }
    }

    @Test
    fun `doWork marks memo as done after notification`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        coVerify { Repository.saveMemo(memo.copy(isDone = true)) }
    }

    @Test
    fun `doWork removes geofence after notification`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } returns memo.id

        buildWorker(memoId = memo.id).doWork()

        coVerify { locationManager.removeReminder(memo.id) }
    }

    @Test
    fun `doWork returns success on the happy path`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } returns memo.id

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success even when saveMemo throws`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } throws RuntimeException("DB error")

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun `doWork returns success even when removeReminder throws`() = runTest {
        val memo = testMemo()
        coEvery { Repository.getMemoById(memo.id) } returns memo
        coEvery { Repository.saveMemo(any()) } returns memo.id
        coEvery { locationManager.removeReminder(any()) } throws RuntimeException("GMS error")

        val result = buildWorker(memoId = memo.id).doWork()

        assertEquals(Result.success(), result)
    }

    private fun testMemo(
        id: Long = 1L,
        lat: Long = 52.0.toBits(),
        lng: Long = 13.0.toBits(),
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
