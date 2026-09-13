package com.sap.codelab.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoTest {

    // Helper: build a memo with only the coordinate fields variable
    private fun memo(lat: Long = 0L, lng: Long = 0L) = Memo(
        id = 1L,
        title = "Title",
        description = "Description",
        reminderDate = 0L,
        reminderLatitude = lat,
        reminderLongitude = lng
    )

    @Test
    fun `hasLocationReminder is false when both coordinates are zero`() {
        assertFalse(memo(0L, 0L).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when latitude is non-zero`() {
        assertTrue(memo(lat = 52.0.toBits()).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when longitude is non-zero`() {
        assertTrue(memo(lng = 13.0.toBits()).hasLocationReminder)
    }

    @Test
    fun `hasLocationReminder is true when both coordinates are non-zero`() {
        assertTrue(memo(lat = 52.0.toBits(), lng = 13.0.toBits()).hasLocationReminder)
    }

    @Test
    fun `reminderLocation is null when no location is set`() {
        assertNull(memo(0L, 0L).reminderLocation)
    }

    @Test
    fun `reminderLocation roundtrip preserves positive coordinates`() {
        val lat = 52.5200
        val lng = 13.4050
        val location = memo(lat.toBits(), lng.toBits()).reminderLocation
        assertNotNull(location)
        assertEquals(lat, location!!.latitude, 0.0)
        assertEquals(lng, location.longitude, 0.0)
    }

    @Test
    fun `reminderLocation roundtrip preserves negative coordinates`() {
        val lat = -33.8688
        val lng = -70.6693
        val location = memo(lat.toBits(), lng.toBits()).reminderLocation
        assertNotNull(location)
        assertEquals(lat, location!!.latitude, 0.0)
        assertEquals(lng, location.longitude, 0.0)
    }

    @Test
    fun `reminderLocation returns non-null when only longitude is set`() {
        // lat=0L means 0.0 degrees (not "no location") when lng is also set
        val location = memo(lat = 0L, lng = 13.0.toBits()).reminderLocation
        assertNotNull(location)
        assertEquals(0.0, location!!.latitude, 0.0)
    }

    @Test
    fun `copy preserves all fields and only changes the specified one`() {
        val original = memo(52.0.toBits(), 13.0.toBits())
        val copy = original.copy(isDone = true)
        assertEquals(original.reminderLatitude, copy.reminderLatitude)
        assertEquals(original.reminderLongitude, copy.reminderLongitude)
        assertFalse(original.isDone)
        assertTrue(copy.isDone)
    }
}
