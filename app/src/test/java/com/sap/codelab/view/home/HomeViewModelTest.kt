package com.sap.codelab.view.home

import app.cash.turbine.test
import com.sap.codelab.AppDependencies
import com.sap.codelab.location.LocationReminderManager
import com.sap.codelab.model.Memo
import com.sap.codelab.repository.Repository
import com.sap.codelab.utils.coroutines.ScopeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
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
    private lateinit var viewModel: HomeViewModel
    private lateinit var locationManager: LocationReminderManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(Repository)
        mockkObject(AppDependencies)
        mockkObject(ScopeProvider)
        // Redirect ScopeProvider.application to testDispatcher so coroutines are controlled
        every { ScopeProvider.application } returns CoroutineScope(testDispatcher)
        locationManager = mockk(relaxed = true)
        every { AppDependencies.locationReminderManager } returns locationManager
        viewModel = HomeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `updateMemo does nothing when isChecked is false`() = runTest {
        viewModel.updateMemo(testMemo(), isChecked = false)
        coVerify(exactly = 0) { Repository.saveMemo(any()) }
        coVerify(exactly = 0) { locationManager.removeReminder(any()) }
    }

    @Test
    fun `updateMemo marks memo as done when isChecked is true`() = runTest {
        val memo = testMemo()
        coEvery { Repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify { Repository.saveMemo(memo.copy(isDone = true)) }
    }

    @Test
    fun `updateMemo removes geofence when memo has a location reminder`() = runTest {
        val memo = testMemo(lat = 52.0.toBits(), lng = 13.0.toBits())
        coEvery { Repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify { locationManager.removeReminder(memo.id) }
    }

    @Test
    fun `updateMemo does not remove geofence when memo has no location reminder`() = runTest {
        val memo = testMemo() // no coordinates
        coEvery { Repository.saveMemo(any()) } returns memo.id

        viewModel.updateMemo(memo, isChecked = true)

        coVerify(exactly = 0) { locationManager.removeReminder(any()) }
    }

    @Test
    fun `loadAllMemos emits returned memos to StateFlow`() = runTest {
        val memos = listOf(testMemo(1), testMemo(2))
        coEvery { Repository.getAll() } returns memos

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
        coEvery { Repository.getOpen() } returns openMemos

        viewModel.memos.test {
            awaitItem() // skip initial empty list
            viewModel.loadOpenMemos()
            assertEquals(openMemos, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshMemos calls getAll when last load was loadAllMemos`() = runTest {
        coEvery { Repository.getAll() } returns emptyList()

        viewModel.loadAllMemos()
        viewModel.refreshMemos()

        coVerify(exactly = 2) { Repository.getAll() }
        coVerify(exactly = 0) { Repository.getOpen() }
    }

    @Test
    fun `refreshMemos calls getOpen when last load was loadOpenMemos`() = runTest {
        coEvery { Repository.getOpen() } returns emptyList()

        viewModel.loadOpenMemos()
        viewModel.refreshMemos()

        coVerify(exactly = 2) { Repository.getOpen() }
        coVerify(exactly = 0) { Repository.getAll() }
    }

    @Test
    fun `memos StateFlow starts with an empty list`() {
        assertTrue(viewModel.memos.value.isEmpty())
    }

    private fun testMemo(id: Long = 1L, lat: Long = 0L, lng: Long = 0L) = Memo(
        id = id,
        title = "Test memo",
        description = "Description",
        reminderDate = 0L,
        reminderLatitude = lat,
        reminderLongitude = lng
    )
}
