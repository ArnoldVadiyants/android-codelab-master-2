package com.sap.codelab.home

import app.cash.turbine.test
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.IMemoRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: IMemoRepository
    private lateinit var locationManager: LocationReminderManager
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        locationManager = mockk(relaxed = true)
        viewModel = HomeViewModel(repository, locationManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateMemo does nothing when isChecked is false`() = runTest {
        viewModel.updateMemo(testMemo(), isChecked = false)
        coVerify(exactly = 0) { repository.saveMemo(any()) }
        coVerify(exactly = 0) { locationManager.removeReminder(any()) }
    }

    @Test
    fun `updateMemo marks memo as done when isChecked is true`() = runTest {
        val memo = testMemo()
        coEvery { repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify { repository.saveMemo(memo.copy(isDone = true)) }
    }

    @Test
    fun `updateMemo removes geofence when memo has a location reminder`() = runTest {
        val memo = testMemo(lat = 52.0, lng = 13.0)
        coEvery { repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify { locationManager.removeReminder(memo.id) }
    }

    @Test
    fun `updateMemo does not remove geofence when memo has no location reminder`() = runTest {
        val memo = testMemo() // no coordinates
        coEvery { repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify(exactly = 0) { locationManager.removeReminder(any()) }
    }

    @Test
    fun `loadAllMemos emits returned memos to StateFlow`() = runTest {
        val memos = listOf(testMemo(1), testMemo(2))
        coEvery { repository.getAll() } returns memos

        viewModel.memos.test {
            awaitItem() // skip initial empty list
            viewModel.loadAllMemos()
            assertEquals(memos, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadOpenMemos emits only open memos to StateFlow`() = runTest {
        val openMemos = listOf(testMemo(3))
        coEvery { repository.getOpen() } returns openMemos

        viewModel.memos.test {
            awaitItem() // skip initial empty list
            viewModel.loadOpenMemos()
            assertEquals(openMemos, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshMemos calls getAll when last load was loadAllMemos`() = runTest {
        coEvery { repository.getAll() } returns emptyList()

        viewModel.loadAllMemos()
        viewModel.refreshMemos()

        coVerify(exactly = 2) { repository.getAll() }
        coVerify(exactly = 0) { repository.getOpen() }
    }

    @Test
    fun `refreshMemos calls getOpen when last load was loadOpenMemos`() = runTest {
        coEvery { repository.getOpen() } returns emptyList()

        viewModel.loadOpenMemos()
        viewModel.refreshMemos()

        coVerify(exactly = 2) { repository.getOpen() }
        coVerify(exactly = 0) { repository.getAll() }
    }

    @Test
    fun `memos StateFlow starts with an empty list`() {
        assertTrue(viewModel.memos.value.isEmpty())
    }

    private fun testMemo(id: Long = 1L, lat: Double = 0.0, lng: Double = 0.0) = Memo(
        id = id,
        title = "Test memo",
        description = "Description",
        reminderDate = 0L,
        reminderLatitude = lat,
        reminderLongitude = lng
    )
}
