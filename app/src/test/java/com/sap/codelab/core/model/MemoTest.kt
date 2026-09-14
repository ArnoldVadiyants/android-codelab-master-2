package com.sap.codelab.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoTest {

    private fun memo(lat: Double = 0.0, lng: Double = 0.0) = Memo(
        id = 1L,
        title = "Title",
        description = "Description",
        reminderDate = 0L,
        reminderLatitude = lat,
        reminderLongitude = lng
    )

    @Test
    fun `hasLocationReminder is false when both coordinates are zero`() {
        assertFalse(memo(0.0, 0.0).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when latitude is non-zero`() {
        assertTrue(memo(lat = 52.0).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when longitude is non-zero`() {
        assertTrue(memo(lng = 13.0).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when both coordinates are non-zero`() {
        assertTrue(memo(lat = 52.0, lng = 13.0).hasLocationReminder)
    }

    @Test
    fun `reminderLocation is null when no location is set`() {
        assertNull(memo(0.0, 0.0).reminderLocation)
    }

    @Test
    fun `reminderLocation roundtrip preserves positive coordinates`() {
        val lat = 52.5200
        val lng = 13.4050
        val location = memo(lat, lng).reminderLocation
        assertNotNull(location)
        assertEquals(lat, location!!.latitude, 0.0)
        assertEquals(lng, location.longitude, 0.0)
    }

    @Test
    fun `reminderLocation roundtrip preserves negative coordinates`() {
        val lat = -33.8688
        val lng = -70.6693
        val location = memo(lat, lng).reminderLocation
        assertNotNull(location)
        assertEquals(lat, location!!.latitude, 0.0)
        assertEquals(lng, location.longitude, 0.0)
    }

    @Test
    fun `reminderLocation returns non-null when only longitude is set`() {
        val location = memo(lat = 0.0, lng = 13.0).reminderLocation
        assertNotNull(location)
        assertEquals(0.0, location!!.latitude, 0.0)
    }

    @Test
    fun `copy preserves all fields and only changes the specified one`() {
        val original = memo(52.0, 13.0)
        val copy = original.copy(isDone = true)
        assertEquals(original.reminderLatitude, copy.reminderLatitude, 0.0)
        assertEquals(original.reminderLongitude, copy.reminderLongitude, 0.0)
        assertFalse(original.isDone)
        assertTrue(copy.isDone)
    }
}
