package com.sap.codelab.create

import com.sap.codelab.AppDependencies
import com.sap.codelab.core.location.LatLng
import com.sap.codelab.core.location.LocationReminderManager
import com.sap.codelab.core.model.Memo
import com.sap.codelab.core.repository.Repository
import com.sap.codelab.core.utils.coroutines.ScopeProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateMemoViewModelTest {

    private lateinit var viewModel: CreateMemoViewModel
    private lateinit var locationManager: LocationReminderManager

    @Before
    fun setUp() {
        mockkObject(Repository)
        mockkObject(AppDependencies)
        mockkObject(ScopeProvider)
        every { ScopeProvider.application } returns CoroutineScope(UnconfinedTestDispatcher())
        locationManager = mockk(relaxed = true)
        every { AppDependencies.locationReminderManager } returns locationManager
        viewModel = CreateMemoViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `isMemoValid is false before any input`() {
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false when title is blank`() {
        viewModel.updateMemo("", "Some description")
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false when description is blank`() {
        viewModel.updateMemo("Title", "")
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is false when both title and description are whitespace`() {
        viewModel.updateMemo("   ", "   ")
        assertFalse(viewModel.isMemoValid())
    }

    @Test
    fun `isMemoValid is true when both title and description are non-blank`() {
        viewModel.updateMemo("My title", "My description")
        assertTrue(viewModel.isMemoValid())
    }

    @Test
    fun `hasTitleError is true when title is blank`() {
        viewModel.updateMemo("", "Description")
        assertTrue(viewModel.hasTitleError())
    }

    @Test
    fun `hasTitleError is false when title is non-blank`() {
        viewModel.updateMemo("Title", "Description")
        assertFalse(viewModel.hasTitleError())
    }

    @Test
    fun `hasTextError is true when description is blank`() {
        viewModel.updateMemo("Title", "")
        assertTrue(viewModel.hasTextError())
    }

    @Test
    fun `hasTextError is false when description is non-blank`() {
        viewModel.updateMemo("Title", "Description")
        assertFalse(viewModel.hasTextError())
    }

    @Test
    fun `location is null initially`() {
        assertNull(viewModel.location)
    }

    @Test
    fun `updateLocation stores the given location`() {
        val latLng = LatLng(52.52, 13.40)
        viewModel.updateLocation(latLng)
        assertEquals(latLng, viewModel.location)
    }

    @Test
    fun `clearLocation sets location back to null`() {
        viewModel.updateLocation(LatLng(52.52, 13.40))
        viewModel.clearLocation()
        assertNull(viewModel.location)
    }

    @Test
    fun `updateLocation replaces the previous location`() {
        viewModel.updateLocation(LatLng(1.0, 2.0))
        val newLocation = LatLng(52.52, 13.40)
        viewModel.updateLocation(newLocation)
        assertEquals(newLocation, viewModel.location)
    }

    @Test
    fun `saveMemo encodes selected location as Double in persisted memo`() = runTest {
        val lat = 52.52
        val lng = 13.40
        viewModel.updateLocation(LatLng(lat, lng))
        viewModel.updateMemo("Title", "Desc")

        val savedMemoSlot = slot<Memo>()
        coEvery { Repository.saveMemo(capture(savedMemoSlot)) } returns 1L

        viewModel.saveMemo()

        assertEquals(lat, savedMemoSlot.captured.reminderLatitude, 0.0)
        assertEquals(lng, savedMemoSlot.captured.reminderLongitude, 0.0)
    }

    @Test
    fun `saveMemo stores zero coordinates when no location is selected`() = runTest {
        viewModel.updateMemo("Title", "Desc")

        val savedMemoSlot = slot<Memo>()
        coEvery { Repository.saveMemo(capture(savedMemoSlot)) } returns 1L

        viewModel.saveMemo()

        assertEquals(0.0, savedMemoSlot.captured.reminderLatitude, 0.0)
        assertEquals(0.0, savedMemoSlot.captured.reminderLongitude, 0.0)
    }

    @Test
    fun `saveMemo registers geofence when location is selected`() = runTest {
        val lat = 52.52
        val lng = 13.40
        viewModel.updateLocation(LatLng(lat, lng))
        viewModel.updateMemo("Title", "Desc")

        coEvery { Repository.saveMemo(any()) } returns 42L

        viewModel.saveMemo()

        coVerify { locationManager.addReminder(memoId = 42L, latitude = lat, longitude = lng) }
    }

    @Test
    fun `saveMemo does not register geofence when no location is selected`() = runTest {
        viewModel.updateMemo("Title", "Desc")
        coEvery { Repository.saveMemo(any()) } returns 1L

        viewModel.saveMemo()

        coVerify(exactly = 0) { locationManager.addReminder(any(), any(), any()) }
    }
}
