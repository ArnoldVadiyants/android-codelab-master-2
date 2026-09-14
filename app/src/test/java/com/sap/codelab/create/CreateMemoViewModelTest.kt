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
import kotlinx.coroutines.launch
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
        coEvery { Repository.saveMemo(any()) } returns 1L
        viewModel = CreateMemoViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `trySave sets both errors when title and description are blank`() {
        viewModel.trySave("", "")
        assertTrue(viewModel.uiState.value.titleError)
        assertTrue(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `trySave sets title error when title is blank`() {
        viewModel.trySave("", "Some description")
        assertTrue(viewModel.uiState.value.titleError)
        assertFalse(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `trySave sets description error when description is blank`() {
        viewModel.trySave("Title", "")
        assertFalse(viewModel.uiState.value.titleError)
        assertTrue(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `trySave sets both errors when title and description are whitespace`() {
        viewModel.trySave("   ", "   ")
        assertTrue(viewModel.uiState.value.titleError)
        assertTrue(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun `trySave emits savedEvent when memo is valid`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<Unit>()
        val job = launch { viewModel.savedEvent.collect { events.add(it) } }

        viewModel.trySave("My title", "My description")

        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `trySave does not emit savedEvent when title is blank`() = runTest(UnconfinedTestDispatcher()) {
        val events = mutableListOf<Unit>()
        val job = launch { viewModel.savedEvent.collect { events.add(it) } }

        viewModel.trySave("", "Description")

        assertTrue(events.isEmpty())
        job.cancel()
    }

    @Test
    fun `location is null initially`() {
        assertNull(viewModel.uiState.value.location)
    }

    @Test
    fun `updateLocation stores the given location`() {
        val latLng = LatLng(52.52, 13.40)
        viewModel.updateLocation(latLng)
        assertEquals(latLng, viewModel.uiState.value.location)
    }

    @Test
    fun `clearLocation sets location back to null`() {
        viewModel.updateLocation(LatLng(52.52, 13.40))
        viewModel.clearLocation()
        assertNull(viewModel.uiState.value.location)
    }

    @Test
    fun `updateLocation replaces the previous location`() {
        viewModel.updateLocation(LatLng(1.0, 2.0))
        val newLocation = LatLng(52.52, 13.40)
        viewModel.updateLocation(newLocation)
        assertEquals(newLocation, viewModel.uiState.value.location)
    }

    @Test
    fun `trySave encodes selected location as Double in persisted memo`() = runTest {
        val lat = 52.52
        val lng = 13.40
        viewModel.updateLocation(LatLng(lat, lng))

        val savedMemoSlot = slot<Memo>()
        coEvery { Repository.saveMemo(capture(savedMemoSlot)) } returns 1L

        viewModel.trySave("Title", "Desc")

        assertEquals(lat, savedMemoSlot.captured.reminderLatitude, 0.0)
        assertEquals(lng, savedMemoSlot.captured.reminderLongitude, 0.0)
    }

    @Test
    fun `trySave stores zero coordinates when no location is selected`() = runTest {
        val savedMemoSlot = slot<Memo>()
        coEvery { Repository.saveMemo(capture(savedMemoSlot)) } returns 1L

        viewModel.trySave("Title", "Desc")

        assertEquals(0.0, savedMemoSlot.captured.reminderLatitude, 0.0)
        assertEquals(0.0, savedMemoSlot.captured.reminderLongitude, 0.0)
    }

    @Test
    fun `trySave registers geofence when location is selected`() = runTest {
        val lat = 52.52
        val lng = 13.40
        viewModel.updateLocation(LatLng(lat, lng))
        coEvery { Repository.saveMemo(any()) } returns 42L

        viewModel.trySave("Title", "Desc")

        coVerify { locationManager.addReminder(memoId = 42L, latitude = lat, longitude = lng) }
    }

    @Test
    fun `trySave does not register geofence when no location is selected`() = runTest {
        viewModel.trySave("Title", "Desc")

        coVerify(exactly = 0) { locationManager.addReminder(any(), any(), any()) }
    }
}
